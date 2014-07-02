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

public class AcceptFriendRequest extends HttpConnections {

	private final static String TAG = "AcceptFriendRequest";
	private Context context;
	private String userId, followerId, koeToken;
	private Handler handler;
	private boolean success = false;

	public AcceptFriendRequest(Context context, Handler handler, String userId, String followerId, String koeToken) {
		super(context);
		this.context = context;
		this.userId = userId;
		this.followerId = followerId;
		this.handler = handler;
		this.koeToken = koeToken;
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(userAcceptdFriendRequestJsonObject(userId, followerId), null, koeToken);
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
				if(BaseActivity.DEBUG) MyLog.i(TAG, "Retry request with new koe_token from meta: " + koeToken);
				if (koeToken.length() > 0) {
					LoggedUser.koeToken = koeToken;
					LoggedUser.save(context);
					result = sendJson(userAcceptdFriendRequestJsonObject(userId, followerId), null, koeToken);
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
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		return doRequest();
	}

	protected void onPreExecute() {
	}

	protected void onPostExecute(String result) {
		final Message message = new Message();
		// success
		if (success) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request success");
			message.obj = true;
			handler.dispatchMessage(message);
		}
		// error
		else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request error");
			message.obj = false;
			handler.dispatchMessage(message);
			Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
		}
	}
}