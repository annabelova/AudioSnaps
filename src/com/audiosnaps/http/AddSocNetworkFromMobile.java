package com.audiosnaps.http;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.classes.Dialogos;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;

public class AddSocNetworkFromMobile extends HttpConnections {

	private final static String TAG = "AddSocNetworkFromMobile";
	private Context context;
	private Handler handler;
	private String network, permissions, access_token, access_token_secret, koeToken, userId;
	private boolean success = false;
	private ProgressDialog progressDialog;
	private Dialogos dialogos;

	public AddSocNetworkFromMobile(Context context, Handler handler, String userId, String network, String permissions, String access_token,
			String access_token_secret, String koeToken) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.userId = userId;
		this.network = network;
		this.permissions = permissions;
		this.access_token = access_token;
		this.access_token_secret = access_token_secret;
		this.koeToken = koeToken;
		dialogos = new Dialogos(context);
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(addSocNetworkFromMobileJsonObject(userId, network, permissions, access_token, access_token_secret), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);
			Log.d(TAG, "---- DATA: " + resultJson);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				JSONObject userDataJson = resultJson.getJSONObject(HttpConnections.USER_DATA);
				result = userDataJson.toString();
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
					result = sendJson(addSocNetworkFromMobileJsonObject(userId, network, permissions, access_token, access_token_secret), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(HttpConnections.SUCCES)) {
						JSONObject userDataJson = resultJson.getJSONObject(HttpConnections.USER_DATA);
						result = userDataJson.toString();
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
			message.obj = result;
			if(permissions != null) if(permissions.contains("publish_actions")) message.arg1 = BaseActivity.FACEBOOK_HAS_PUBLISH_ACTIONS;
			handler.dispatchMessage(message);
		}
		// error
		else {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Request error");
			message.obj = ERROR;
			handler.dispatchMessage(message);
			Toast.makeText(context, context.getResources().getString(R.string.ERROR_TITLE_GENERIC), Toast.LENGTH_SHORT).show();
		}
	}
}