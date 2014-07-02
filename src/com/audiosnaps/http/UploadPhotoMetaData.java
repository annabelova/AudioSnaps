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

public class UploadPhotoMetaData extends HttpConnections {

	private final static String TAG = "UploadPhoto";
	private String userId, koeToken, uploadHash, caption, filterId;
	private boolean sendToFb, sendToTw, isPrivate;
	private double latitude, longitude;
	private Handler handler;
	private boolean success = false;
	private ProgressDialog progressDialog;
	private Dialogos dialogos;

	public UploadPhotoMetaData(Context context, Handler handler, String userId, String uploadHash, String caption, boolean sendToFb, boolean sendToTw, boolean isPrivate, String filterId,
			double latitude, double longitude, String koeToken) {
		super(context);
		this.userId = userId;
		this.uploadHash = uploadHash;
		this.caption = caption;
		this.sendToFb = sendToFb;
		this.sendToTw = sendToTw;
		this.isPrivate = isPrivate;
		this.filterId = filterId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.koeToken = koeToken;
		this.handler = handler;
		dialogos = new Dialogos(context);
	}

	// Do json request
	private String doRequest() {
		String result = null;

		try {
			result = sendJson(userUploadMetaDataPictureJsonObject(userId, uploadHash, caption, sendToFb, sendToTw, isPrivate, filterId, latitude, longitude), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				// resultJson = resultJson.getJSONObject(HttpConnections.LIKE_DATA);
				result = resultJson.toString();
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
					result = sendJson(userUploadMetaDataPictureJsonObject(userId, uploadHash, caption, sendToFb, sendToTw, isPrivate, filterId, latitude, longitude), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						// JSONArray resultJsonArray = resultJson.getJSONArray(HttpConnections.LIKE_DATA);
						result = resultJson.toString();
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
			Toast.makeText(context, context.getResources().getString(R.string.noConnection), Toast.LENGTH_SHORT).show();
		}
	}
}