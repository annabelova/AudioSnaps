package com.audiosnaps.http;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;
import com.audiosnaps.classes.Dialogos;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;

public class SetUserSettings extends HttpConnections {

	private final static String TAG = "SetUserSettings";
	private Handler handler;
	private String userId, koeToken, caption, privacy, sharesLikesFacebook, sharesLikesTwitter, email, userName;
	int notificationsInFacebook, notificationsInMyPhone, notificationsByEmail;
	private boolean success = false;
	private ProgressDialog progressDialog;
	private Dialogos dialogos;

	// Constructor
	public SetUserSettings(Context context, Handler handler, String userId, String koeToken, String caption, String privacy, String sharesLikesFacebook, String sharesLikesTwitter,
			int notificationsInFacebook, int notificationsInMyPhone, int notificationsByEmail, String email, String userName) {
		super(context);
		this.userId = userId;
		this.koeToken = koeToken;
		this.handler = handler;
		this.caption = caption;
		this.privacy = privacy;
		this.sharesLikesFacebook = sharesLikesFacebook;
		this.sharesLikesTwitter = sharesLikesTwitter;
		this.notificationsInFacebook = notificationsInFacebook;
		this.notificationsInMyPhone = notificationsInMyPhone;
		this.notificationsByEmail = notificationsByEmail;
		this.email = email;
		this.userName = userName;
		dialogos = new Dialogos(context);
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(
					userSetSettingsJsonObject(userId, caption, privacy, sharesLikesFacebook, sharesLikesTwitter, notificationsInFacebook, notificationsInMyPhone, notificationsByEmail, email, userName),
					null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				success = true;
			}
			// error, retry with new koe_token
			else {
				result = null;
				resultJson = resultJson.getJSONObject(META);
				koeToken = resultJson.getString(KOE_TOKEN);
				if (BaseActivity.DEBUG)
					MyLog.i(TAG, "Retry request with new koe_token from meta: " + koeToken);
				if (koeToken.length() > 0) {
					LoggedUser.koeToken = koeToken;
					LoggedUser.save(context);
					result = sendJson(
							userSetSettingsJsonObject(userId, caption, privacy, sharesLikesFacebook, sharesLikesTwitter, notificationsInFacebook, notificationsInMyPhone, notificationsByEmail, email,
									userName), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						success = true;
					}
					// error, network error
					else {
						result = null;
					}
				}
			}
		} catch (Exception e) {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Exception json request");
			e.printStackTrace();
		}

		return result;
	}

	// AsyncTask background work
	@Override
	protected String doInBackground(Object... params) {
		return doRequest();
	}

	protected void onPreExecute() {
		progressDialog = dialogos.loadingProgressDialog();
	}

	protected void onPostExecute(String result) {
		progressDialog.dismiss();
		final Message message = new Message();
		// success
		if (success) {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Request success");
			message.obj = true;
			handler.dispatchMessage(message);
		}
		// error
		else {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Request error");
			message.obj = false;
			handler.dispatchMessage(message);
			Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
		}
	}
}