package com.audiosnaps.http;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;

public class UnblockUser extends HttpConnections {

	private final static String TAG = "UnblockUser";
	private Context context;
	private String userId, unblockId, koeToken;
	private boolean success = false;
	private Handler handler;
	
	public UnblockUser(Context context, String userId, String unblockId, String koeToken, Handler handler) {
		super(context);
		this.context = context;
		this.userId = userId;
		this.unblockId = unblockId;
		this.koeToken = koeToken;
		this.handler = handler;
	}
	
	@Override
	protected String doInBackground(Object... params) {
		return doRequest();
	}

	private String doRequest() {
		String result = null;
		
		try {
			result = sendJson(unblockUserJsonObject(userId, unblockId), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				success = true;
				result = resultJson.getString(UNBLOCKED_USER);
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
					result = sendJson(blockUserJsonObject(userId, unblockId), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						success = true;
						result = resultJson.getString(UNBLOCKED_USER);
					}
					// error, network error
					else {
						result = HttpConnections.ERROR;
					}
				}
			}
		} catch (Exception e) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Exception json request");
			e.printStackTrace();
		}

		return result;
	}
	
	protected void onPostExecute(String result) {
		final Message message = new Message();
		message.obj = result;
		// success
		if (success) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request success");
			handler.dispatchMessage(message);
		}
		// error
		else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request error");
			Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
		}
	}
}
