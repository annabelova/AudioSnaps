package com.audiosnaps.http;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.classes.Dialogos;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;

public class SharePictureInSoc extends HttpConnections {

	private final static String TAG = "SharePictureInSoc";
	private String userId, picHash, koeToken, network;
	private boolean showLoading, success = false;
	private ProgressDialog progressDialog;
	private Dialogos dialogos;
	private Handler handler;

	// Constructor
	public SharePictureInSoc(Context context, String userId, String koeToken, String picHash, String network, Handler handler) {
		super(context);
		this.userId = userId;
		this.picHash = picHash;
		this.koeToken = koeToken;
		this.network = network;
		this.handler = handler;
		dialogos = new Dialogos(context);
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(sharePicInSocJsonObject(userId, picHash, network), null, koeToken);
			JSONObject resultJson;
			Log.v(TAG, result);
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
				if (BaseActivity.DEBUG)
					MyLog.i(TAG, "Retry request with new koe_token from meta: " + koeToken);
				if (koeToken.length() > 0) {
					LoggedUser.koeToken = koeToken;
					LoggedUser.save(context);
					result = sendJson(sharePicInSocJsonObject(userId, picHash, network), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						success = true;
					}
					// error, network error
					else {
						result = HttpConnections.ERROR;
					}
				}
			}
		} catch (Exception e) {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Exception json request");
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
		if(showLoading){
			progressDialog = dialogos.loadingProgressDialog();
		}
	}

	protected void onPostExecute(String result) {
		if(showLoading){
			progressDialog.dismiss();
		}
		final Message message = new Message();
		// success
		if (success) {
			if (BaseActivity.DEBUG) MyLog.d(TAG, "Request success");
			message.obj = result;
			handler.dispatchMessage(message);
		}
		// error
		else {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Request error");
			message.obj = result;
			Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
		}
	}
	
}
