package com.audiosnaps.facebook;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.classes.Dialogos;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.http.AddSocNetworkFromMobile;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.login.LoginRegisterUtil;
import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebook.OnLoginListener;
import com.sromku.simple.fb.SimpleFacebook.OnLogoutListener;
import com.sromku.simple.fb.SimpleFacebook.OnPermissionListener;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

public class FacebookManager {

	public static final String PUBLISH_ACTIONS = "publish_actions";

	private static String logTag = "FacebookManager";
	
	private static ProgressDialog progressDialog;
	
	public static void askForReadWritePermissionsAndDoLoginRegister(final Activity activity, final SimpleFacebook mSimpleFacebook){
		
		Permissions[] permissions = new Permissions[] { Permissions.BASIC_INFO, Permissions.EMAIL };
		
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
		    .setAppId(activity.getResources().getString(R.string.app_id))
		    .setNamespace("audiosnaps")
		    .setPermissions(permissions)
		    .build();
		
		SimpleFacebook.setConfiguration(configuration);
		
		final Dialogos dialogos = new Dialogos(activity);

		if(mSimpleFacebook.isLogin()){
			
			askForPublishPermissionsDoRegisterLoginAndLogout(activity, mSimpleFacebook);
			
		}else{
		
				// login listener
				OnLoginListener onLoginListener = new SimpleFacebook.OnLoginListener() {
	
					@Override
					public void onFail(String reason) {
						dismiss(progressDialog);
						Log.i(logTag, reason);
					}
		
					@Override
					public void onException(Throwable throwable) {
						dismiss(progressDialog);
						Log.i(logTag, "Bad thing happened", throwable);
					}
		
					@Override
					public void onThinking() {
						// show progress bar or something to the user while login is happening
						progressDialog = dialogos.loadingProgressDialog();
						progressDialog.show();
						Log.i(logTag, "In progress");
					}
		
					@Override
					public void onLogin() {
						 
						askForPublishPermissionsDoRegisterLoginAndLogout(activity, mSimpleFacebook);
						
					}
	
					@Override
					public void onNotAcceptingPermissions() {
						dismiss(progressDialog);
						Log.i(logTag, "User didn't accept read permissions");
					}
		
				};
		
				// log in
				mSimpleFacebook.login(onLoginListener);
			
			}

	}
	
	private static void askForPublishPermissionsDoRegisterLoginAndLogout(final Activity activity, final SimpleFacebook mSimpleFacebook){
		
		Log.i(logTag, "Logged in");
		
		OnPermissionListener mOnPermissionListener = new OnPermissionListener() {

		    @Override
		    public void onSuccess(final String accessToken) 
		    {
		    	// pass updated accessToken
		    	doLoginRegisterAndLogOut(accessToken, activity, mSimpleFacebook, true);
		    	Log.i(logTag, "publish permissions success");
		    }

		    @Override
		    public void onNotAcceptingPermissions() 
		    {
		    	// pass old access token
		    	doLoginRegisterAndLogOut(mSimpleFacebook.getAccessToken(), activity, mSimpleFacebook, false);
		        Log.i(logTag, "User didn't accept publish permissions");
		    }

		    @Override
		    public void onThinking() 
		    {
		        // show progress bar or something 
		        Log.i(logTag, "Thinking...");
		    }

		    @Override
		    public void onException(final Throwable throwable) 
		    {
		    	// pass old access token
		    	doLoginRegisterAndLogOut(mSimpleFacebook.getAccessToken(), activity, mSimpleFacebook, false);
		        Log.i(logTag, "Bad thing happened", throwable);
		    }

		    @Override
		    public void onFail(final String reason) 
		    {
		    	// pass old access token
		    	doLoginRegisterAndLogOut(mSimpleFacebook.getAccessToken(), activity, mSimpleFacebook, false);
		        // insure that you are logged in before getting the albums
		        Log.i(logTag, reason);
		    }
		};
		
		Permissions[] permissions = new Permissions[] { Permissions.BASIC_INFO, Permissions.EMAIL, Permissions.PUBLISH_ACTION };
		
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
		    .setAppId(activity.getResources().getString(R.string.app_id))
		    .setNamespace("audiosnaps")
		    .setPermissions(permissions)
		    .build();
		
		SimpleFacebook.setConfiguration(configuration);
		
		mSimpleFacebook.requestPublish(mOnPermissionListener);
		
	}
	
