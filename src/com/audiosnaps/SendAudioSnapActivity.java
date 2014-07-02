/**
 * 
 */
package com.audiosnaps;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apache.commons.codec.DecoderException;
import com.audiosnap.library.AudioParams;
import com.audiosnap.library.BitmapLoader;
import com.audiosnap.library.SoundAndShot;
import com.audiosnap.library.util.BitmapUtil;
import com.audiosnap.library.util.ExifUtil;
import com.audiosnap.library.util.IOUtil;
import com.audiosnap.library.util.PCMUtil;
import com.audiosnaps.adapters.MentionedFriendsListAdapter;
import com.audiosnaps.classes.Animaciones;
import com.audiosnaps.classes.AudioPlayer;
import com.audiosnaps.classes.AudioSnapsFileCache;
import com.audiosnaps.classes.ConnectionDetector;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.ex.NoAudioException;
import com.audiosnaps.facebook.FacebookManager;
import com.audiosnaps.http.AddSocNetworkFromMobile;
import com.audiosnaps.http.GetCompleteFriendsList;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.UploadPhoto;
import com.audiosnaps.http.UploadPhotoMetaData;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.login.LoginRegisterActivity;
import com.audiosnaps.twitter.TwitterLoginActivity;
import com.audiosnaps.view.CommentsEditText;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.sromku.simple.fb.SimpleFacebook;
import com.todoroo.aacenc.AACEncoder;
import com.todoroo.aacenc.AACToM4A;

/**
 * @author David
 * 
 */
public class SendAudioSnapActivity extends Activity {

	private static final String TAG = "SendAudioSnapActivity";

	public static final String AS_FILE_PREFIX = "AS_";
	public static final String AS_FILE_SUFFIX = ".jpg";

	private static final float OVER_REMAINING = 0.08212f;
	private RelativeLayout over, under;
	private boolean underVisible;
	private Animaciones animaciones;
	private Button visibleBtn, hiddenBtn;
	private TextView sendPictureTextView;

	// Origins
	private static final String APPLICATION = "application";
	private static final String SDCARD_AUDIOSNAP = "sdcard-audiosnap";
	private static final String SDCARD_SOUND_SHOT = "sdcard-sound&shot";

	private int soundAndShotBottomY;

	private PlayAudio playTask;
	private boolean isPlaying;
	private File pcm;
	private File jpeg;
	private File m4a;
	private ImageView stamp;
	private Context context = this;
	private CommentsEditText commentsTextView;
	private CheckBox btnTwitterSendPic, btnFacebookSendPic;

	private String aacFilePath;
	public String m4aFilePath;

	private short[] samples;

	private boolean isProcessing;
	private int format;
	private Bitmap bitmap;

	private String origin;
	private AudioPlayer audioPlayer;

	private JSONArray jsonArrayFriends;
	private MentionedFriendsListAdapter mentionedFriendsListAdapter;

	private AudioSnapsFileCache audioSnapsFileCache;

	private SharedPreferences prefs;

	private RelativeLayout preview;

	private ImageButton dismissBtn, okBtn;

	private Button saveButton;

	private boolean done;

	private SimpleFacebook mSimpleFacebook;

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Hide status-bar
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Hide title-bar, must be before setContentView
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// fix screen orientation to portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.taken);

		prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		soundAndShotBottomY = BitmapUtil.getPixels(50 + 10, this);

		// Referencia al layout de la foto
		over = (RelativeLayout) findViewById(R.id.over);
		under = (RelativeLayout) findViewById(R.id.under);

		// El layout de la foto tapa el del caption
		underVisible = false;
		under.setVisibility(View.GONE);

		animaciones = new Animaciones(this);
		context = this;

		aacFilePath = new AudioSnapsFileCache().getTmpDir().getAbsolutePath() + "/tmp.aac";
		m4aFilePath = new AudioSnapsFileCache().getTmpDir().getAbsolutePath() + "/tmp.m4a";

		// Views
		final TextView lblContadorCaracteresCaption = (TextView) findViewById(R.id.lbContadorCaracteresCaption);
		commentsTextView = (CommentsEditText) findViewById(R.id.commentsTextView);

		btnFacebookSendPic = (CheckBox) findViewById(R.id.btnFacebookSendPic);

