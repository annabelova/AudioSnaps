package com.audiosnaps.http;

import org.json.JSONArray;
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

public class UserFeed extends HttpConnections {

	private final static String TAG = "SetUserSettings";
	private Context context;
	private Handler handler;
	private int direction, numPictures;
	private String userId, koeToken, fromTimestamp, targetUserId, fromHash;
	private boolean success = false;

	// , showLoading;
	// private ProgressDialog progressDialog;
	// private Dialogos dialogos;

	public UserFeed(Context context, Handler handler, String userId, String targetUserId, String koeToken, String fromHash, String fromTimestamp, int direction, int numPictures, boolean showLoading) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.userId = userId;
		this.fromHash = fromHash;
		this.targetUserId = targetUserId;
		this.fromTimestamp = fromTimestamp;
		this.direction = direction;
		this.numPictures = numPictures;
		this.koeToken = koeToken;
		// this.showLoading = showLoading;
		// dialogos = new Dialogos(context);
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(userFeedJsonObject(userId, targetUserId, fromHash, fromTimestamp, direction, numPictures), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				JSONArray frinedsJsonArray = resultJson.getJSONArray(HttpConnections.FEED_DATA);
				success = true;
				if (frinedsJsonArray.length() > 0) {
					result = frinedsJsonArray.toString();
				} else {
					resultJson.put(HttpConnections.NO_PHOTOS, true);
					result = resultJson.toString();
				}
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
					result = sendJson(userFeedJsonObject(userId, targetUserId, fromHash, fromTimestamp, direction, numPictures), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						JSONArray frinedsJsonArray = resultJson.getJSONArray(HttpConnections.FEED_DATA);
						success = true;
						if (frinedsJsonArray.length() > 0) {
							result = frinedsJsonArray.toString();
						} else {
							resultJson.put(HttpConnections.NO_PHOTOS, true);
							result = resultJson.toString();
						}
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
		// if(showLoading){

		// progressDialog = dialogos.loadingProgressDialog();
		// }
	}

	protected void onPostExecute(String result) {
		// if(showLoading){
		// progressDialog.dismiss();
		// }
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
			Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
		}
	}
}