package com.audiosnaps.classes;

import android.R.interpolator;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;

import com.audiosnap.library.util.BitmapUtil;
import com.audiosnaps.log.MyLog;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class Animaciones {

	private static String TAG = "Animaciones";
	private Context context;
	private final static int SWIPE_UP_PADDING = 45;

	public Animaciones(Context context) {
		this.context = context;
	}

	public Animaciones() {
	}

	// Fade in stamp
	public void fadeInStamp(final ImageView stamp) {
		Animation animFadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
		stamp.setAnimation(animFadeIn);
		stamp.post(new Runnable() {
		    public void run() {
		    	stamp.setVisibility(ImageView.VISIBLE);
		    }
		});
	}

	// Fade out stamp
	public void fadeOutStamp(final ImageView stamp) {
		Animation animFadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
		stamp.setAnimation(animFadeOut);
		// hide stamp icon
		stamp.post(new Runnable() {
		    public void run() {
		    	stamp.setVisibility(ImageView.INVISIBLE);
		    }
		});
	}

	// Animation feedPhoto
	public void translateUpDownLayout(int position, final View view, float percent) {
		switch (position) {
		case BaseActivity.POSITION_TOP:
			ObjectAnimator.ofFloat(view, "translationY", 0).start();
			break;
		case BaseActivity.POSITION_CENTER:
			ObjectAnimator.ofFloat(view, "translationY", -BaseActivity.screenHeight * (1 - percent)).start();
			break;
		}
	}

	// RETORNA PULGADAS PANTALLA DISPOSITIVO
	public double calculaPulgadasPantalla(DisplayMetrics displayMetrics) {
		int widthInPixels = displayMetrics.widthPixels;
		int heightInPixels = displayMetrics.heightPixels;
		float widthDpi = displayMetrics.xdpi;
		float heightDpi = displayMetrics.ydpi;
		float widthInches = widthInPixels / widthDpi;
		float heightInches = heightInPixels / heightDpi;
		double diagonalInches = Math.sqrt((widthInches * widthInches) + (heightInches * heightInches));

		if(BaseActivity.DEBUG) MyLog.d(TAG, "Punlgadas pantalla: " + diagonalInches);

		return diagonalInches;
	}

	// CARGA CUALQUIER ANIMACIÓN SOBRE OBJETO VIEW
	public void cargarIniciarAnimacion(int animacion, View view) {
		Animation animation = AnimationUtils.loadAnimation(context, animacion);
		view.startAnimation(animation);
	}

	// ESCALADO DE OBJETOS VIEWS
	public void viewScaleView(View view, double factorEscalado) {
		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		layoutParams.width = (int) ((view.getWidth() * factorEscalado));
		layoutParams.height = (int) ((view.getHeight() * factorEscalado));
		view.setLayoutParams(layoutParams);
	}

	// ESCALADO DE OBJETOS VIEWS EN PX
	public void scaleViewPx(View view, int ancho, int alto) {
		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		if (ancho != 0) {
			layoutParams.width = ancho;
		}
		if (alto != 0) {
			layoutParams.height = alto;
		}
		view.setLayoutParams(layoutParams);
	}

	// Animation feedPhoto
	public void translatePhotoLayout(int position, int swipeUpPadding, final View viewPhoto, final View viewHeader, final View viewUserInfo, final View viewComments, final View userNotifications, boolean moveUp, boolean durationZero, AnimatorListener listener) {

		switch (position) {
			case BaseActivity.POSITION_TOP:
				if (!moveUp) {
					ObjectAnimator oa = ObjectAnimator.ofFloat(viewPhoto, "translationY", 0);
					if(durationZero) oa.setDuration(0);
					oa.addListener(listener);
					oa.start();
	
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							if(viewComments!=null)
								viewComments.setVisibility(View.VISIBLE);
							viewUserInfo.setVisibility(View.VISIBLE);
						}
					}, durationZero ? 0 : 300);
				}
				break;
			case BaseActivity.POSITION_CENTER:
				if (moveUp) {
					int vcHeight=0;
					if(viewComments!=null)
						vcHeight=viewComments.getHeight();
					ObjectAnimator oa = ObjectAnimator.ofFloat(viewPhoto, "translationY", -vcHeight + swipeUpPadding);
					if(durationZero) oa.setDuration(0);
					oa.addListener(listener);
					oa.start();
					if(viewComments!=null)
						viewComments.setVisibility(View.VISIBLE);
					viewUserInfo.setVisibility(View.INVISIBLE);
	
				} else {
					if (userNotifications != null) {
						ObjectAnimator oa = ObjectAnimator.ofFloat(viewPhoto, "translationY", +viewUserInfo.getHeight() - viewUserInfo.getPaddingBottom() - BitmapUtil.getPixels(2, context));
						if(durationZero) oa.setDuration(0);
						oa.start();
					} else {
						ObjectAnimator oa = ObjectAnimator.ofFloat(viewPhoto, "translationY", +viewUserInfo.getHeight());
						if(durationZero) oa.setDuration(0);
						oa.start();
					}
					ObjectAnimator oa = ObjectAnimator.ofFloat(viewHeader, "translationY", -viewHeader.getHeight());
					oa.addListener(listener);
					if(durationZero) oa.setDuration(0);
					oa.start();
					if(viewComments!=null)
						viewComments.setVisibility(View.INVISIBLE);
					viewUserInfo.setVisibility(View.VISIBLE);
	
				}
				break;
			case BaseActivity.POSITION_BOTTOM:
				if (moveUp) {
					ObjectAnimator.ofFloat(viewPhoto, "translationY", 0).start();
					ObjectAnimator oa = ObjectAnimator.ofFloat(viewHeader, "translationY", 0);
					if(durationZero) oa.setDuration(0);
					oa.addListener(listener);
					oa.start();
	
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							//viewComments.setVisibility(View.VISIBLE);
							viewUserInfo.setVisibility(View.VISIBLE);
						}
					}, durationZero ? 0 : 300);
	
				}
				break;
		}
	}

	// Translate animation
	public void translatePhotoOut(float fromPhotoX, float fromPhotoY, float toPhotoY, float fromHeaderX, float fromHeaderY, float toHeaderY, int duration, View viewPhoto, View viewHeader) {

		// Photo animation
		TranslateAnimation translateAnimation = new TranslateAnimation(fromPhotoX, fromPhotoX, fromPhotoY, toPhotoY);
		translateAnimation.setDuration(duration);
		translateAnimation.setInterpolator(context, interpolator.linear);
		translateAnimation.setFillAfter(true);

		// Header animation
		TranslateAnimation translateAnimationHeader = new TranslateAnimation(fromHeaderX, fromHeaderX, fromHeaderY, -toHeaderY);
		translateAnimationHeader.setDuration(duration);
		translateAnimationHeader.setInterpolator(context, interpolator.linear);
		translateAnimationHeader.setFillAfter(true);
		viewHeader.startAnimation(translateAnimationHeader);

		viewPhoto.startAnimation(translateAnimation);
		viewHeader.startAnimation(translateAnimationHeader);

		translateAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Animation start");
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Animation end");
			}
		});
	}

	public void fadeIn(View view, long duration, long offset) {
		Animation animFadeIn = AnimationUtils.loadAnimation(context.getApplicationContext(), android.R.anim.fade_in);
		animFadeIn.setDuration(duration);
		animFadeIn.setStartOffset(offset);
		view.startAnimation(animFadeIn);
		view.setVisibility(View.VISIBLE);
	}

	public void fadeOut(View view, long duration, long offset) {
		Animation animFadeOut = AnimationUtils.loadAnimation(context.getApplicationContext(), android.R.anim.fade_out);
		animFadeOut.setDuration(duration);
		animFadeOut.setStartOffset(offset);
		view.startAnimation(animFadeOut);
		view.setVisibility(View.GONE);
	}

	public void semiFadeIn(View view, long duration, long offset) {
		Animation animFadeIn = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.semi_fade_in);
		animFadeIn.setDuration(duration);
		animFadeIn.setStartOffset(offset);
		view.startAnimation(animFadeIn);
		ViewHelper.setAlpha(view, 1.0f);
	}

	public void semiFadeOut(View view, long duration, long offset) {
		Animation animFadeOut = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.semi_fade_out);
		animFadeOut.setDuration(duration);
		animFadeOut.setStartOffset(offset);
		view.startAnimation(animFadeOut);
		ViewHelper.setAlpha(view, 0.5f);
	}

	// public void translate(View view, float fromX, float toX, float fromY,
	// float toY, long duration, long offset){
	// TranslateAnimation translateAnimation = new TranslateAnimation(fromX,
	// toX, fromY, toY);
	// translateAnimation.setDuration(duration);
	// translateAnimation.setStartOffset(offset);
	// translateAnimation.setFillAfter(true);
	// view.startAnimation(translateAnimation);
	// view.setVisibility(View.VISIBLE);
	// }

}
