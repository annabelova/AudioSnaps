package com.audiosnaps.http;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.audiosnaps.log.MyLog;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.classes.Dialogos;

public class Login extends HttpConnections {

	private final static String TAG = "Login";
	private Context context;
	private Handler handler;
	private String email, userName, password, network, permissions, access_token, access_token_secret, apns_token;
	private boolean success = false;
	private ProgressDialog progressDialog;
	private Dialogos dialogos;

	public Login(Context context, Handler handler, String email, String userName, String password, String network, String permissions, String access_token, String access_token_secret,
			String apns_token) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.email = email;
		this.userName = userName;
		this.password = password;
		this.network = network;
		this.permissions = permissions;
		this.access_token = access_token;
		this.access_token_secret = access_token_secret;
		this.apns_token = apns_token;
		dialogos = new Dialogos(context);
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(loginJsonObject(email, userName, password, network, permissions, access_token, access_token_secret, apns_token), null, null);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				JSONObject userDataJson = resultJson.getJSONObject(HttpConnections.USER_DATA);
				result = userDataJson.toString();
				success = true;
			}
			// error, retry with new koe_token
			else {
				result = null;
			}
		} catch (Exception e) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Exception json request");
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
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request success");
			message.obj = result;
			handler.dispatchMessage(message);
		}
		// error
		else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request error");
			message.obj = ERROR;
			handler.dispatchMessage(message);
			Toast.makeText(context, context.getResources().getString(R.string.loginError), Toast.LENGTH_SHORT).show();
		}
	}
}