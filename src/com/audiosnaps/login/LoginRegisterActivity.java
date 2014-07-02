package com.audiosnaps.login;

import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.WelcomeActivity;
import com.audiosnaps.facebook.FacebookManager;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.twitter.TwitterLoginActivity;
import com.facebook.Session;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.sromku.simple.fb.SimpleFacebook;

@SuppressLint("HandlerLeak")
public class LoginRegisterActivity extends Activity {

	protected static final String logTag = "LoginRegiterActivity";

	private Button twBtn, mailBtn;
	private Button authBtn;
	private TextView termsOfUse;
	private String kKPTermsURL = "http://audiosnaps.com/static/terms/";
	private boolean webViewOn;
	private LinearLayout termsOfUseWeb;
	private DisplayMetrics metrics;
	private LoginRegisterUtil loginRegisterUtil;

	private SimpleFacebook mSimpleFacebook;

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Dialogos dialogo = new Dialogos(this);
		// dialogo.okAlertDialog("abriendo loginregisteractivity ...").show();

		Map<String, ?> keys = getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE).getAll();

		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
		}

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		final boolean register = this.getIntent().getBooleanExtra("register", true);

		if (register)
			setContentView(R.layout.register);
		else
			setContentView(R.layout.login);

		loginRegisterUtil = new LoginRegisterUtil(this);

		try {
			Session session = Session.getActiveSession();
			if (session != null)
				session.closeAndClearTokenInformation();
		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(logTag, "Exception, no facebook sesion active");
		}

		twBtn = (Button) findViewById(R.id.bt_login_tw);
		twBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (register) {
					loginToTwitter();
				} else {
					loginToTwitter();
				}
			}

		});

		mailBtn = (Button) findViewById(R.id.bt_login_mail);
		mailBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (register) {
					launchMailRegister();
				} else {
					launchMailLogin();
				}
			}
		});

		authBtn = (Button) findViewById(R.id.authButton);
		authBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FacebookManager.askForReadWritePermissionsAndDoLoginRegister(getActivity(), mSimpleFacebook);
			}
		});

		// Terms of Use
		termsOfUse = (TextView) findViewById(R.id.termsOfUse);
		termsOfUseWeb = (LinearLayout) findViewById(R.id.termsOfUseWeb);

		metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		ViewHelper.setTranslationY(termsOfUseWeb, metrics.heightPixels);

		SpannableStringBuilder strBuilder = new SpannableStringBuilder(Html.fromHtml(getString(R.string.TERMS_AND_CONDITIONS)));
		UnderlineSpan[] underlines = strBuilder.getSpans(0, strBuilder.length(), UnderlineSpan.class);

		for (UnderlineSpan span : underlines) {
			int start = strBuilder.getSpanStart(span);
			int end = strBuilder.getSpanEnd(span);
			int flags = strBuilder.getSpanFlags(span);
			ClickableSpan termsOfUseLauncher = new ClickableSpan() {
				public void onClick(View view) {
					if (BaseActivity.DEBUG)
						MyLog.e(logTag, "on click");

					webViewOn = true;

					// Simplest usage: note that an exception will NOT be thrown
					// if there is an error loading this page (see below).
					((WebView) findViewById(R.id.webView)).loadUrl(kKPTermsURL);

					termsOfUseWeb.setVisibility(View.VISIBLE);

					ObjectAnimator animator = ObjectAnimator.ofFloat(termsOfUseWeb, "translationY", 0);
					animator.setDuration(1000);
					animator.start();

				}
			};
			strBuilder.setSpan(termsOfUseLauncher, start, end, flags);
		}

		termsOfUse.setText(correctLinkPaths(strBuilder));
		// termsOfUse.setLinksClickable(true);
		termsOfUse.setMovementMethod(LinkMovementMethod.getInstance());

	}

	private Activity getActivity() {
		return this;
	}

	/**
	 * Removes relative a hrefs
	 * 
	 * @param spantext
	 *            (from Html.fromhtml())
	 * @return spanned with fixed links
	 */
	private Spanned correctLinkPaths(Spanned spantext) {
		Object[] spans = spantext.getSpans(0, spantext.length(), Object.class);
		for (Object span : spans) {
			int start = spantext.getSpanStart(span);
			int end = spantext.getSpanEnd(span);
			int flags = spantext.getSpanFlags(span);
			if (span instanceof URLSpan) {
				URLSpan urlSpan = (URLSpan) span;
				if (!urlSpan.getURL().startsWith("http")) {
					if (urlSpan.getURL().startsWith("/")) {
						urlSpan = new URLSpan("http://domain+path" + urlSpan.getURL());
					} else {
						urlSpan = new URLSpan("http://domain+path/" + urlSpan.getURL());
					}
				}
				((Spannable) spantext).removeSpan(span);
				((Spannable) spantext).setSpan(urlSpan, start, end, flags);
			}
		}
		return spantext;
	}

	// New activity para email login & register
	private void launchMailLogin() {
		Intent intent = new Intent();
		intent.setClass(this, MailLoginActivity.class);
		startActivityForResult(intent, WelcomeActivity.FINISH);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	private void launchMailRegister() {
		Intent intent = new Intent();
		intent.setClass(this, MailRegisterActivity.class);
		startActivityForResult(intent, WelcomeActivity.FINISH);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	@Override
	public void onBackPressed() {
		if (webViewOn) {
			webViewOn = false;
			ObjectAnimator animator = ObjectAnimator.ofFloat(termsOfUseWeb, "translationY", metrics.heightPixels);
			animator.setDuration(1000);
			animator.start();
		} else {
			super.onBackPressed();
			overridePendingTransition(R.anim.left_in, R.anim.right_out);
		}
	}

	// Twitter and Facebook Login
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == BaseActivity.REQUEST_CODE_TWITTER_LOGIN) {
			
			if (resultCode == RESULT_OK) {
				String token = data.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN);
				String tokenSecret = data.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN_SECRET);
				
				loginRegisterUtil.registerLogin(getActivity(), getActivity(), BaseActivity.TWITTER_LOGIN, null, null, null, HttpConnections.LOGIN_TWITTER_MODE, null, token, tokenSecret, null, null);
			}
		} else {
			mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
		}
		
		if (resultCode == WelcomeActivity.FINISH) {
			setResult(WelcomeActivity.FINISH);
			super.onBackPressed();
		}
	}

	// Twitter Login
	private void loginToTwitter() {
		// Launch twitter activity
		Intent intent = new Intent(this, TwitterLoginActivity.class);
		startActivityForResult(intent, BaseActivity.REQUEST_CODE_TWITTER_LOGIN);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mSimpleFacebook = SimpleFacebook.getInstance(this);

		if (BaseActivity.DEBUG)
			MyLog.d(logTag, "onResume Login");
	}

	// @Override
	// protected void doWithPermissions(Session session) {
	//
	// String permissions = session.getPermissions().toString();
	// Log.d("", "---- Permisos solicitados: " + permissions.toString());
	//
	// // Guardamos facebook login mode
	// SharedPreferences sharedPreferences =
	// getSharedPreferences(BaseActivity.SHARED_PREFERENCES,
	// Context.MODE_PRIVATE);
	// SharedPreferences.Editor editor = sharedPreferences.edit();
	// editor.putBoolean(BaseActivity.FACEBOOK_IS_LOGGED_IN, true);
	//
	// // Guardamos preference si ha permitido publish_actions
	// if (permissions.contains(PUBLISH_ACTIONS)) {
	// editor.putBoolean(BaseActivity.FACEBOOK_PUBLISH_ACTIONS, true);
	// }
	// editor.commit();
	//
	// // Preparamos permisos
	// Log.d("", "---- Permisos enviados: " + permissions);
	// permissions = permissions.replace("[", "");
	// permissions = permissions.replace("]", "");
	// Log.d("", "---- Login tras obtener token: " + session.getAccessToken());
	//
	// // login
	// loginRegisterUtil.registerLogin(getActivity(), getActivity(),
	// BaseActivity.FACEBOOK_LOGIN, null, null, null,
	// HttpConnections.LOGIN_FACEBOOK_MODE, permissions,
	// session.getAccessToken(), null, null);
	//
	// }
}
