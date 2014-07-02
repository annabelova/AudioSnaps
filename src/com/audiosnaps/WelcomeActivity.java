package com.audiosnaps;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.audiosnap.library.util.ViewUtil;
import com.audiosnaps.activities.UserFeedActivity;
import com.audiosnaps.classes.Animaciones;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.login.LoginRegisterActivity;
import com.audiosnaps.login.LoginRegisterUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class WelcomeActivity extends Activity {

	protected static final String TAG = "WelcomeActivity";

	private static final float PANEL_HEIGHT_PERC = 0.104f;
	private static final float STAMP_HEIGHT_PERC = 0.074f;
	private static final float FLECO_HEIGHT_PERC = 0.015f;
	private static final float STAMP_MARGIN_PERC = 0.048f;
	private static final float BUTTON_WIDTH_PERC = 0.428f;
	private static final float BUTTON_HEIGHT_PERC = 0.063f;
	private static final float BUTTON_Y_MARGIN_PERC = 0.018f;
	private static final float BUTTON_X_MARGIN_PERC = 0.038f;

	public static final int FINISH = 10;

	private Animaciones animaciones;
	private ImageView foto1, textTop1, textBottom1, textTop2, textBottom2, stamp, fleco;
	private LinearLayout panel;
	private RelativeLayout textTop, textBottom;
	private Button loginBtn, registerBtn;
	private boolean isFirstSequence;

	protected ImageLoader imageLoader = ImageLoader.getInstance();

	private DisplayImageOptions displayImageOptions;

	private String picHash;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		displayImageOptions = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).build();

		Uri uri = getIntent().getData();
		if (uri != null) picHash = getPicHash(uri);
		// Log.v(TAG, "pic hash: " + picHash);
		
		// Comprobamos si estamos logueados
		LoginRegisterUtil loginRegisterUtil = new LoginRegisterUtil(this);
		if (loginRegisterUtil.isLogged(this)) {
//			if(picHash != null){
//				Intent intent = new Intent(this, UserFeedActivity.class);
//				intent.putExtra(HttpConnections.USER_TARGET_ID, LoggedUser.id);
//				intent.putExtra(HttpConnections.PIC_HASH, picHash);
//				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.ONE_PICTURE_FEED);
//				finish();
//				startActivity(intent);
//				return;
//			}else{
				Intent intent = new Intent(this, MainActivity.class);
				finish();
				startActivity(intent);
				return;
//			}
		}

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.welcome);

		foto1 = (ImageView) findViewById(R.id.foto_1);
		panel = (LinearLayout) findViewById(R.id.panel);
		textTop1 = (ImageView) findViewById(R.id.text_top_1);
		textBottom1 = (ImageView) findViewById(R.id.text_bottom_1);
		textTop2 = (ImageView) findViewById(R.id.text_top_2);
		textBottom2 = (ImageView) findViewById(R.id.text_bottom_2);
		textTop = (RelativeLayout) findViewById(R.id.text_top);
		textBottom = (RelativeLayout) findViewById(R.id.text_bottom);
		fleco = (ImageView) findViewById(R.id.fleco);
		stamp = (ImageView) findViewById(R.id.stamp_intro);

		loginBtn = (Button) findViewById(R.id.loginBtn);
		registerBtn = (Button) findViewById(R.id.registerBtn);

		stamp.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				stamp.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						PlayAudio playTask = new PlayAudio();
						stamp.setClickable(false);
						animaciones.semiFadeOut(stamp, 300, 0);
						// Play audio
						playTask.execute();
					}
				});
			}
		});

		loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchRegisterLogin(false);
			}
		});

		registerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchRegisterLogin(true);
			}
		});

		initSizes();

		animaciones = new Animaciones(this);

		playFirstSequence();

	}

	private void launchRegisterLogin(boolean register) {
		Intent intent = new Intent();
		intent.setClass(WelcomeActivity.this, LoginRegisterActivity.class);
		intent.putExtra("register", register);
		if(picHash != null) intent.putExtra(HttpConnections.PIC_HASH, picHash);
		startActivityForResult(intent, FINISH);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	private String getPicHash(Uri uri) {
		Pattern p = Pattern.compile("http://audiosnaps.com/k/([\\w]{4})/");
		Matcher m = p.matcher(uri.toString());
		if (m.find()) return m.group(1);
		else return null;
	}

	private void initSizes() {

		ViewUtil util = new ViewUtil(this);

		util.resize(panel, 0, PANEL_HEIGHT_PERC);
		util.resize(fleco, 1.0f, FLECO_HEIGHT_PERC);
		util.resize(stamp, 0, STAMP_HEIGHT_PERC);
		stamp.getLayoutParams().width = stamp.getLayoutParams().height;
		util.setRelativeLayoutMargins(stamp, 0, 0, STAMP_MARGIN_PERC, 0);
		util.resize(loginBtn, BUTTON_WIDTH_PERC, BUTTON_HEIGHT_PERC);
		util.setLinearLayoutMargins(loginBtn, BUTTON_X_MARGIN_PERC, BUTTON_Y_MARGIN_PERC, BUTTON_X_MARGIN_PERC, BUTTON_Y_MARGIN_PERC);
		util.resize(registerBtn, BUTTON_WIDTH_PERC, BUTTON_HEIGHT_PERC);
		util.setLinearLayoutMargins(registerBtn, BUTTON_X_MARGIN_PERC, BUTTON_Y_MARGIN_PERC, BUTTON_X_MARGIN_PERC, BUTTON_Y_MARGIN_PERC);

	}

	private void playFirstSequence() {

		isFirstSequence = true;

		// fade in back pic
		animaciones.fadeIn(foto1, 1000, 0);

		if (!this.getIntent().getBooleanExtra("logout", false)) {

			// hide bottom panel
			hidePanel(500, 500);

			// show top and bottom images
			animaciones.fadeIn(textTop, 500, 1000);
			animaciones.fadeIn(textBottom, 500, 2000);

			// hide stamp and show
			ViewHelper.setTranslationY(stamp, stamp.getLayoutParams().height);
			stamp.setVisibility(ImageView.VISIBLE);
			ObjectAnimator animator = ObjectAnimator.ofFloat(stamp, "translationY", 0).setDuration(300);
			animator.setStartDelay(2200);
			animator.start();

		} else {

			// buttons must be visible
			loginBtn.setVisibility(Button.VISIBLE);
			registerBtn.setVisibility(Button.VISIBLE);

			// hide bottom panel and show
			hidePanel(0, 0);
			showPanel(500, 500);

			// show top and bottom images
			animaciones.fadeIn(textTop, 500, 1000);
			animaciones.fadeIn(textBottom, 500, 2000);

			// hide and show stamp
			ViewHelper.setTranslationY(textBottom, -(panel.getLayoutParams().height - fleco.getLayoutParams().height));
			showStamp(2200, 300);

		}
	}

	private void hidePanel(int delay, int duration) {

		ObjectAnimator animator = ObjectAnimator.ofFloat(panel, "translationY", panel.getLayoutParams().height - fleco.getLayoutParams().height);
		animator.setDuration(delay);
		animator.setStartDelay(duration);
		animator.start();

	}

	private void playSecondSequence() {

		isFirstSequence = false;

		animaciones.fadeOut(stamp, 100, 0);

		if (!this.getIntent().getBooleanExtra("logout", false)) {

			loginBtn.setVisibility(Button.VISIBLE);
			registerBtn.setVisibility(Button.VISIBLE);
			showPanel(0, 500);

		} else {

			ViewHelper.setTranslationY(stamp, stamp.getLayoutParams().height);
			ViewHelper.setTranslationY(textBottom, 0);

		}

		animaciones.fadeOut(foto1, 500, 500);
		animaciones.fadeOut(textTop, 500, 500);
		animaciones.fadeOut(textBottom, 500, 500);

		textTop1.setVisibility(View.INVISIBLE);
		textBottom1.setVisibility(View.INVISIBLE);
		textTop2.setVisibility(View.VISIBLE);
		textBottom2.setVisibility(View.VISIBLE);

		animaciones.fadeIn(textTop, 500, 1000);
		animaciones.fadeIn(textBottom, 500, 2000);

		showStamp(2200, 300);

	}

	private void showStamp(int delay, int duration) {

		ObjectAnimator animator;
		ViewHelper.setTranslationY(stamp, stamp.getLayoutParams().height);
		animator = ObjectAnimator.ofFloat(stamp, "translationY", 0).setDuration(duration);
		animator.setStartDelay(delay);
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animator arg0) {

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animator arg0) {
				stamp.setVisibility(ImageView.VISIBLE);
				ViewHelper.setAlpha(stamp, 1.0f);
			}
		});

		animator.start();
	}

	private void showPanel(int delay, int duration) {

		ObjectAnimator animator;
		animator = ObjectAnimator.ofFloat(panel, "translationY", 0).setDuration(duration);
		animator.setStartDelay(delay);
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator anim) {
				// TODO
			}

			@Override
			public void onAnimationEnd(Animator anim) {
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) stamp.getLayoutParams();
				params.bottomMargin = +panel.getLayoutParams().height - fleco.getLayoutParams().height;
				stamp.setLayoutParams(params);
			}

			@Override
			public void onAnimationRepeat(Animator anim) {
				// TODO
			}

			@Override
			public void onAnimationStart(Animator anim) {
				// TODO
			}
		});
		animator.start();
	}

	private class PlayAudio extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... params) {

			int resource;

			if (isFirstSequence)
				resource = R.raw.crits;
			else
				resource = R.raw.mar;

			final MediaPlayer mPlayer = MediaPlayer.create(WelcomeActivity.this, resource);
			mPlayer.start();

			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {

					mPlayer.release();

					if (isFirstSequence) {
						playSecondSequence();
					} else {
						animaciones.semiFadeIn(stamp, 300, 0);
						stamp.setClickable(true);
					}
				}
			});
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			// DO NOTHING
		}

		protected void onPostExecute(Void result) {
			// DO NOTHING
		}
	}

	// Facebook login
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == FINISH)
			finish();
		super.onActivityResult(requestCode, resultCode, data);
	}

}
