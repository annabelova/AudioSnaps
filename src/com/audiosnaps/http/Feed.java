package com.audiosnaps.http;

import org.json.JSONArray;
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

public class Feed extends HttpConnections {

	private final static String TAG = "SetUserSettings";
	private Context context;
	private Handler handler;
	private int fromHash, direction, numPictures;
	private String userId, koeToken, fromTimestamp;
	private boolean success = false;
//			, showLoading;
//	private ProgressDialog progressDialog;
//	private Dialogos dialogos;

	public Feed(Context context, Handler handler, String userId, String koeToken, int fromHash, String fromTimestamp, int direction, int numPictures, boolean showLoading) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.userId = userId;
		this.fromHash = fromHash;
		this.fromTimestamp = fromTimestamp;
		this.direction = direction;
		this.numPictures = numPictures;
		this.koeToken = koeToken;
//		this.showLoading = showLoading;
//		dialogos = new Dialogos(context);
	}
	
	
	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(feedJsonObject(userId, fromHash, fromTimestamp, direction, numPictures), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				JSONArray frinedsJsonArray = resultJson.getJSONArray(HttpConnections.FEED_DATA);
				result = frinedsJsonArray.toString();
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
					result = sendJson(feedJsonObject(userId, fromHash, fromTimestamp, direction, numPictures), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						JSONArray frinedsJsonArray = resultJson.getJSONArray(HttpConnections.FEED_DATA);
						result = frinedsJsonArray.toString();
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
//		if(showLoading){
//			progressDialog = dialogos.loadingProgressDialog();
//		}
	}

	protected void onPostExecute(String result) {
//		if(showLoading){
//			progressDialog.dismiss();
//		}
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