package com.audiosnaps.http;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;

public class NotificationClicked extends HttpConnections {

	private final static String TAG = "NotificationClicked";
	private String timestamp, userId, notificationId, koeToken;
	private boolean success = false;

	public NotificationClicked(Context context, Handler handler, String userId, String notificationId, String timestamp, String koeToken) {
		super(context);
		this.userId = userId;
		this.timestamp = timestamp;
		this.notificationId = notificationId;
		this.koeToken = koeToken;
	}

	// Do json request
	private String doRequest() {
		String result = null;

		try {
			result = sendJson(userNotificationClickedJsonObject(userId, notificationId, timestamp), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				success = true;
			}
			// error, retry with new koe_token
			else {
				Log.v(TAG, result);
				result = null;
				if(resultJson.has(META)){
					resultJson = resultJson.getJSONObject(META);
					koeToken = resultJson.getString(KOE_TOKEN);
				}
				if(BaseActivity.DEBUG) MyLog.i(TAG, "Retry request with new koe_token from meta: " + koeToken);
				if (koeToken.length() > 0) {
					LoggedUser.koeToken = koeToken;
					LoggedUser.save(context);
					result = sendJson(userNotificationClickedJsonObject(userId, notificationId, timestamp), null, koeToken);
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
//		final Message message = new Message();
		// success
		if (success) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "---- Notification clicked success ----");
			// message.obj = result;
			// handler.dispatchMessage(message);
		}
		// error
		else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request error");
			// message.obj = ERROR;
			// handler.dispatchMessage(message);
			// Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
		}
	}
}