package com.audiosnaps.login;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.audiosnaps.log.MyLog;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.WelcomeActivity;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.facebook.FacebookManager;
import com.audiosnaps.http.AppLogout;
import com.audiosnaps.http.GetUserSimpleProfile;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.Login;
import com.facebook.Session;
import com.google.android.gms.gcm.GoogleCloudMessaging;

@SuppressLint("HandlerLeak")
public class LoginRegisterUtil {

	private final static String TAG = "LoginRegisterUtil";

	public static final String SENDER_ID = "614970503154";
	private String userId = "";
	private String koeToken = "";
	private SharedPreferences prefs;
	private Editor editor;

	public static String GMC_ID = "";

	GoogleCloudMessaging gcm;
	Context context;
	String regid = "";

	// Constructor
	public LoginRegisterUtil(Context context) {
		this.context = context;
	}

	// Check if user is logged
	public boolean isLogged(Context context) {
		prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		if (prefs.contains(BaseActivity.IS_LOGGED)) {
			if (prefs.getBoolean(BaseActivity.IS_LOGGED, false)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void saveAsLogged() {

	}

	// Login http
	public void registerLogin(Activity activity, Context context, String loginMode, String email, String userName, String password, String network, String permissions, String accesToken,
			String accesTokenSecret, String apnsToken, Handler handler) {

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "---- START LOGIN ---");

		// GMC check register
		String regid = "";
		prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		regid = prefs.getString(BaseActivity.GMC_REGISTER_ID, "");
		gcm = GoogleCloudMessaging.getInstance(context);

		if (regid.length() == 0) {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- SIN GMC_ID, REGISTRAMOS ----");
			registerBackground(activity, context, loginMode, email, userName, password, network, permissions, accesToken, accesTokenSecret, apnsToken, handler);
		} else {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- GMC_ID: " + regid);
			doRegisterLogin(activity, context, loginMode, email, userName, password, network, permissions, accesToken, accesTokenSecret, apnsToken, regid, handler);
		}
	}

	// Ejecuta login una vez tengamos GCM_ID
	private void doRegisterLogin(final Activity activity, final Context context, final String loginMode, String email, String userName, String password, String network, final String permissions,
			String accesToken, String accesTokenSecret, String apnsToken, String regid, final Handler handler) {

		prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		editor = prefs.edit();

		final Handler loginHandler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				try {
					if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

						JSONObject jsonObject = new JSONObject(result);

						// Login ok, Guardamos datos user

						koeToken = jsonObject.getString(HttpConnections.KOE_TOKEN);
						editor.putString(HttpConnections.KOE_TOKEN, jsonObject.getString(HttpConnections.KOE_TOKEN));
						userId = jsonObject.getString(HttpConnections.USER_ID);
						editor.putString(HttpConnections.USER_ID, jsonObject.getString(HttpConnections.USER_ID));
						editor.putString(HttpConnections.USER_NAME, jsonObject.getString(HttpConnections.USER_NAME));
						editor.putString(HttpConnections.PRIVACY_MODE, jsonObject.getString(HttpConnections.PRIVACY_MODE));

						// Guardamos el login mode
						if (loginMode == BaseActivity.TWITTER_LOGIN) {
							if (!jsonObject.isNull(HttpConnections.TWITTER_DATA)) {
								if (jsonObject.get(HttpConnections.TWITTER_DATA) instanceof JSONObject) {
									editor.putBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, !jsonObject.getJSONObject(HttpConnections.TWITTER_DATA).getString(HttpConnections.ID).equalsIgnoreCase("null"));
								}
							}
						}
						
						// Guardamos el login mode
						if (loginMode == BaseActivity.FACEBOOK_LOGIN) {
							if (!jsonObject.isNull(HttpConnections.FACEBOOK_DATA)) {
								if (jsonObject.get(HttpConnections.FACEBOOK_DATA) instanceof JSONObject) {
									editor.putBoolean(BaseActivity.FACEBOOK_IS_LOGGED_IN, !jsonObject.getJSONObject(HttpConnections.FACEBOOK_DATA).getString(HttpConnections.ID).equalsIgnoreCase("null"));
									 // Guardamos preference si ha permitido publish_actions
									 if (permissions.contains(FacebookManager.PUBLISH_ACTIONS)) {
										 editor.putBoolean(BaseActivity.FACEBOOK_PUBLISH_ACTIONS, true);
									 }
								}
							}
						}
						
						editor.putBoolean(BaseActivity.IS_LOGGED, true);
						editor.putString(BaseActivity.LOGGED_MODE, loginMode);
						editor.commit();
						
						if (BaseActivity.DEBUG)
							MyLog.d(TAG, "New koe_token login: " + jsonObject.getString(HttpConnections.KOE_TOKEN));

						final Handler handlerUserInfo = new Handler() {
							public void handleMessage(Message msg) {
								final String result = (String) msg.obj;
								try {
									if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
										try {
											JSONObject jsonObject = new JSONObject(result);

											prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
											editor = prefs.edit();
											editor.putString(HttpConnections.NUM_OF_FOLLOWERS, jsonObject.getString(HttpConnections.NUM_OF_FOLLOWERS));
											editor.putString(HttpConnections.NUM_OF_FRIENDS, jsonObject.getString(HttpConnections.NUM_OF_FRIENDS));
											editor.putString(HttpConnections.NUM_OF_PICS, jsonObject.getString(HttpConnections.NUM_OF_PICS));
											editor.putString(HttpConnections.PICTURE_URL, jsonObject.getString(HttpConnections.PICTURE_URL));
											editor.putString(HttpConnections.USER_CAPTION, jsonObject.getString(HttpConnections.USER_CAPTION));
											editor.commit();

											if (BaseActivity.DEBUG)
												MyLog.d(TAG, "Login ok, continue to MainActivity");

											// Lanzamos MainActivity
											Intent intent = new Intent(context, MainActivity.class);
											activity.startActivity(intent);
											activity.setResult(WelcomeActivity.FINISH);
											activity.finish();

										} catch (Exception e) {
											e.printStackTrace();
											if (BaseActivity.DEBUG)
												MyLog.d(TAG, "Exception");
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							};
						};
						GetUserSimpleProfile getUserSimpleProfile = new GetUserSimpleProfile(context, handlerUserInfo, userId, userId, koeToken);
						getUserSimpleProfile.execute();
					}else{
						if(handler != null) handler.dispatchMessage(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			};
		};

		new Login(context, loginHandler, email, userName, password, network, permissions, accesToken, accesTokenSecret, regid).execute();
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration id, app versionCode, and expiration time in the application's shared preferences.
	 */
	private void registerBackground(final Activity activity, final Context context, final String loginMode, final String email, final String userName, final String password, final String network,
			final String permissions, final String accesToken, final String accesTokenSecret, final String apnsToken, final Handler handler) {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration id=" + regid;
					setRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "---- GMD ID: " + regid);
				doRegisterLogin(activity, context, loginMode, email, userName, password, network, permissions, accesToken, accesTokenSecret, apnsToken, regid, handler);
			}
		}.execute(null, null, null);
	}

	// Desregistro de GCM
	private void unregisterBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					gcm.unregister();
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration id=" + regid;

					setRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "---- GMD_ID UNREGISTERED -----");
			}
		}.execute(null, null, null);
	}

	// Guarda GCM_ID en SharedPreferences
	private void setRegistrationId(Context context, String regId) {
		editor = prefs.edit();
		editor.putString(BaseActivity.GMC_REGISTER_ID, regId);
		editor.commit();
	}

	// Ejecuta Logout
	public void logout(final Activity activity, final Context context, final boolean showToastError, boolean noSDCard) {

		prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		editor = prefs.edit();
		final String loginMode = prefs.getString(BaseActivity.LOGGED_MODE, "");

		// No SD Card
		if (noSDCard) {
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					final String result = (String) msg.obj;
					try {
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							// Logout SOC
							try {
								editor.clear();
								editor.commit();

								if (loginMode.equalsIgnoreCase(BaseActivity.FACEBOOK_LOGIN)) {
									try {
										Session session = Session.getActiveSession();
										session.closeAndClearTokenInformation();
									} catch (Exception e) {
										e.printStackTrace();
										if (BaseActivity.DEBUG)
											MyLog.d(TAG, "Exception, no facebook sesion active");
									}

								} else if (loginMode.equalsIgnoreCase(BaseActivity.TWITTER_LOGIN)) {

								} else if (loginMode.equalsIgnoreCase(BaseActivity.MAIL_LOGIN)) {

								}

								unregisterBackground();

								if (BaseActivity.DEBUG)
									MyLog.d(TAG, "Logout!");
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								// Vamos a pantalla login
								Intent intent = new Intent(context, WelcomeActivity.class);
								intent.putExtra("logout", true);
								activity.finish();
								activity.startActivity(intent);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			};

			new AppLogout(context, handler, LoggedUser.id, prefs.getString(BaseActivity.GMC_REGISTER_ID, ""), LoggedUser.koeToken, showToastError).execute();
		}

		// Normal logout
		else {
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					final String result = (String) msg.obj;
					try {
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							// Eliminamos todas las SharedPreferences
							editor.clear();
							editor.commit();

							// Logout SOC
							if (loginMode.equalsIgnoreCase(BaseActivity.FACEBOOK_LOGIN)) {
								try {
									Session session = Session.getActiveSession();
									session.closeAndClearTokenInformation();
								} catch (Exception e) {
									e.printStackTrace();
									if (BaseActivity.DEBUG)
										MyLog.d(TAG, "Exception, no facebook sesion active");
								}

							} else if (loginMode.equalsIgnoreCase(BaseActivity.TWITTER_LOGIN)) {

							} else if (loginMode.equalsIgnoreCase(BaseActivity.MAIL_LOGIN)) {

							}

							// Eliminamos registro a GMC
							unregisterBackground();

							if (BaseActivity.DEBUG)
								MyLog.d(TAG, "Logout!");

							// Vamos a pantalla login
							Intent intent = new Intent(context, WelcomeActivity.class);
							intent.putExtra("logout", true);
							activity.finish();
							activity.startActivity(intent);

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			};

			new AppLogout(context, handler, LoggedUser.id, prefs.getString(BaseActivity.GMC_REGISTER_ID, ""), LoggedUser.koeToken, showToastError).execute();
		}
	}

}
