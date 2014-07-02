package com.audiosnaps;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import com.audiosnaps.log.MyLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.audiosnap.library.util.BitmapUtil;
import com.audiosnaps.classes.Animaciones;
import com.audiosnaps.classes.AudioPlayer;
import com.audiosnaps.classes.AudioSnapsFileCache;
import com.audiosnaps.classes.CoordsClass;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class FullScreenPictureActivity extends Activity {

	protected static final String logTag = "FullScreenPictureActivity";

	private static final int FADE_IN_DURATION = 300;
	private static final int FADE_OUT_DURATION = 300;
	private static final int WAIT_UNTIL_HIDE_CAPTION = 100;

	private static final int STAMP_MARGIN_DPS = 10;

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private ImageView btnStamp;
	private PhotoViewAttacher mAttacher;
	private Context context = this;

	private Animaciones animaciones;
	private ImageView imageViewAudioSnap;
	private TextView captionAppTxt;

	private AudioPlayer audioPlayer;

	protected int translationHeight;

	private Rect stampRect;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(BaseActivity.DEBUG) MyLog.e(logTag, "onCreate");

		if(BaseActivity.DEBUG) MyLog.e(logTag, "screen width: " + BaseActivity.screenWidth + ", height: " + BaseActivity.screenHeight);

		// Hide status-bar
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Hide title-bar, must be before setContentView
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// force portrait layout
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.activity_show);

		Intent intent = getIntent();

		animaciones = new Animaciones(this);

		final String picHash = intent.getStringExtra("picHash");
		final String captionApp = intent.getStringExtra("captionApp");

		imageViewAudioSnap = (ImageView) findViewById(R.id.imageViewAudioSnap);

		final File fileImage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + BaseActivity.CACHE_AUDIOSNAPS_FILES + picHash);

		final File fileAudio = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + BaseActivity.CACHE_AUDIOSNAPS_FILES + "audio" + picHash);

		btnStamp = (ImageView) findViewById(R.id.stamp);

		if (fileImage.exists()) {
			
			audioPlayer = new AudioPlayer(new Runnable() {
				@Override
				public void run() {
					// fade in stamp
					animaciones.fadeInStamp(btnStamp);
					btnStamp.setClickable(true);
				}
			});

			try {
				if (fileAudio.exists()) {
					audioPlayer.setFileSource(fileAudio);
				} else {
					audioPlayer.setFileSource(new AudioSnapsFileCache().extraeAudio(fileImage, picHash));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {				
				
				btnStamp.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						btnStamp.setClickable(false);
						// fade out stamp
						animaciones.fadeOutStamp(btnStamp);
						audioPlayer.play();
					}
				});

			}
		}
		
		if (captionApp != null) {

			captionAppTxt = (TextView) findViewById(R.id.lbContadorCaracteresCaption);
			captionAppTxt.setText(captionApp);

			if(BaseActivity.DEBUG) MyLog.e(logTag, captionApp);
		}

		btnStamp.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void onGlobalLayout() {

				stampRect = new Rect();

				if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

					btnStamp.getHitRect(stampRect);
					if(BaseActivity.DEBUG) MyLog.i(logTag, "primer left: " + stampRect.left + ", right: " + stampRect.right + ", top: " + stampRect.top + " , bottom: " + stampRect.bottom);

				}

				if (fileImage.exists()) {

					imageLoader.displayImage(Uri.fromFile(fileImage).toString(), imageViewAudioSnap, BaseActivity.optionsAudioSnapImage, new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String str, View view) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLoadingFailed(String str, View view, FailReason failReason) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLoadingComplete(String str, View arg1, Bitmap bitmap) {

							animaciones.fadeOut(captionAppTxt, FADE_OUT_DURATION, WAIT_UNTIL_HIDE_CAPTION);

							if(BaseActivity.DEBUG) MyLog.i(logTag, "width: " + btnStamp.getWidth() + ", " + " height:" + btnStamp.getHeight());

							final CoordsClass coords = new CoordsClass(btnStamp.getWidth(), btnStamp.getHeight(), BitmapUtil.getPixels(STAMP_MARGIN_DPS, context), BitmapUtil.getPixels(6, context));

							// set image height
							imageViewAudioSnap.getLayoutParams().height = imageViewAudioSnap.getWidth() * BaseActivity.screenHeight / BaseActivity.screenWidth;

							// Attach a PhotoViewAttacher, which takes care of
							// all of the zooming functionality.
							mAttacher = new PhotoViewAttacher(imageViewAudioSnap);

							mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {

								@Override
								public void onPhotoTap(View view, float x, float y) {

									if(BaseActivity.DEBUG) MyLog.i(logTag, "onPhotoTap");

									PointF touchPoint = coords.getTouchPoint(x, y, mAttacher.getDisplayRect());

									if(BaseActivity.DEBUG) MyLog.i(logTag, "x: " + touchPoint.x + ", y: " + touchPoint.y);
									if(BaseActivity.DEBUG) MyLog.i(logTag, "left: " + stampRect.left + ", right: " + stampRect.right + ", top: " + stampRect.top + " , bottom: " + stampRect.bottom);

									if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB && stampRect.contains((int) touchPoint.x, (int) touchPoint.y)) {

										if (audioPlayer != null) {
											// fade out stamp
											animaciones.fadeOutStamp(btnStamp);

											audioPlayer.play();
											if(BaseActivity.DEBUG) MyLog.i(logTag, "click dentro del stamp");
										}
										
									} else if (mAttacher.getScale() > 1.0f) {

										mAttacher.setScale(1.0f, x, y, true);
										if(BaseActivity.DEBUG) MyLog.i(logTag, "restart zoom");

									} else {

										// show caption
										if (captionAppTxt.getVisibility() != View.VISIBLE) {
											animaciones.fadeIn(captionAppTxt, FADE_IN_DURATION, 0);
											if(BaseActivity.DEBUG) MyLog.i(logTag, "fade in");
										} else {
											animaciones.fadeOut(captionAppTxt, FADE_OUT_DURATION, 0);
											if(BaseActivity.DEBUG) MyLog.i(logTag, "fade out");
										}
									}
								}

							});

							mAttacher.setOnMatrixChangeListener(new OnMatrixChangedListener() {

								boolean first = true;
								
								@Override
								public void onMatrixChanged(RectF rect) {

									if (captionAppTxt.getVisibility() == View.VISIBLE)
										animaciones.fadeOut(captionAppTxt, 0, 0);

									if (btnStamp != null) {

										PointF translationPoint = coords.getTranslationPoint(rect);

										ObjectAnimator.ofFloat(btnStamp, "translationX", translationPoint.x).setDuration(0).start();
										if(BaseActivity.DEBUG) MyLog.i(logTag, "translate x");

										ObjectAnimator.ofFloat(btnStamp, "translationY", translationPoint.y).setDuration(0).start();
										if(BaseActivity.DEBUG) MyLog.i(logTag, "translate y");

										if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
											stampRect.offsetTo((int) translationPoint.x + coords.getTranslationWidth() - btnStamp.getWidth(),
													(int) translationPoint.y + coords.getTranslationHeight() - btnStamp.getHeight());
										}

									}

									if(BaseActivity.DEBUG) MyLog.i(logTag, "onMatrixChanged");

									if (first){
										btnStamp.setVisibility(View.VISIBLE);
										first = false;
									}
								}
							});

						}

						@Override
						public void onLoadingCancelled(String str, View view) {
							// TODO Auto-generated method stub

						}
					});

				}

				ViewTreeObserver obs = btnStamp.getViewTreeObserver();

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}

		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(BaseActivity.DEBUG) MyLog.e(logTag, "onKeyDown");
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void finish() {

		if (audioPlayer != null) {
			audioPlayer.stop();
		}

		if(BaseActivity.DEBUG) MyLog.e(logTag, "finish");
		super.finish();
	}
	
	@Override
	public void onDestroy() {
		recycleBitmapFromImageView(imageViewAudioSnap);
		super.onDestroy();
	}
	
	private void recycleBitmapFromImageView(ImageView imageViewAudioSnap) {
		Drawable drawable = imageViewAudioSnap.getDrawable();
		if (drawable instanceof BitmapDrawable) {
		    ((BitmapDrawable) drawable).getBitmap().recycle();
		}
	}

}
