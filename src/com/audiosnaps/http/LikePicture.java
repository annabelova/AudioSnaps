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

public class LikePicture extends HttpConnections {

	private final static String TAG = "LikePicture";
	private String picHash, userId, koeToken;
	private Handler handler;
	private boolean success = false;
	private int direction;
//	private ProgressDialog progressDialog;
//	private Dialogos dialogos;

	public LikePicture(Context context, Handler handler, String userId, String koeToken, String picHash, int direction) {
		super(context);
		this.userId = userId;
		this.picHash = picHash;
		this.handler = handler;
		this.direction = direction;
		this.koeToken = koeToken;
//		dialogos = new Dialogos(context);
	}
	
	// Do json request
	private String doRequest() {
		String result = null;
		
		try {
			result = sendJson(userLikePictureJsonObject(userId, picHash, direction), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				resultJson = resultJson.getJSONObject(HttpConnections.LIKE_DATA);
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
					result = sendJson(userLikePictureJsonObject(userId, picHash, direction), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						JSONArray resultJsonArray = resultJson.getJSONArray(HttpConnections.LIKE_DATA);
						result = resultJsonArray.toString();
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
		//progressDialog = dialogos.loadingProgressDialog();
	}

	protected void onPostExecute(String result) {
		//progressDialog.dismiss();
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