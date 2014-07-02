package com.audiosnaps.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import com.audiosnaps.log.MyLog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.WelcomeActivity;
import com.audiosnaps.http.CheckUniqueEmail;
import com.audiosnaps.http.CheckUniqueUserName;
import com.audiosnaps.http.HttpConnections;

public class MailRegisterActivity extends Activity {

	private final String TAG = "MailRegisterActivity";
	private LoginRegisterUtil loginRegisterUtil;
	private Context context = this;
	private boolean userNameOk = false, mailOk = false, pwd1Ok = false, pwd2Ok = false;

	/** Called when the activity is first created. */
	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		final Activity activity = this;

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.mail_register);
		
		loginRegisterUtil = new LoginRegisterUtil(context);

		// Views
		final EditText username = (EditText) findViewById(R.id.username);
		final ProgressBar progressBarUserName = (ProgressBar) findViewById(R.id.progressBarUserName);
		final ImageView userNameCheck = (ImageView) findViewById(R.id.userNameCheck);

		final EditText mail = (EditText) findViewById(R.id.mail);
		final ProgressBar progressBarMail = (ProgressBar) findViewById(R.id.progressBarMail);
		final ImageView mailCheck = (ImageView) findViewById(R.id.mailCheck);

		final EditText pwd = (EditText) findViewById(R.id.pwd);
		final ImageView pwd1Check = (ImageView) findViewById(R.id.pwd1Check);

		final EditText pwd2 = (EditText) findViewById(R.id.pwd2);
		final ImageView pwd2Check = (ImageView) findViewById(R.id.pwd2Check);

		Button nextBtn = (Button) findViewById(R.id.nextBtn);

		// Check user name real time
		username.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (username.length() > 5) {
					progressBarUserName.setVisibility(View.VISIBLE);
					userNameCheck.setVisibility(View.INVISIBLE);
					final Handler handler = new Handler() {
						public void handleMessage(Message msg) {
							final boolean success = (Boolean) msg.obj;
							try {
								if (success) {
									userNameOk = true;
									progressBarUserName.setVisibility(View.INVISIBLE);
									userNameCheck.setBackgroundResource(R.drawable.form_ok_2x);
									userNameCheck.setVisibility(View.VISIBLE);
								} else {
									userNameOk = false;
									progressBarUserName.setVisibility(View.INVISIBLE);
									userNameCheck.setBackgroundResource(R.drawable.form_ko_2x);
									userNameCheck.setVisibility(View.VISIBLE);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						};
					};

					if(BaseActivity.DEBUG) MyLog.d(TAG, "Checking username: " + s);
					CheckUniqueUserName checkUniqueUserName = new CheckUniqueUserName(activity, handler, HttpConnections.USER_NO_ID, null, s.toString());
					checkUniqueUserName.execute();
				} else {
					userNameOk = false;
					progressBarUserName.setVisibility(View.INVISIBLE);
					userNameCheck.setBackgroundResource(R.drawable.form_ko_2x);
					userNameCheck.setVisibility(View.VISIBLE);
				}
			}

			public void afterTextChanged(Editable s) {
			}
		});

		// Check email real time
		mail.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if (isEmailValid(s)) {
					if(BaseActivity.DEBUG) MyLog.d(TAG, "Mail ok");
					progressBarMail.setVisibility(View.VISIBLE);
					mailCheck.setVisibility(View.INVISIBLE);

					final Handler handler = new Handler() {
						public void handleMessage(Message msg) {
							final boolean success = (Boolean) msg.obj;
							try {
								if (success) {
									mailOk = true;
									progressBarMail.setVisibility(View.INVISIBLE);
									mailCheck.setBackgroundResource(R.drawable.form_ok_2x);
									mailCheck.setVisibility(View.VISIBLE);
								} else {
									mailOk = false;
									progressBarMail.setVisibility(View.INVISIBLE);
									mailCheck.setBackgroundResource(R.drawable.form_ko_2x);
									mailCheck.setVisibility(View.VISIBLE);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						};
					};

					CheckUniqueEmail checkUniqueEmail = new CheckUniqueEmail(activity, handler, HttpConnections.USER_NO_ID, null, s.toString());
					checkUniqueEmail.execute();
				} else {
					mailOk = false;
					if(BaseActivity.DEBUG) MyLog.d(TAG, "Mail wrong");
					progressBarMail.setVisibility(View.INVISIBLE);
					mailCheck.setVisibility(View.VISIBLE);
					mailCheck.setBackgroundResource(R.drawable.form_ko_2x);
				}
			}

			public void afterTextChanged(Editable s) {
			}
		});

		// Check password1 lenght
		pwd.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				pwd1Check.setVisibility(View.VISIBLE);
				if (s.length() > 5) {
					if(BaseActivity.DEBUG) MyLog.d(TAG, "Password lenght ok");
					pwd1Ok = true;
					pwd1Check.setBackgroundResource(R.drawable.form_ok_2x);
				} else {
					pwd1Ok = false;
					if(BaseActivity.DEBUG) MyLog.d(TAG, "Password lenght wrong");
					pwd1Check.setBackgroundResource(R.drawable.form_ko_2x);
				}
			}

			public void afterTextChanged(Editable s) {
			}
		});

		// Check password2
		pwd2.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				pwd2Check.setVisibility(View.VISIBLE);
				if (s.toString().equalsIgnoreCase(pwd.getText().toString())) {
					pwd2Ok = true;
					if(BaseActivity.DEBUG) MyLog.d(TAG, "Password match ok");
					pwd2Check.setBackgroundResource(R.drawable.form_ok_2x);
				} else {
					pwd2Ok = false;
					if(BaseActivity.DEBUG) MyLog.d(TAG, "Password match wrong");
					pwd2Check.setBackgroundResource(R.drawable.form_ko_2x);
				}
			}

			public void afterTextChanged(Editable s) {
			}
		});

		// Envio datos registros
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (userNameOk && mailOk && pwd1Ok && pwd2Ok) {
					loginRegisterUtil.registerLogin(activity, context, BaseActivity.MAIL_LOGIN, mail.getText().toString(), username.getText().toString(), pwd.getText().toString(),
							HttpConnections.LOGIN_EMAIL_MODE, null, null, null, null, null);
				}

			}
		});

	}

	private boolean isEmailValid(CharSequence email) {
		if(BaseActivity.DEBUG) MyLog.d(TAG, "Checking email: " + email);
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

}
