package com.audiosnaps.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.http.HttpConnections;

public class MailLoginActivity extends Activity {

	private final static String TAG = "MailLoginActivity";
	private Context context = this;
	private Activity activity = this;
	private EditText txtPassowrd, txtMail;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.mail_login);

		txtMail = (EditText) findViewById(R.id.mail);
		txtPassowrd = (EditText) findViewById(R.id.pwd);
		final Button nextBtn = (Button) findViewById(R.id.nextBtn);

		nextBtn.setOnClickListener(new OnClickListener() {

			@SuppressLint("HandlerLeak")
			@Override
			public void onClick(View v) {
				nextBtn.setEnabled(false);
				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						final String result = (String) msg.obj;
						if (result.equalsIgnoreCase(HttpConnections.ERROR)) {
							nextBtn.setEnabled(true);
						}
					};
				};
				
				LoginRegisterUtil loginRegisterUtil = new LoginRegisterUtil(context);
				loginRegisterUtil.registerLogin(activity, context, BaseActivity.MAIL_LOGIN, txtMail.getText().toString(), null, txtPassowrd.getText().toString(), HttpConnections.LOGIN_EMAIL_MODE, null, null, null, null, handler);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
