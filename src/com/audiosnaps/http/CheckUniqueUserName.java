package com.audiosnaps.http;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.audiosnaps.log.MyLog;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;

public class CheckUniqueUserName extends HttpConnections {

	private final static String TAG = "CheckUniqueUserName";
	private Context context;
	private Handler handler;
	private String userName, koeToken, userId;
	private boolean success = false;

	public CheckUniqueUserName(Context context, Handler handler, String userId, String koeToken, String userName) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.userName = userName;
		this.koeToken = koeToken;
		this.userId = userId;
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(userCheckUniqueUserNameJsonObject(userId, userName), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				success = true;
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
	}

	protected void onPostExecute(String result) {
		final Message message = new Message();
		if (success) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Enviamos result Json al handler");
			message.obj = true;
			handler.dispatchMessage(message);
		} else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "result = null");
			message.obj = false;
			handler.dispatchMessage(message);
			Toast.makeText(context, context.getResources().getString(R.string.NOT_UNIQUE_USERNAME), Toast.LENGTH_SHORT).show();
		}
	}
}