	private static void dismiss(ProgressDialog progressDialog) {
		if(progressDialog != null) 
			if(progressDialog.isShowing()) 
				progressDialog.dismiss();
	}
	
	private static void doLoginRegisterAndLogOut(String accessToken, final Activity activity, SimpleFacebook mSimpleFacebook, boolean publishPermissions){
		
		// the updated access token
        //Log.i(logTag, accessToken);
        
        // Get permissions
		final List<String> permissions = new ArrayList<String>();
		permissions.add("basic_info");
		permissions.add("email");
		if(publishPermissions) permissions.add(PUBLISH_ACTIONS);
		
		// do login/register a audiosnaps
		//Log.i(logTag, "login/register");
		
		LoginRegisterUtil loginRegisterUtil = new LoginRegisterUtil(activity);
		
		// login
		loginRegisterUtil .registerLogin(activity, activity, 
				BaseActivity.FACEBOOK_LOGIN, null, null, null, HttpConnections.LOGIN_FACEBOOK_MODE, 
				permissions.toString().replace("[", "").replace("]", ""), 
				mSimpleFacebook.getAccessToken(), null, null, null);

		// log permissions
		//Log.i(logTag, permissions.toString().replace("[", "").replace("]", ""));

		// logout listener
		OnLogoutListener onLogoutListener = new SimpleFacebook.OnLogoutListener() {

			@Override
			public void onFail(String reason) {
				dismiss(progressDialog);
				Log.i(logTag, reason);
			}

			@Override
			public void onException(Throwable throwable) {
				dismiss(progressDialog);
				Log.i(logTag, "Bad thing happened", throwable);
			}

			@Override
			public void onThinking() {
				// show progress bar or something to the user while login is happening
				Log.i(logTag, "In progress");
			}

			@Override
			public void onLogout() {
				dismiss(progressDialog);
				Log.i(logTag, "You are logged out");
			}

		};

		// log out
		mSimpleFacebook.logout(onLogoutListener);
	}
	
	public static void askForReadWritePermissionsAndAddSocNetwork(final Activity activity, final SimpleFacebook mSimpleFacebook, final Handler handler){
		
		Permissions[] permissions = new Permissions[] { Permissions.BASIC_INFO, Permissions.EMAIL };
		
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
		    .setAppId(activity.getResources().getString(R.string.app_id))
		    .setNamespace("audiosnaps")
		    .setPermissions(permissions)
		    .build();
		
		SimpleFacebook.setConfiguration(configuration);
		
		final Dialogos dialogos = new Dialogos(activity);

		if(mSimpleFacebook.isLogin()){
			
			askForPublishPermissionsDoAddSocNetworkAndLogout(activity, mSimpleFacebook, handler);
			
		}else{
		
				// login listener
				OnLoginListener onLoginListener = new SimpleFacebook.OnLoginListener() {
	
					@Override
					public void onFail(String reason) {
						dismiss(progressDialog);
						Log.i(logTag, reason);
					}
		
					@Override
					public void onException(Throwable throwable) {
						dismiss(progressDialog);
						Log.i(logTag, "Bad thing happened", throwable);
					}
		
					@Override
					public void onThinking() {
						// show progress bar or something to the user while login is happening
						progressDialog = dialogos.loadingProgressDialog();
						progressDialog.show();
						Log.i(logTag, "In progress");
					}
		
					@Override
					public void onLogin() {
						 
						askForPublishPermissionsDoAddSocNetworkAndLogout(activity, mSimpleFacebook, handler);
						
					}
	
					@Override
					public void onNotAcceptingPermissions() {
						dismiss(progressDialog);
						Log.i(logTag, "User didn't accept read permissions");
					}
		
				};
		
				// log in
				mSimpleFacebook.login(onLoginListener);
			
			}
		
	}
	
