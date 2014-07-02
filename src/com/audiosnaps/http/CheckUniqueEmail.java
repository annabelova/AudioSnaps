package com.audiosnaps.http;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;

public class CheckUniqueEmail extends HttpConnections {

	private final static String TAG = "CheckUniqueEmail";
	private Context context;
	private Handler handler;
	private String email, userId, koeToken;
	private boolean success = false;

	public CheckUniqueEmail(Context context, Handler handler, String userId, String koeToken, String email) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.email = email;
		this.userId = userId;
		this.koeToken = koeToken;
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(userCheckUniqueEmailJsonObject(userId, email), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				result = resultJson.toString();
				success = true;
			} else {
				if(koeToken != null){
					result = null;
					resultJson = resultJson.getJSONObject(META);
					koeToken = resultJson.getString(KOE_TOKEN);
					if(BaseActivity.DEBUG) MyLog.i(TAG, "Retry request with new koe_token from meta: " + koeToken);
					if (koeToken.length() > 0) {
						LoggedUser.koeToken = koeToken;
						LoggedUser.save(context);
						result = sendJson(userCheckUniqueEmailJsonObject(userId, email), null, koeToken);
						resultJson = new JSONObject(result);
						// success
						if (resultJson.getBoolean(SUCCES)) {
							result = resultJson.toString();
							success = true;
						}
						// error, network error
						else {
							result = null;
						}
					}
				}
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
			Toast.makeText(context, context.getResources().getString(R.string.emailInUse), Toast.LENGTH_SHORT).show();
		}
	}
}