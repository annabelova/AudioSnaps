package com.audiosnaps.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.facebook.FacebookManager;
import com.audiosnaps.http.AddSocNetworkFromMobile;
import com.audiosnaps.http.CheckUniqueUserName;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.SetUserSettings;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.twitter.TwitterLoginActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sromku.simple.fb.SimpleFacebook;

@SuppressLint("HandlerLeak")
public class UserConfigActivity extends Activity {

	private final static String TAG = "UserConfigActivity";
	private Context context = this;
	private Activity activity = this;
	// private UserClass userClass = null;
	private String privacy, sharesFb, sharesTw;
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	private boolean userNameOk = true, mailOk = true;

	// fb login
	private CheckBox checkBoxShareLikesFacebookConfig;
	private CheckBox checkBoxShareLikesTwitterConfig;
	private SimpleFacebook mSimpleFacebook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);

		// userClass = MainActivity.userClass;
		initConfig();
	}

	// Actualiza layout info user
	public void initConfig() {

		final SharedPreferences preferences = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		final Editor editor = preferences.edit();

		// Views
		ImageView imgAvatarConfig = (ImageView) activity.findViewById(R.id.imgAvatarConfig);
		final EditText txtBioConfig = (EditText) activity.findViewById(R.id.txtBioConfig);
		final ImageView btnRockstarConfig = (ImageView) activity.findViewById(R.id.btnRockstarConfig);
		final ImageView btnGrandmaConfig = (ImageView) activity.findViewById(R.id.btnGrandmaConfig);
		checkBoxShareLikesFacebookConfig = (CheckBox) activity.findViewById(R.id.checkBoxShareLikesFacebookConfig);
		checkBoxShareLikesTwitterConfig = (CheckBox) activity.findViewById(R.id.checkBoxShareLikesTwitterConfig);
		final CheckBox checkBoxSaveLibraryConfig = (CheckBox) activity.findViewById(R.id.checkBoxSaveLibraryConfig);
		// final EditText txtEmailConfig = (EditText)
		// activity.findViewById(R.id.txtEmailConfig);
		ImageView btnSaveConfig = (ImageView) activity.findViewById(R.id.btnSaveConfig);
		final Spinner spinnerInFacebookConfig = (Spinner) activity.findViewById(R.id.spinnerInFacebookConfig);
		final Spinner spinnerInMyPhoneConfig = (Spinner) activity.findViewById(R.id.spinnerInMyPhoneConfig);
		final Spinner spinnerByEmailConfig = (Spinner) activity.findViewById(R.id.spinnerByEmailConfig);
		final TextView privacyModeText = (TextView) activity.findViewById(R.id.privacyModeText);
		final TextView contadorCaracteresBioConfig = (TextView) activity.findViewById(R.id.contadorCaracteresBioConfig);
		TextView lblTuPaginaDePerfil = (TextView) activity.findViewById(R.id.lblTuPaginaDePerfil);
		final EditText txtUserNameConfig = (EditText) activity.findViewById(R.id.txtUserNameConfig);
		final ProgressBar progressBarUserName = (ProgressBar) findViewById(R.id.progressBarUserName);
		final ImageView userNameCheck = (ImageView) findViewById(R.id.userNameCheck);
		// final ProgressBar progressBarMail = (ProgressBar)
		// findViewById(R.id.progressBarMail);
		// final ImageView mailCheck = (ImageView) findViewById(R.id.mailCheck);

		// init spinners
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, BaseActivity.arrayIntervalosNotificaciones);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerInFacebookConfig.setAdapter(spinnerAdapter);
		spinnerInMyPhoneConfig.setAdapter(spinnerAdapter);
		spinnerByEmailConfig.setAdapter(spinnerAdapter);
		initSpinner(spinnerInFacebookConfig, Integer.valueOf(preferences.getString(HttpConnections.NOTIFICATION_FB_INTERVAL, "0")));
		initSpinner(spinnerInMyPhoneConfig, Integer.valueOf(preferences.getString(HttpConnections.NOTIFICATION_APNS_INTERVAL, "0")));
		initSpinner(spinnerByEmailConfig, Integer.valueOf(preferences.getString(HttpConnections.NOTIFICATION_EMAIL_INTERVAL, "0")));

		// Fill layout
		try {
			if (preferences.getString(HttpConnections.PICTURE_URL, null) != null) {
				imageLoader.displayImage(preferences.getString(HttpConnections.PICTURE_URL, ""), imgAvatarConfig, BaseActivity.optionsAvatarImage, null);
			}
		} catch (Exception e) {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Exception");
			e.printStackTrace();
		}

		lblTuPaginaDePerfil.setText(lblTuPaginaDePerfil.getText().toString().replace("%@", preferences.getString(HttpConnections.USER_NAME, "")));
		txtUserNameConfig.setText(preferences.getString(HttpConnections.USER_NAME, ""));

		// txtEmailConfig.setText(preferences.getString(HttpConnections.EMAIL,
		// ""));

		if (preferences.getString(HttpConnections.USER_CAPTION, "") != null) {
			if (preferences.getString(HttpConnections.USER_CAPTION, "").equalsIgnoreCase("null")) {
				txtBioConfig.setText("");
				contadorCaracteresBioConfig.setText("100");
			} else {
				txtBioConfig.setText(preferences.getString(HttpConnections.USER_CAPTION, ""));
				contadorCaracteresBioConfig.setText(String.valueOf(100 - preferences.getString(HttpConnections.USER_CAPTION, "").length()));
			}
		}

		privacy = preferences.getString(HttpConnections.PRIVACY_MODE, HttpConnections.ROCKSTAR);
		if (privacy.equalsIgnoreCase(HttpConnections.ROCKSTAR)) {
			btnRockstarConfig.setBackgroundResource(R.drawable.rockstar_on);
			privacyModeText.setText(context.getResources().getString(R.string.ROCKSTAR_COPY));
		} else {
			btnGrandmaConfig.setBackgroundResource(R.drawable.grandma_on);
			privacyModeText.setText(context.getResources().getString(R.string.GRANDMA_COPY));
		}

		RelativeLayout relativeFacebookShareConfig = (RelativeLayout) findViewById(R.id.relativeFacebookShareConfig);

		// Facebook associated?
		if (preferences.getBoolean(BaseActivity.FACEBOOK_IS_LOGGED_IN, false)) {
			sharesFb = preferences.getString(HttpConnections.SHARE_LIKES_FB, "0");
			if (sharesFb.equalsIgnoreCase(BaseActivity.ACTIVATED)) {
				checkBoxShareLikesFacebookConfig.setChecked(true);
			}
		} else {
			//relativeFacebookShareConfig.setVisibility(View.GONE);
		}

		RelativeLayout relativeTwitterShareConfig = (RelativeLayout) findViewById(R.id.relativeTwitterShareConfig);

		// Twitter associated?
		if (preferences.getBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, false)) {
			sharesTw = preferences.getString(HttpConnections.SHARE_LIKES_TW, "0");
			if (sharesTw.equalsIgnoreCase(BaseActivity.ACTIVATED)) {
				checkBoxShareLikesTwitterConfig.setChecked(true);
			}
		} else {
			//relativeTwitterShareConfig.setVisibility(View.GONE);
		}

		// hide upper separator
		if (relativeFacebookShareConfig.getVisibility() == View.GONE && relativeTwitterShareConfig.getVisibility() == View.GONE) {
			findViewById(R.id.separador2).setVisibility(View.GONE);
		}

		if (preferences.getBoolean(BaseActivity.SAVE_IN_LIBRARY, true)) {
			checkBoxSaveLibraryConfig.setChecked(true);
		}

		// Listeners
		btnRockstarConfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnRockstarConfig.setBackgroundResource(R.drawable.rockstar_on);
				btnGrandmaConfig.setBackgroundResource(R.drawable.grandma_off);
				privacyModeText.setText(context.getResources().getString(R.string.ROCKSTAR_COPY));
				privacy = HttpConnections.ROCKSTAR;
			}
		});

		btnGrandmaConfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnRockstarConfig.setBackgroundResource(R.drawable.rockstar_off);
				btnGrandmaConfig.setBackgroundResource(R.drawable.grandma_on);
				privacyModeText.setText(context.getResources().getString(R.string.GRANDMA_COPY));
				privacy = HttpConnections.GRANDMA;
			}
		});

		if (!getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE).getBoolean(BaseActivity.FACEBOOK_PUBLISH_ACTIONS, false)) {

			checkBoxShareLikesFacebookConfig.setChecked(false);

			checkBoxShareLikesFacebookConfig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						checkBoxShareLikesFacebookConfig.setChecked(false);
					}
				}
			});

			checkBoxShareLikesFacebookConfig.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					
					// add_soc_network_from_mobile
					final Handler handler = new Handler() {
						public void handleMessage(Message msg) {
							final String result = (String) msg.obj;
							if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
			
								if (msg.arg1 == BaseActivity.FACEBOOK_HAS_PUBLISH_ACTIONS) {
			
									SharedPreferences sharedPreferences = getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = sharedPreferences.edit();
									editor.putBoolean(BaseActivity.FACEBOOK_IS_LOGGED_IN, true);
									editor.putBoolean(BaseActivity.FACEBOOK_PUBLISH_ACTIONS, true);
									editor.putString(HttpConnections.SHARE_LIKES_FB, "1");
									editor.commit();
			
									checkBoxShareLikesFacebookConfig.setOnClickListener(null);
									
									checkBoxShareLikesFacebookConfig.setOnCheckedChangeListener(null);
									
									checkBoxShareLikesFacebookConfig.setChecked(true);
			
								} else {
			
									SharedPreferences sharedPreferences = getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = sharedPreferences.edit();
									editor.putBoolean(BaseActivity.FACEBOOK_IS_LOGGED_IN, true);
									editor.commit();
			
									checkBoxShareLikesFacebookConfig.setChecked(false);
			
								}
			
							} else {
			
								checkBoxShareLikesFacebookConfig.setChecked(false);
			
							}
						};
					};
					
					FacebookManager.askForReadWritePermissionsAndAddSocNetwork(getActivity(), mSimpleFacebook, handler);
					
				}});

		}

		checkBoxShareLikesTwitterConfig.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Twitter associated?
				if (!preferences.getBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, false)) {
					checkBoxShareLikesTwitterConfig.setChecked(false);
					loginToTwitter();
				}
			}
		});
		
		checkBoxSaveLibraryConfig.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkBoxSaveLibraryConfig.isChecked()) {
					editor.putBoolean(BaseActivity.SAVE_IN_LIBRARY, true);
					editor.commit();
				} else {
					editor.putBoolean(BaseActivity.SAVE_IN_LIBRARY, false);
					editor.commit();
				}
			}
		});
		txtBioConfig.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// This sets a textview to the current length
				contadorCaracteresBioConfig.setText(String.valueOf(100 - s.length()));
			}

			public void afterTextChanged(Editable s) {
			}
		});

		// Check user name real time
		txtUserNameConfig.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (txtUserNameConfig.length() > 5) {
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

					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Checking username: " + s);
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

		// Save listener
		btnSaveConfig.setOnClickListener(new OnClickListener() {
							
			@Override
			public void onClick(View v) {

				final String sharesFb = (checkBoxShareLikesFacebookConfig.isChecked()) ? BaseActivity.ACTIVATED : BaseActivity.DEACTIVATED;
				final String sharesTw = (checkBoxShareLikesTwitterConfig.isChecked()) ? BaseActivity.ACTIVATED : BaseActivity.DEACTIVATED;
				
				// Email && UserName ok
				if (mailOk && userNameOk) {
					final Handler handler = new Handler() {
						public void handleMessage(Message msg) {

							// Settings sava ok
							if ((Boolean) msg.obj) {

								SharedPreferences preferences = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
								Editor editor = preferences.edit();
								editor.putString(HttpConnections.USER_CAPTION, txtBioConfig.getText().toString());
								editor.putString(HttpConnections.PRIVACY_MODE, privacy);
								editor.putString(HttpConnections.USER_NAME, txtUserNameConfig.getText().toString());
								// editor.putString(HttpConnections.EMAIL,
								// txtEmailConfig.getText().toString());
								editor.putString(HttpConnections.NOTIFICATION_FB_INTERVAL, String.valueOf(spinnerSelection(spinnerInFacebookConfig)));
								editor.putString(HttpConnections.NOTIFICATION_APNS_INTERVAL, String.valueOf(spinnerSelection(spinnerInMyPhoneConfig)));
								editor.putString(HttpConnections.NOTIFICATION_EMAIL_INTERVAL, String.valueOf(spinnerSelection(spinnerByEmailConfig)));
								editor.putString(HttpConnections.SHARE_LIKES_FB, sharesFb);
								editor.putString(HttpConnections.SHARE_LIKES_TW, sharesTw);
								editor.commit();
								
								Toast.makeText(context, context.getResources().getString(R.string.SETTINGS_SAVED), Toast.LENGTH_SHORT).show();

								finish();
							}
							// Settings save error
							else {
								Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
							}
						};
					};

					SetUserSettings setUserSettings = new SetUserSettings(context, handler, LoggedUser.id, LoggedUser.koeToken, txtBioConfig.getText().toString(), privacy, sharesFb, sharesTw,
							spinnerSelection(spinnerInFacebookConfig), spinnerSelection(spinnerInMyPhoneConfig), spinnerSelection(spinnerByEmailConfig), "email", txtUserNameConfig.getText().toString());
					setUserSettings.execute();
				}
				// Email in use
				else {
					Toast.makeText(context, context.getResources().getString(R.string.NOT_UNIQUE_USERNAME), Toast.LENGTH_SHORT).show();

					// if (!mailOk && !userNameOk) {
					// Toast.makeText(context,
					// context.getResources().getString(R.string.USERNAME_OR_EMAIL_ALREADY_TAKEN),
					// Toast.LENGTH_SHORT).show();
					// } else if (!userNameOk && mailOk) {
					// Toast.makeText(context,
					// context.getResources().getString(R.string.NOT_UNIQUE_USERNAME),
					// Toast.LENGTH_SHORT).show();
					// } else if (userNameOk && !mailOk) {
					// Toast.makeText(context,
					// context.getResources().getString(R.string.EMAIL_EXISTS_WRONG_PASSWORD),
					// Toast.LENGTH_SHORT).show();
					// }

				}

			}
		});
	}

	// private boolean isEmailValid(CharSequence email) {
	// if (BaseActivity.DEBUG)
	// MyLog.d(TAG, "Checking email: " + email);
	// return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	// }

	// Inicializa el spinner en la posición del valor que tenga el user
	private void initSpinner(Spinner spinner, int interval) {

		switch (interval) {
		case HttpConnections.AS_IT_HAPPENS:
			spinner.setSelection(0);
			break;
		case HttpConnections.HOURLY:
			spinner.setSelection(1);
			break;
		case HttpConnections.DAILY:
			spinner.setSelection(2);
			break;
		case HttpConnections.MONTHLY:
			spinner.setSelection(3);
			break;
		case HttpConnections.NEVER:
			spinner.setSelection(4);
			break;
		case HttpConnections.BACKEND_DEFAULT_INTERVAL:
			spinner.setSelection(2);
			break;
		default:
			spinner.setSelection(0);
			break;
		}
	}

	// Devuelve valor a enviar al server según posición spinner
	private int spinnerSelection(Spinner spinner) {

		int interval = 0;

		switch (spinner.getSelectedItemPosition()) {
		case HttpConnections.POSITION_AS_IT_HAPPENS:
			interval = HttpConnections.AS_IT_HAPPENS;
			break;
		case HttpConnections.POSITION_HOURLY:
			interval = HttpConnections.HOURLY;
			break;
		case HttpConnections.POSITION_DAILY:
			interval = HttpConnections.DAILY;
			break;
		case HttpConnections.POSITION_MONTHLY:
			interval = HttpConnections.MONTHLY;
			break;
		case HttpConnections.POSITION_NEVER:
			interval = HttpConnections.NEVER;
			break;
		default:
			interval = HttpConnections.AS_IT_HAPPENS;
			break;
		}

		return interval;
	}

	// Twitter Login
	private void loginToTwitter() {
		// Launch twitter activity
		Intent intent = new Intent(this, TwitterLoginActivity.class);
		startActivityForResult(intent, BaseActivity.REQUEST_CODE_TWITTER_LOGIN);
	}

	// Twitter and Facebook Login
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

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
							editor.putString(HttpConnections.SHARE_LIKES_TW, "1");
							editor.commit();

							checkBoxShareLikesTwitterConfig.setChecked(true);

						}
					};
				};

				AddSocNetworkFromMobile addSocNetworkFromMobile = new AddSocNetworkFromMobile(this, handler, LoggedUser.id, "twitter", null, token, tokenSecret, LoggedUser.koeToken);
				addSocNetworkFromMobile.execute();

			}
		} else{
			mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
			super.onActivityResult(requestCode, resultCode, data);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
	}
	
	private Activity getActivity(){
		return this;
	}

}