	private static void askForPublishPermissionsDoAddSocNetworkAndLogout(final Activity activity, final SimpleFacebook mSimpleFacebook, final Handler handler){
		
		Log.i(logTag, "Logged in");
		
		OnPermissionListener mOnPermissionListener = new OnPermissionListener() {

		    @Override
		    public void onSuccess(final String accessToken) 
		    {
		    	// pass updated accessToken
		    	doAddSocNetworkAndLogOut(accessToken, activity, mSimpleFacebook, handler, true);
		    	Log.i(logTag, "publish permissions success");
		    }

			@Override
		    public void onNotAcceptingPermissions() 
		    {
		    	// pass old access token
		    	doAddSocNetworkAndLogOut(mSimpleFacebook.getAccessToken(), activity, mSimpleFacebook, handler, false);
		        Log.i(logTag, "User didn't accept publish permissions");
		    }

		    @Override
		    public void onThinking() 
		    {
		        // show progress bar or something 
		        Log.i(logTag, "Thinking...");
		    }

		    @Override
		    public void onException(final Throwable throwable) 
		    {
		    	// pass old access token
		    	doAddSocNetworkAndLogOut(mSimpleFacebook.getAccessToken(), activity, mSimpleFacebook, handler, false);
		        Log.i(logTag, "Bad thing happened", throwable);
		    }

		    @Override
		    public void onFail(final String reason) 
		    {
		    	// pass old access token
		    	doAddSocNetworkAndLogOut(mSimpleFacebook.getAccessToken(), activity, mSimpleFacebook, handler, false);
		        // insure that you are logged in before getting the albums
		        Log.i(logTag, reason);
		    }
		};
		
		Permissions[] permissions = new Permissions[] { Permissions.BASIC_INFO, Permissions.EMAIL, Permissions.PUBLISH_ACTION };
		
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
		    .setAppId(activity.getResources().getString(R.string.app_id))
		    .setNamespace("audiosnaps")
		    .setPermissions(permissions)
		    .build();
		
		SimpleFacebook.setConfiguration(configuration);
		
		mSimpleFacebook.requestPublish(mOnPermissionListener);
		
	}
	
	 private static void doAddSocNetworkAndLogOut(String accessToken, final Activity activity, SimpleFacebook mSimpleFacebook, Handler handler, boolean publishPermissions) {
			
		 	// the updated access token
	        Log.i(logTag, accessToken);
	        
	        // Get permissions
			final List<String> permissions = new ArrayList<String>();
			permissions.add("basic_info");
			permissions.add("email");
			if(publishPermissions) permissions.add(PUBLISH_ACTIONS);
			
			// do login/register a audiosnaps
			Log.i(logTag, "login/register");
			
			// add_soc_network_from_mobile
			AddSocNetworkFromMobile addSocNetworkFromMobile = 
					new AddSocNetworkFromMobile(activity, handler, LoggedUser.id, "facebook", permissions.toString().replace("[", "").replace("]", ""), accessToken, null, LoggedUser.koeToken);
			addSocNetworkFromMobile.execute();

			// log permissions
			Log.i(logTag, permissions.toString().replace("[", "").replace("]", ""));

			// logout listener
			OnLogoutListener onLogoutListener = new SimpleFacebook.OnLogoutListener() {

				@Override
				public void onFail(String reason) {
					dismiss(progressDialog);
					Log.i(logTag, reason);
				}

				@Override
				public void onException(Throwable throwable) {
					dismiss(progressDialog);
					Log.i(logTag, "Bad thing happened", throwable);
				}

				@Override
				public void onThinking() {
					// show progress bar or something to the user while login is happening
					Log.i(logTag, "In progress");
				}

				@Override
				public void onLogout() {
					dismiss(progressDialog);
					Log.i(logTag, "You are logged out");
				}

			};

			// log out
			mSimpleFacebook.logout(onLogoutListener);
			
	}
	
}
