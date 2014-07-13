package com.audiosnaps;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

import com.audiosnaps.log.MyLog;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.audiosnap.library.AudioParams;
import com.audiosnap.library.CameraPreview;
import com.audiosnap.library.util.AudioUtil;
import com.audiosnaps.classes.AudioSnapsFileCache;
import com.audiosnaps.classes.Dialogos;
import com.audiosnaps.view.TouchSquare;
import com.google.analytics.tracking.android.EasyTracker;
import com.sonymobile.camera.addon.capturingmode.CapturingModeSelector;

public class TakeAudioSnapActivity extends Activity {

	private static final int TIME_FILL_BTN = 5100;

	// Margin up view in dps
	private static final int MARGIN_UP = 1;

	public final static String EXTRA_AUDIO_PATH = "com.audiosnaps.take.AUDIO";
	public final static String EXTRA_PICTURE_PATH = "com.audiosnaps.take.PICTURE";
	public final static String EXTRA_FORMAT = "com.audiosnaps.take.FORMAT";
	public final static String EXTRA_ORIENTATION = "com.audiosnaps.take.ORIENTATION";

	public final static String ACTION_TAKE = "take";

	private static final String logTag = "TakeAudioSnapActivity";

	private static boolean isMute;

	private AudioRecord audioRecord;

	private CameraPreview cameraPreview;
	private FrameLayout preview;
	private TouchSquare square;

	private RecordAudio recordTask;

	private String localFilesPath;

	private ImageView takePicBtn;

	private String pcmPath;
	private String jpegPath;

	private static boolean isRecording = false;

	private final Context context = this;

	private ImageView up;
	private ImageView down;

	private int viewHeight;
	private float viewAspectRatio = 1150 / 640;

	private ImageButton formatBtn;

	// Orientation
	private OrientationEventListener mOrientationEventListener;
	private int mOrientation = -1;

	private SharedPreferences prefs;

	private int camera, flash, format;
	
	/// begin - by anna
	private ImageButton mModeButton;
    private ImageButton mThumbnailButton;
    private CapturingModeSelector mCapturingModeSelector;
    
    private ImageButton flashBtn;
    private ImageButton flipBtn;
    private ImageButton cancelBtn;
    private ImageView takePicBtnBgFlow;
    private ImageView takePicBtnBg;
    
    private long mImageId = -1;
    private String mImageFileLocation = null;
    private long mImageDateTaken = 0;
    
    // Sony Experia vars
    // Value of pressed icon color filter.
    public static final int PRESSED_COLOR_FILTER = 0x66000000;
    // The following capturing mode name is registered to the Camera Add-on framework.
    // The name must match the same value as "name" attribute of "<mode>" tag of
    // "res/xml/mode.xml".
    public static final String MODE_NAME = "audiosnaps_camera_app";
	/// end - by anna