		if (!prefs.getBoolean(BaseActivity.FACEBOOK_PUBLISH_ACTIONS, false)) {
			btnFacebookSendPic.setSelected(false);

			btnFacebookSendPic.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					// add_soc_network_from_mobile
					final Handler handler = new Handler() {
						public void handleMessage(Message msg) {
							final String result = (String) msg.obj;
							if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

								if (msg.arg1 == BaseActivity.FACEBOOK_HAS_PUBLISH_ACTIONS) {

									SharedPreferences sharedPreferences = getActivity().getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = sharedPreferences.edit();
									editor.putBoolean(BaseActivity.FACEBOOK_IS_LOGGED_IN, true);
									editor.putBoolean(BaseActivity.FACEBOOK_PUBLISH_ACTIONS, true);
									editor.commit();

									btnFacebookSendPic.setSelected(true);

									btnFacebookSendPic.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {

											if (BaseActivity.DEBUG)
												MyLog.d(TAG, "Pulsamos botón tw like");

											if (btnFacebookSendPic.isSelected()) {
												btnFacebookSendPic.setSelected(false);
											} else {
												btnFacebookSendPic.setSelected(true);
											}
										}
									});

								} else {

									SharedPreferences sharedPreferences = getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = sharedPreferences.edit();
									editor.putBoolean(BaseActivity.FACEBOOK_IS_LOGGED_IN, true);
									editor.commit();

									btnFacebookSendPic.setSelected(false);
								}

							} else {

								btnFacebookSendPic.setSelected(false);

							}
						};
					};

					FacebookManager.askForReadWritePermissionsAndAddSocNetwork(getActivity(), mSimpleFacebook, handler);

				}
			});

		} else {

			btnFacebookSendPic.setSelected(true);

			btnFacebookSendPic.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Pulsamos botón tw like");

					if (btnFacebookSendPic.isSelected()) {
						btnFacebookSendPic.setSelected(false);
					} else {
						btnFacebookSendPic.setSelected(true);
					}
				}
			});
		}

		btnTwitterSendPic = (CheckBox) findViewById(R.id.btnTwitterSendPic);

		btnTwitterSendPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "Pulsamos botón tw like");

				if (!prefs.getBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, false)) {
					btnTwitterSendPic.setSelected(false);
					loginToTwitter();
				} else if (btnTwitterSendPic.isSelected()) {
					btnTwitterSendPic.setSelected(false);
				} else {
					btnTwitterSendPic.setSelected(true);
				}

			}
		});

		if (!prefs.getBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, false)) {
			btnTwitterSendPic.setSelected(false);
		} else {
			btnTwitterSendPic.setSelected(true);
		}

		initMentionedFriendsList();

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				origin = handleImage(intent); // Handle single image being sent
			}
		} else if (TakeAudioSnapActivity.ACTION_TAKE.equals(action)) {
			pcm = new File(intent.getStringExtra(TakeAudioSnapActivity.EXTRA_AUDIO_PATH));
			jpeg = new File(intent.getStringExtra(TakeAudioSnapActivity.EXTRA_PICTURE_PATH));
			format = intent.getIntExtra(TakeAudioSnapActivity.EXTRA_FORMAT, BaseActivity.FORMAT_1_1);
			if (BaseActivity.DEBUG)
				MyLog.e(TAG, "format: " + (format == BaseActivity.FORMAT_4_3 ? "4:3" : "1:1"));
			origin = APPLICATION;
		}

		if (origin == null)
			finish();

		ImageView picture = (ImageView) findViewById(R.id.picture);

		// load picture with correct dimensions
		if (origin.equals(APPLICATION)) {

			int w = 576, h = 576;

			if (format == BaseActivity.FORMAT_4_3)
				h = 768;

			bitmap = BitmapLoader.loadBitmap(jpeg.getPath(), w, h);

			if (bitmap.getHeight() != 576 && bitmap.getWidth() != 576) {
				if (bitmap.getHeight() > bitmap.getWidth())
					bitmap = Bitmap.createScaledBitmap(bitmap, 576, 576 * bitmap.getHeight() / bitmap.getWidth(), true);
				else
					bitmap = Bitmap.createScaledBitmap(bitmap, 576 * bitmap.getWidth() / bitmap.getHeight(), 576, true);
			}

		} else if (origin.equals(SDCARD_SOUND_SHOT)) {

			bitmap = BitmapLoader.loadBitmap(jpeg.getPath(), 576);

			if (bitmap.getHeight() != 576 && bitmap.getWidth() != 576) {

				int w, h;

				if (bitmap.getHeight() > bitmap.getWidth()) {
					w = 576;
					h = 576 * bitmap.getHeight() / bitmap.getWidth();
				} else {
					w = 576 * bitmap.getWidth() / bitmap.getHeight();
					h = 576;
				}

				bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
			}

		} else {

			bitmap = BitmapFactory.decodeFile(jpeg.getPath());

		}

		// rotation
		if (origin.equals(APPLICATION)) {

			int rotation = 0;
			Matrix matrix = new Matrix();

			switch (intent.getIntExtra(TakeAudioSnapActivity.EXTRA_ORIENTATION, BaseActivity.ORIENTATION_PORTRAIT_NORMAL)) {

			case BaseActivity.ORIENTATION_PORTRAIT_NORMAL:
				rotation = 90;
				break;
			case BaseActivity.ORIENTATION_LANDSCAPE_NORMAL:
				rotation = 0;
				break;
			case BaseActivity.ORIENTATION_PORTRAIT_INVERTED:
				rotation = 270;
				break;
			case BaseActivity.ORIENTATION_LANDSCAPE_INVERTED:
				rotation = 180;
				break;
			}

			matrix.postRotate(rotation);

			if (BaseActivity.DEBUG)
				MyLog.e(TAG, "rotation: " + rotation);

			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		}

		int x = 1, y = 1;

		// clipping
		if (origin.equals(APPLICATION)) {

			if (format == BaseActivity.FORMAT_4_3) {
				x = 4;
				y = 3;
			}

			if (BaseActivity.DEBUG)
				MyLog.e(TAG, "format: " + (format == BaseActivity.FORMAT_4_3 ? "format 4:3" : "format 1:1"));

			bitmap = BitmapUtil.clip(bitmap, x, y);

		}

		stamp = (ImageView) findViewById(R.id.stamp);

		dismissBtn = (ImageButton) findViewById(R.id.dismissBtn);
		okBtn = (ImageButton) findViewById(R.id.okBtn);

		saveButton = (Button) findViewById(R.id.button2);
		visibleBtn = (Button) findViewById(R.id.bt_visible);
		hiddenBtn = (Button) findViewById(R.id.bt_hidden);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		int w = displaymetrics.widthPixels;

		int h = displaymetrics.widthPixels * bitmap.getHeight() / bitmap.getWidth();

		// en caso de que la altura de la imagen sea más grande de la altura
		// máxima hay que recortar la foto
		if (h > BaseActivity.screenHeight - 2 * soundAndShotBottomY) {

			picture.setImageBitmap(BitmapUtil.getRoundedCornerBitmap(Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true), 0, 0, w, h - soundAndShotBottomY), 5, this));

			picture.setPadding(0, 0, 0, soundAndShotBottomY);
			ObjectAnimator.ofFloat(stamp, "translationY", -soundAndShotBottomY).setDuration(0).start();

		} else {

			picture.setImageBitmap(BitmapUtil.getRoundedCornerBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true), 5, this));

		}

		final Context context = this;

		dismissBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (origin.equals(APPLICATION)) {
					Intent intent = new Intent(context, TakeAudioSnapActivity.class);
					startActivity(intent);
				}
				finish();
			}
		});

		okBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// Comprobamos contectividad
				if (new ConnectionDetector(context).isConnectingToInternet()) {
					animaciones.translateUpDownLayout(BaseActivity.POSITION_CENTER, over, OVER_REMAINING);
					underVisible = true;

					under.setVisibility(View.VISIBLE);

					dismissBtn.setVisibility(View.GONE);
					okBtn.setVisibility(View.GONE);

					if (!done) {
						SendAudioSnap createTask = new SendAudioSnap();
						createTask.execute();
					}

				} else {
					isNotUploaded();
					// new AudioSnapsFileCache().addPhotoToGallery(jpeg,
					// context);

					if (!done) {
						if (origin.equals(APPLICATION)) {
							CreateAudioSnap createTask = new CreateAudioSnap();
							createTask.execute();
						}
					}
				}
			}
		});

		sendPictureTextView = (TextView) findViewById(R.id.send_picture_txt);

		final String privacyMode = prefs.getString(HttpConnections.PRIVACY_MODE, HttpConnections.ROCKSTAR);

		if (privacyMode.equals(HttpConnections.ROCKSTAR))
			sendPictureTextView.setText(Html.fromHtml(getString(R.string.HELP_VISIBLE_ROCKSTAR)));
		else
			sendPictureTextView.setText(Html.fromHtml(getString(R.string.HELP_VISIBLE_GRANDMA)));

		visibleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				visibleBtn.setVisibility(View.GONE);
				hiddenBtn.setVisibility(View.VISIBLE);
				if (privacyMode.equals(HttpConnections.ROCKSTAR))
					sendPictureTextView.setText(Html.fromHtml(getString(R.string.HELP_HIDDEN_ROCKSTAR)));
				else
					sendPictureTextView.setText(Html.fromHtml(getString(R.string.HELP_HIDDEN_GRANDMA)));
			}
		});

		hiddenBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hiddenBtn.setVisibility(View.GONE);
				visibleBtn.setVisibility(View.VISIBLE);
				if (privacyMode.equals(HttpConnections.ROCKSTAR))
					sendPictureTextView.setText(Html.fromHtml(getString(R.string.HELP_VISIBLE_ROCKSTAR)));
				else
					sendPictureTextView.setText(Html.fromHtml(getString(R.string.HELP_VISIBLE_GRANDMA)));

			}
		});

		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Metemos temporalmente aquí el ok para el envío de la metadata
				final Handler handler = new Handler() {
					@SuppressLint("HandlerLeak")
					public void handleMessage(Message msg) {
						final String result = (String) msg.obj;
						try {
							if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
								Toast.makeText(context, context.getResources().getString(R.string.uploadedOk), Toast.LENGTH_LONG).show();
								MainActivity.fotoUploaded = true;
								finish();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				};

				boolean isPrivate = false;
				if (hiddenBtn.getVisibility() == View.VISIBLE) {
					isPrivate = true;
				}

				SharedPreferences prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
				new UploadPhotoMetaData(context, handler, LoggedUser.id, prefs.getString(HttpConnections.UPLOAD_HASH, ""), commentsTextView.getFormattedComment(), btnFacebookSendPic.isSelected(), btnTwitterSendPic
						.isSelected(), isPrivate, "0", 0, 0, LoggedUser.koeToken).execute();
			}
		});

		// EditText listener
		commentsTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				int cuentaCaracteresCaption = BaseActivity.MAX_SIZE_COMMENT - commentsTextView.getText().length();
				if (cuentaCaracteresCaption >= 0) {
					lblContadorCaracteresCaption.setText(String.valueOf(cuentaCaracteresCaption));
					// habilitar el botón
					if (!saveButton.isEnabled()) {
						saveButton.setEnabled(true);
						saveButton.getBackground().setAlpha(255);
					}
				} else {
					lblContadorCaracteresCaption.setText(String.valueOf(0));
					// deshabilitar el botón
					if (saveButton.isEnabled()) {
						saveButton.setEnabled(false);
						saveButton.getBackground().setAlpha(127);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

		preview = (RelativeLayout) findViewById(R.id.preview);

		// Ampliar el área clicable
		preview.post(new Runnable() {
			public void run() {
				// Post in the parent's message queue to make sure the parent
				// lays out its children before we call getHitRect()
				Rect delegateArea = new Rect();
				ImageView delegate = stamp;
				delegate.getHitRect(delegateArea);
				delegateArea.top -= 50;
				delegateArea.left -= 50;
				delegateArea.right += 50;
				TouchDelegate expandedArea = new TouchDelegate(delegateArea, delegate);

				// give the delegate to an ancestor of the view we're
				// delegating the area to
				if (View.class.isInstance(delegate.getParent())) {
					((View) delegate.getParent()).setTouchDelegate(expandedArea);
				}
			};
		});

		stamp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// fade out stamp icon
				animaciones.fadeOutStamp(stamp);

				if (!origin.equals(SDCARD_AUDIOSNAP)) {

					playTask = new PlayAudio();
					// Play audio
					playTask.execute();

				} else {

					audioPlayer = null;

					audioPlayer = new AudioPlayer(new Runnable() {
						@Override
						public void run() {
							// fade in stamp
							animaciones.fadeInStamp(stamp);
							stamp.setClickable(true);
						}
					});

					audioPlayer.setFileSource(m4a);

					stamp.setClickable(false);
					audioPlayer.play();

				}

			}
		});

		if (!origin.equals(SDCARD_AUDIOSNAP)) {
			ProcessAudio processTask = new ProcessAudio();
			processTask.execute();
		}

	}

	private class PlayAudio extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... params) {

			while (isProcessing)
				;

			isPlaying = true;
			int bufferSize = AudioTrack.getMinBufferSize(AudioParams.frequency, AudioFormat.CHANNEL_OUT_MONO, AudioParams.audioEncoding);
			short[] audiodata = new short[bufferSize / 4];

			try {

				DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(pcm)));
				AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AudioParams.frequency, AudioFormat.CHANNEL_OUT_MONO, AudioParams.audioEncoding, bufferSize, AudioTrack.MODE_STREAM);

				if (BaseActivity.DEBUG)
					MyLog.e("play", "playing audio");

				audioTrack.play();
				while (isPlaying && dis.available() > 0) {
					int i = 0;
					while (dis.available() > 0 && i < audiodata.length) {
						audiodata[i] = dis.readShort();
						i++;
					}
					audioTrack.write(audiodata, 0, audiodata.length);
				}
				isPlaying = false;
				dis.close();

			} catch (Throwable t) {
				if (BaseActivity.DEBUG)
					MyLog.e("AudioTrack", "Playback Failed: " + t.getLocalizedMessage());
			}
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			// DO NOTHING
		}

		protected void onPostExecute(Void result) {
			// fade in stamp icon
			animaciones.fadeInStamp(stamp);
			stamp.setEnabled(true);
		}
	}

	private class ProcessAudio extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... params) {

			System.out.println("Process audio start");

			isProcessing = true;

			// split and save last 5 seconds
			// if(BaseActivity.DEBUG) MyLog.e("pcm", "File length: " +
			// pcm.length());
			// if(BaseActivity.DEBUG) MyLog.e("pcm", "max: "+
			// AudioParams.timeInSeconds *
			// AudioParams.frequency * 2);

			// long start = System.currentTimeMillis();
			// long stop = 0;

			try {
				ByteBuffer buffer = IOUtil.readFileByteBuffer(pcm, ByteOrder.BIG_ENDIAN);
				// stop = System.currentTimeMillis();
				// System.out.println("read: " + (stop - start) + "ms");

				if (pcm.length() > AudioParams.timeInSeconds * AudioParams.frequency * 2)
					buffer = PCMUtil.removeFirstBytes(buffer, AudioParams.timeInSeconds * AudioParams.frequency * 2);

				// stop = System.currentTimeMillis();
				// System.out.println("cut: " + (stop - start) + "ms");

				samples = PCMUtil.fadeInfadeOut(buffer, 10);
				// stop = System.currentTimeMillis();
				// System.out.println("fade: " + (stop - start) + "ms");

				IOUtil.writeShortArrayToFile(samples, pcm);
				// stop = System.currentTimeMillis();
				// System.out.println("write: " + (stop - start) + "ms");

			} catch (IOException e) {
				e.printStackTrace();
			}

			if (BaseActivity.DEBUG)
				MyLog.e("pcm", "File length after processing (cut-off & fade in/out): " + pcm.length());

			System.out.println("Process audio stop");

			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			// DO NOTHING
		}

		protected void onPostExecute(Void result) {

			isProcessing = false;

			System.out.println("is processing false");
		}
	}

	private class CreateAudioSnap extends AsyncTask<Void, Integer, Void> {

		private String audiosnapPath;

		protected boolean inGallery;

		public CreateAudioSnap() {
			inGallery = true;
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (!origin.equals(SDCARD_AUDIOSNAP)) {

				while (isProcessing)
					;

				audiosnapPath = createAudioSnap(inGallery);

				// final Handler handler = new Handler() {
				// public void handleMessage(Message msg) {
				// final String result = (String) msg.obj;
				// try {
				// if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
				// if(BaseActivity.DEBUG) MyLog.d(TAG,
				// "---- Photo uploaded succesfully! ----");
				// } else {
				// if(BaseActivity.DEBUG) MyLog.d(TAG,
				// "---- Photo uploaded error! ----");
				// }
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// };
				// };

			} else {

				audiosnapPath = jpeg.getPath();
				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "---- UPLOADING picture: " + audiosnapPath.toString());

			}

			// TODO send audiosnap to server --> audiosnap is in path:
			// audiosnapPath

			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			// DO NOTHING
		}

		protected void onPostExecute(Void result) {
			done = true;

			// actualizar galeria
			if (inGallery)
				context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
		}

		private String createAudioSnap(boolean inGallery) {

			// System.out.println("Creating start");

			AACEncoder encoder = new AACEncoder();

			// TODO encode PCM to AAC/MPEG-2 --> .m4a

			// System.err.println("computed sample rate: 44100");

			// long start = System.currentTimeMillis();

			encoder.init(128000, 1, 44100, 16, aacFilePath);

			// long stop = System.currentTimeMillis();
			// System.out.println("init" + (stop - start) + "ms");

			try {

				// stop = System.currentTimeMillis();
				// System.out.println("read" + (stop - start) + "ms");

				byte[] bytes = new byte[samples.length * 2];
				ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(samples);

				// System.out.println("1:" + bytes.length);
				encoder.encode(bytes);

				// stop = System.currentTimeMillis();
				// System.out.println("encode" + (stop - start) + "ms");

				System.out.println("aac: " + aacFilePath);

				System.out.println("m4a: " + m4aFilePath);

				encoder.uninit();

				new AACToM4A().convert(context, aacFilePath, m4aFilePath);

				// stop = System.currentTimeMillis();
				// System.out.println("wrap" + (stop - start) + "ms");

			} catch (IOException e1) {
				e1.printStackTrace();
			}

			(new File(aacFilePath)).delete();

			// stop = System.currentTimeMillis();

			// System.out.println((stop - start) + "ms");
			// System.out.println("2:" + (new File(M4A_FILE)).length());

			// TODO insert .m4a into EXIF

			// set name of the audiosnap
			// audiosnapPath =
			// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
			// + getAlbum(new Date());

			String imageFilePath = getIamgeFilePath(inGallery);

			// save bitmap to file
			try {
				BitmapUtil.saveBitmap(bitmap, new File(imageFilePath));
			} catch (IOException e) {
				e.printStackTrace();
			}

			File m4aFile = new File(m4aFilePath);

			try {
				ExifUtil.mergeIntoAudioSnap(imageFilePath, m4aFile, /* dstWidth */bitmap.getWidth(), /* dstHeight */bitmap.getHeight());
			} catch (DecoderException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			m4aFile.delete();

			return imageFilePath;
		}
	}

	private String getIamgeFilePath(boolean inGallery) {

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = AS_FILE_PREFIX + timeStamp + "_" + AS_FILE_SUFFIX;

		if (prefs.getBoolean(BaseActivity.SAVE_IN_LIBRARY, true) || inGallery) {
			return new AudioSnapsFileCache().getAlbumDir().getAbsolutePath() + "/" + imageFileName;
		} else {
			return new AudioSnapsFileCache().getTmpDir().getAbsolutePath() + "/" + imageFileName;
		}
	}

	private class SendAudioSnap extends CreateAudioSnap {

		public SendAudioSnap() {
			inGallery = false;
		}

		@Override
		protected void onPostExecute(Void result) {

			done = true;

			// Envio de photo
			new UploadPhoto(context, null, LoggedUser.id, new File(super.audiosnapPath), LoggedUser.koeToken).execute();
			// finish();
		}

	}

	@Override
	public void finish() {
		if (pcm != null)
			pcm.delete();
		if (m4a != null)
			m4a.delete();

		// // TODO Ha acabado el upload de la foto?
		// if(!prefs.getBoolean(BaseActivity.SAVE_IN_LIBRARY, true)){
		// File file = new File(getIamgeFilePath());
		// file.delete();
		// }

		// System.out.println("delete pcm");
		if (origin.equals(APPLICATION)) {
			// System.out.println("delete jpeg");
			if (jpeg != null)
				jpeg.delete();
		}
		super.finish();
	}

	public String getAlbum(Date date) {
		return "/" + DateFormat.getDateTimeInstance().format(date).replaceAll("/", "_").replaceAll(":", "_").replaceAll(" ", "_") + ".jpeg";
	}

	private String handleImage(Intent intent) {

		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

		if (imageUri != null) {

			// redirect to correct activity

			String fileImage = getRealPathFromURI(imageUri);
			SoundAndShot ss = new SoundAndShot(new File(fileImage));

			if (ss.is()) {

				// Image is a Shot&Saund and audio has been extracted
				pcm = new File(ss.getAudioPath());
				jpeg = new File(ss.getPicturePath());

				return SDCARD_SOUND_SHOT;

			} else {

				// Check if image is an AudioSnap or a simple picture
				// TODO try to extract audio to pcm assuming is audiosnap

				audioSnapsFileCache = new AudioSnapsFileCache();

				try {
					m4a = audioSnapsFileCache.extraeAudio(new File(fileImage), "pcm-audiosnaps");
				} catch (UnsupportedEncodingException e) {
					finish();
				} catch (NoAudioException e) {
					isNotAudioSnap();
				} catch (DecoderException e) {
					finish();
				}

				jpeg = new File(fileImage);

				return SDCARD_AUDIOSNAP;

			}

		}

		return null;
	}

	private void isNotAudioSnap() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(context.getResources().getString(R.string.NO_AUDIO_TITLE));
		alertDialog.setMessage(context.getResources().getString(R.string.NO_AUDIO_MESSAGE));
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
	}

	private void isNotUploaded() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(context.getResources().getString(R.string.notUploadedTitle));
		alertDialog.setMessage(context.getResources().getString(R.string.notUploaded));
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
	}

	private String getRealPathFromURI(Uri contentUri) {

		if (contentUri.getScheme().contentEquals("content")) {

			String res = null;
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
			if (cursor.moveToFirst()) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				res = cursor.getString(column_index);
			}
			cursor.close();
			return res;
		}

		return contentUri.getPath();

	}

	@Override
	public void onBackPressed() {

		if (underVisible) {

			// Subir la pestaña
			underVisible = false;
			animaciones.translateUpDownLayout(BaseActivity.POSITION_TOP, over, OVER_REMAINING);

			// Esconder botones de la pestaña
			dismissBtn.setVisibility(View.VISIBLE);
			okBtn.setVisibility(View.VISIBLE);

			under.setVisibility(View.GONE);

		} else {
			underVisible = true;
			super.onBackPressed();
		}
	}

	// Get friends list
	@SuppressLint("HandlerLeak")
	private void initMentionedFriendsList() {

		// Check if list of friends is saved
		if (prefs.contains(BaseActivity.MY_FRIENDS_JSONARRAY)) {
			try {
				jsonArrayFriends = new JSONArray(prefs.getString(BaseActivity.MY_FRIENDS_JSONARRAY, ""));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mentionedFriendsListAdapter = new MentionedFriendsListAdapter(context, jsonArrayFriends);
			commentsTextView.setAdapter(mentionedFriendsListAdapter);
		} else {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- Request friends list for mentions ----");
			final Handler handler = new Handler() {

				public void handleMessage(Message msg) {
					final String result = (String) msg.obj;
					try {
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							// Save list of friends
							Editor editor = prefs.edit();
							editor.putString(BaseActivity.MY_FRIENDS_JSONARRAY, result);
							editor.commit();

							jsonArrayFriends = new JSONArray(result);
							mentionedFriendsListAdapter = new MentionedFriendsListAdapter(context, jsonArrayFriends);
							commentsTextView.setAdapter(mentionedFriendsListAdapter);

						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				};
			};

			GetCompleteFriendsList getFriendsList = new GetCompleteFriendsList(context, handler, LoggedUser.id, LoggedUser.id, true, false, false, LoggedUser.koeToken, false);
			getFriendsList.execute();
		}
	}

	// Twitter Login
	private void loginToTwitter() {
		// Launch twitter activity
		Intent intent = new Intent(this, TwitterLoginActivity.class);
		startActivityForResult(intent, BaseActivity.REQUEST_CODE_TWITTER_LOGIN);
	}

	// Twitter and Facebook Login
	@SuppressLint("HandlerLeak")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == BaseActivity.REQUEST_CODE_TWITTER_LOGIN) {

			if (resultCode == RESULT_OK) {

				String token = data.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN);
				String tokenSecret = data.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN_SECRET);

				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						final String result = (String) msg.obj;
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							SharedPreferences sharedPreferences = getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, true);
							editor.commit();

							btnTwitterSendPic.setSelected(true);

						}
					};
				};

				AddSocNetworkFromMobile addSocNetworkFromMobile = new AddSocNetworkFromMobile(this, handler, LoggedUser.id, "twitter", null, token, tokenSecret, LoggedUser.koeToken);
				addSocNetworkFromMobile.execute();

			}
		} else {
			mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
			super.onActivityResult(requestCode, resultCode, data);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindDrawables(findViewById(R.id.layout));
		System.gc();
	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	
	private Activity getActivity() {
		return this;
	}

}