	/** Called when the activity is first created. */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Hide status-bar
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Hide title-bar, must be before setContentView
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// force portrait layout
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.photo);
		
		/// begin - by anna
		mModeButton = ((ImageButton)findViewById(R.id.mode_button));
		  
        if (BaseActivity.isXperiaDevice){
	        mModeButton.setImageResource(R.drawable.mode_icon);
	        mModeButton.setOnClickListener(new ModeSelectorButtonClickListener());
	        mModeButton.setOnTouchListener(new ModeSelectorButtonTouchListener());
        }else{
        	mModeButton.setVisibility(View.GONE);
        	mModeButton = null;
        }
        
        if (BaseActivity.isXperiaDevice) {

        	// Create a parent view for the capturing mode selector view.
        	ViewGroup modeSelectorContainer = (ViewGroup)findViewById(R.id.modeselector_container);

        	// Create a CapturingModeSelector
        	mCapturingModeSelector = new CapturingModeSelector(this, modeSelectorContainer);

        	// Set two listeners on the CapturingModeSelector
        	mCapturingModeSelector.setOnModeSelectListener(new MyOnModeSelectListener());
        	mCapturingModeSelector.setOnModeFinishListener(new MyOnModeFinishListener());
        }

        mThumbnailButton = ((ImageButton)findViewById(R.id.thumbnail_button));
        mThumbnailButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.putExtra(MainActivity.EXIT_CODE, MainActivity.TAKEAUDIOSNAP_THUMBNAIL);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				finish();
			}
		});
        if (!BaseActivity.isXperiaDevice) {
        	mThumbnailButton.setVisibility(View.INVISIBLE);
        } else {
        	setThumbnail();
        }
		/// end - by anna

		// Camera preferences
		prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		camera = prefs.getInt(BaseActivity.CAMERA, BaseActivity.BACK_CAMERA);
		flash = prefs.getInt(BaseActivity.FLASH, BaseActivity.FLASH_OFF);
		format = prefs.getInt(BaseActivity.PIC_FORMAT, BaseActivity.FORMAT_1_1);

		localFilesPath = new AudioSnapsFileCache().getTmpDir().getAbsolutePath();

		File dir = new File(localFilesPath);

		if (dir.exists()) {
			// Empty in case there are remaining files
			for (File f : dir.listFiles())
				f.delete();
		}

		cameraPreview = new CameraPreview(this, camera, flash);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		preview = ((FrameLayout) findViewById(R.id.preview));
		preview.addView(cameraPreview, params);

		AudioUtil.mute(context, true);
		isMute = true;

		takePicBtn = (ImageView) findViewById(R.id.takePicBtn);

		takePicBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				isRecording = false;

				AudioUtil.mute(context, false);

				isMute = false;

				// TODO lanza excepción y peta con Nexus, etc...
				takePicture(null, null, jpegCallback);
			}
		});

		cancelBtn = (ImageButton) findViewById(R.id.cancelBtn);

		cancelBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				(new File(jpegPath)).delete();
				(new File(pcmPath)).delete();
				// Save prefs
				saveCameraPrefs();
				finish();
			}
		});
		
		takePicBtnBg = (ImageView) findViewById(R.id.takePicBtnBg);

		takePicBtnBgFlow = (ImageView) findViewById(R.id.takePicBtnBgFlow);

		takePicBtnBgFlow.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void onGlobalLayout() {

				Animation animation = new TranslateAnimation(0, 0, 49, 0);
				animation.setDuration(TIME_FILL_BTN);
				animation.setFillAfter(true);
				takePicBtnBgFlow.startAnimation(animation);

				ViewTreeObserver obs = takePicBtnBgFlow.getViewTreeObserver();

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}

			}
		});

		File localFiles = new File(localFilesPath);
		if (!localFiles.exists())
			localFiles.mkdirs();
		try {
			jpegPath = File.createTempFile("tmp", ".jpeg", localFiles).getAbsolutePath();
			pcmPath = File.createTempFile("tmp", ".pcm", localFiles).getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't create file on SD card", e);
		}

		// if(BaseActivity.DEBUG) MyLog.d(LOG_TAG, "onCreate'd");

		up = (ImageView) findViewById(R.id.up);
		down = (ImageView) findViewById(R.id.down);

		up.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// gets called after layout has been done but before display
				// so we can get the height then hide the view

				int viewWidth = BaseActivity.screenWidth;
				viewHeight = (int) (viewWidth * viewAspectRatio);

				LayoutParams params = (LayoutParams) up.getLayoutParams();
				params.width = viewWidth;
				params.height = viewHeight;
				up.setLayoutParams(params);

				params = (LayoutParams) down.getLayoutParams();
				params.width = viewWidth;
				params.height = viewHeight;
				down.setLayoutParams(params);

				up.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				if (format == BaseActivity.FORMAT_4_3){
					resize(3, 4);
				}else{
					resize(1, 1);
				}
			}

		});

		formatBtn = (ImageButton) findViewById(R.id.formatBtn);

		updateFormatBtnBackground();

		formatBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {

				if (format == BaseActivity.FORMAT_4_3){
					resize(1, 1);
					format = BaseActivity.FORMAT_1_1;
				}else{
					resize(3, 4);
					format = BaseActivity.FORMAT_4_3;
				}

				updateFormatBtnBackground();

			}
		});

		flipBtn = (ImageButton) findViewById(R.id.flipBtn);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		// System.out.println("N. cameras: " + Camera.getNumberOfCameras());

		// if phone has only one camera, hide "switch camera" button
			if (Camera.getNumberOfCameras() > 1) {
				flipBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
	
						if(camera == BaseActivity.BACK_CAMERA)
							camera = BaseActivity.FRONT_CAMERA;
						else 
							camera = BaseActivity.BACK_CAMERA;
	
						cameraPreview.flip();
					}
				});
			} else {
				flipBtn.setVisibility(ImageButton.GONE);
			}
		}
		else
		{
			flipBtn.setVisibility(ImageButton.GONE);
		}

		flashBtn = (ImageButton) findViewById(R.id.flashBtn);

		updateFlashBtnBackground(flashBtn);

		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			flashBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					flash = (flash + 1) % 3;
					cameraPreview.flash(flash);
					updateFlashBtnBackground(flashBtn);
					if(BaseActivity.DEBUG) MyLog.i(logTag, "flash: " + flash);
				}
			});
		} else {
			flashBtn.setVisibility(ImageButton.GONE);
		}

		recordTask = new RecordAudio();
		recordTask.execute();
	}

	private void updateFormatBtnBackground() {
		if (format == BaseActivity.FORMAT_4_3){
			formatBtn.setBackgroundResource(R.drawable.bt_format_4_3);
		}else{
			formatBtn.setBackgroundResource(R.drawable.bt_format_1_1);
		}
	}

	private void updateFlashBtnBackground(final ImageButton flashBtn) {

		if (flash == BaseActivity.FLASH_ON)
			flashBtn.setBackgroundResource(R.drawable.bt_flash_on_2x);
		else if (flash == BaseActivity.FLASH_AUTO)
			flashBtn.setBackgroundResource(R.drawable.bt_flash_auto_2x);
		else
			flashBtn.setBackgroundResource(R.drawable.bt_flash_off_2x);

	}

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera mCamera) {

			FileOutputStream outStream = null;

			try {
				// write to local sandbox file system
				// outStream =
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
				// System.currentTimeMillis()), 0);
				// Or write to sdcard
				outStream = new FileOutputStream(jpegPath);
				outStream.write(data);
				outStream.close();
				// if(BaseActivity.DEBUG) MyLog.d(LOG_TAG, "onPictureTaken - wrote bytes: " +
				// data.length);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				// Save prefs
				saveCameraPrefs();

				Intent intent = new Intent(context, SendAudioSnapActivity.class);
				intent.putExtra(EXTRA_AUDIO_PATH, pcmPath);
				intent.putExtra(EXTRA_PICTURE_PATH, jpegPath);
				intent.putExtra(EXTRA_FORMAT, format);
				intent.putExtra(EXTRA_ORIENTATION, mOrientation);
				intent.setAction(ACTION_TAKE);
				startActivity(intent);
				
				/// begin - by anna
				if ( BaseActivity.isXperiaDevice ) {
					Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
					mThumbnailButton.setImageBitmap(bmp);
				}
				/// end - by anna
			}

		}

	};

	private void saveCameraPrefs() {
		Editor editor = prefs.edit();
		editor.putInt(BaseActivity.FLASH, flash);
		editor.putInt(BaseActivity.CAMERA, camera);
		editor.putInt(BaseActivity.PIC_FORMAT, format);
		editor.commit();
	}

	private class RecordAudio extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			isRecording = true;

			File file = new File(pcmPath);
			try {
				DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
				int bufferSize = AudioRecord.getMinBufferSize(AudioParams.frequency, AudioFormat.CHANNEL_IN_MONO, AudioParams.audioEncoding);

				audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AudioParams.frequency, AudioFormat.CHANNEL_IN_MONO, AudioParams.audioEncoding, bufferSize);

				short[] buffer = new short[bufferSize];
				audioRecord.startRecording();
				while (isRecording) {
					int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
					for (int i = 0; i < bufferReadResult; i++) {
						dos.writeShort(buffer[i]);
					}
				}
				audioRecord.stop();
				audioRecord.release();
				dos.close();

			} catch (Throwable t) {
				if(BaseActivity.DEBUG) MyLog.e("AudioRecord", "Recording Failed: " + t.getLocalizedMessage());
			}

			return null;

		}

		protected void onProgressUpdate(Integer... progress) {
			// DO NOTHING
		}

		protected void onPostExecute(Void result) {
			// DO NOTHING
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (isMute)
			AudioUtil.mute(context, false);
		isRecording = false;
		EasyTracker.getInstance().activityStop(this);
		finish();
	}

	private void resize(int x, int y) {

		int height = (BaseActivity.screenHeight - BaseActivity.screenWidth * y / x) / 2;

		LayoutParams params = (LayoutParams) up.getLayoutParams();
		if (height > 0) {
			params.topMargin = -(viewHeight - height);
			if(BaseActivity.DEBUG) MyLog.e(logTag, "top margin: " + params.topMargin + ", up: " + viewHeight + ", height: " + height);
		}

		up.setLayoutParams(params);
		up.requestLayout();

		params = (LayoutParams) down.getLayoutParams();
		if (height > 0) {
			params.bottomMargin = -(viewHeight - height);
			if(BaseActivity.DEBUG) MyLog.e(logTag, "bottom margin: " + params.bottomMargin + ", up: " + viewHeight + ", height: " + height);
		}

		down.setLayoutParams(params);
		down.requestLayout();
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

	@Override
	protected void onResume() {
		super.onResume();
		
		if (mOrientationEventListener == null) {
			mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {

				@Override
				public void onOrientationChanged(int orientation) {

					if (orientation >= 315 || orientation < 45) {
						if (mOrientation != BaseActivity.ORIENTATION_PORTRAIT_NORMAL) {
							mOrientation = BaseActivity.ORIENTATION_PORTRAIT_NORMAL;
						}
					} else if (orientation < 315 && orientation >= 225) {
						if (mOrientation != BaseActivity.ORIENTATION_LANDSCAPE_NORMAL) {
							mOrientation = BaseActivity.ORIENTATION_LANDSCAPE_NORMAL;
						}
					} else if (orientation < 225 && orientation >= 135) {
						if (mOrientation != BaseActivity.ORIENTATION_PORTRAIT_INVERTED) {
							mOrientation = BaseActivity.ORIENTATION_PORTRAIT_INVERTED;
						}
					} else { // orientation <135 && orientation > 45
						if (mOrientation != BaseActivity.ORIENTATION_LANDSCAPE_INVERTED) {
							mOrientation = BaseActivity.ORIENTATION_LANDSCAPE_INVERTED;
						}
					}
				}
			};
		}
		if (mOrientationEventListener.canDetectOrientation()) {
			mOrientationEventListener.enable();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		mOrientationEventListener.disable();
		
		if (mCapturingModeSelector != null) {
            mCapturingModeSelector.release();
            mCapturingModeSelector = null;
        }
	}

	public void setCameraFocus(AutoFocusCallback autoFocus) {
		if (cameraPreview.mCamera.getParameters().getFocusMode().equals(cameraPreview.mCamera.getParameters().FOCUS_MODE_AUTO)
				|| cameraPreview.mCamera.getParameters().getFocusMode().equals(cameraPreview.mCamera.getParameters().FOCUS_MODE_MACRO)) {
			cameraPreview.mCamera.autoFocus(autoFocus);
		}
	}

	public void takePicture(final ShutterCallback shutter, final PictureCallback raw, final PictureCallback jpeg) {

		if (cameraPreview.mCamera.getParameters().getFocusMode().equals(Parameters.FOCUS_MODE_AUTO)) {

			cameraPreview.mCamera.autoFocus(new AutoFocusCallback() {

				boolean once = true;

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					if (once) {
						cameraPreview.mCamera.takePicture(shutter, raw, jpeg);
						once = false;
					}
				}
			});

		} else {

			cameraPreview.mCamera.takePicture(shutter, raw, jpeg);

		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (cameraPreview.mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
			cameraPreview.mCamera.autoFocus(null);
			preview.removeView(square);
			square = new TouchSquare(this, new PointF(event.getX(), event.getY()));
			preview.addView(square);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (mCapturingModeSelector != null && mCapturingModeSelector.isOpened()) {
            mCapturingModeSelector.close();
            setViewsVisibility(View.VISIBLE);
        } else {
        	saveCameraPrefs();
            super.onBackPressed();
        }
	}

	/// begin - by anna
	private void showPopupWithMessage(String msg) {
        Dialogos dialog = new Dialogos(this);
        AlertDialog alertDialog = dialog.okAlertDialog(msg);
        alertDialog.show();
    }
	
	/**
     * Implementation of CapturingModeSelector.OnModeSelectListener.
     */
    private class MyOnModeSelectListener implements CapturingModeSelector.OnModeSelectListener {
        /**
         * onModeSelect(String modeName) is called when the current mode and the next mode are in
         * <UL>
         * <LI> the same package and the same activity.</LI>
         * </UL>
         * The next mode is specified by an argument, modeName. The modeName is
         * the name of the mode that has been selected by the user.
         */
        @Override
        public void onModeSelect(String modeName) {
            mCapturingModeSelector.close();
            setViewsVisibility(View.VISIBLE);
            setThumbnail();

            /// begin - by anna
            
            if (mImageDateTaken == 0) {
            	return;
            }
            
//            Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), mImageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
            Bitmap bmp = null;
			try {
				bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
	            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
	            byte[] data = stream.toByteArray();

	            
	            FileOutputStream outStream = null;

				try {
					// write to local sandbox file system
					// outStream =
					// CameraDemo.this.openFileOutput(String.format("%d.jpg",
					// System.currentTimeMillis()), 0);
					// Or write to sdcard
					outStream = new FileOutputStream(jpegPath);
					outStream.write(data);
					outStream.close();
					// if(BaseActivity.DEBUG) MyLog.d(LOG_TAG, "onPictureTaken - wrote bytes: " +
					// data.length);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {

					// Save prefs
					saveCameraPrefs();

					Intent intent = new Intent(context, SendAudioSnapActivity.class);
					intent.putExtra(EXTRA_AUDIO_PATH, pcmPath);
					intent.putExtra(EXTRA_PICTURE_PATH, jpegPath);
					intent.putExtra(EXTRA_FORMAT, format);
					intent.putExtra(EXTRA_ORIENTATION, mOrientation);
					intent.setAction(ACTION_TAKE);
					startActivity(intent);
				}
			}
            /// end - by anna
        }
    }

    /**
     * Implementation of CapturingModeSelector.OnModeFinishListener.
     */
    private class MyOnModeFinishListener implements CapturingModeSelector.OnModeFinishListener {
        /**
         * onModeFinish() is called when current mode and the next mode are in
         * <UL>
         * <LI> same package and different activity.</LI>
         * <LI> different package and same activity.</LI>
         * <LI> different package and different activity.</LI>
         * </UL>
         * In other words, this is called when an activity of the current mode needs to finish.
         */
        @Override
        public void onModeFinish() {
        	if (mCapturingModeSelector != null) {
                mCapturingModeSelector.close();
            }
            finish();
        }
    }
    
    /**
     * This is a convenience method to get the latest captured image from the
     * MediaStore
     */
    private void getLatestImage() {
        MyLog.v(logTag, "getLatestImage");
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{BaseColumns._ID, MediaColumns.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN};
        String order = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";
        String selection =  MediaColumns.DATA + " LIKE '%" + BaseActivity.STORAGE_PATH_PREFIX + "%" + BaseActivity.AS_FILE_SUFFIX + "'";

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(imageUri, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
                mImageId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                mImageFileLocation = cursor.getString(cursor.getColumnIndex(MediaColumns.DATA));
                mImageDateTaken = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.
                        ImageColumns.DATE_TAKEN));
                MyLog.v(logTag, "getLatestImage, mImageId = " + mImageId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    /**
     * According to the UI guidelines, the mode selector button should apply a color filter
     * to display a pressed state when pressed.
     */
    private class ModeSelectorButtonTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mModeButton.onTouchEvent(event);
            if (mModeButton.isPressed()) {
                mModeButton.setColorFilter(PRESSED_COLOR_FILTER);
            } else {
                mModeButton.clearColorFilter();
            }
            return true;
        }
    }

    /**
     * When a user opens the CapturingModeSelector by tapping mode selector button in the UI,
     * pass the current mode name to the camera add-on framework.
     * The camera add-on framework will highlight the icon of the mode whose name was passed.
     */
    private class ModeSelectorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
        	
            if (mCapturingModeSelector != null) {
                // If the view tapped is the CapturingModeSelector button, hide the
                // mode name text view, buttons, open the CapturingModeSelector and
                // remove the color filter
                mCapturingModeSelector.open(MODE_NAME);
                setViewsVisibility(View.INVISIBLE);
                mModeButton.clearColorFilter();
            }
        }
    }
    
    /**
     * Sets the visibility of the UI elements
     */
    private void setViewsVisibility(int visibility) {
    	if (BaseActivity.isXperiaDevice && this.mModeButton != null)
    		mModeButton.setVisibility(visibility);
    	flashBtn.setVisibility(visibility);
        mThumbnailButton.setVisibility(visibility);
        flipBtn.setVisibility(visibility);
        formatBtn.setVisibility(visibility);
        cancelBtn.setVisibility(visibility);
        takePicBtn.setVisibility(visibility);
        takePicBtnBg.setVisibility(visibility);
        takePicBtnBgFlow.setVisibility(visibility);
    }

    /**
     * Creates a cursor containing media items,
     * extracts the last one captured and displays it in a thumbnail view.
     * If there is no photo available, present a container image.
     */
    private void setThumbnail() {
        // Reset last values
        mImageDateTaken = 0;

        // Latest media thumbnail requirement
        getLatestImage();
        
        // No image available on device
        if (mImageDateTaken != 0) {
            mThumbnailButton.setVisibility(View.VISIBLE);
            Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), mImageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
            MyLog.v(logTag, "setThumbnail(), display thumbnail");
            mThumbnailButton.setImageBitmap(bmp);
        }
    }
	/// end - by anna
}