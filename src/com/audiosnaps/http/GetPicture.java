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

public class GetPicture extends HttpConnections {

	private final static String TAG = "GetPicture";
	private Context context;
	private Handler handler;
	private String userId, koeToken, picHash;
	private boolean success = false, showLoading;
	private ProgressDialog progressDialog;
	private Dialogos dialogos;

	public GetPicture(Context context, Handler handler, String userId, String picHash, String koeToken, boolean showLoading) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.userId = userId;
		this.picHash = picHash;
		this.koeToken = koeToken;
		this.showLoading = showLoading;
		dialogos = new Dialogos(context);
	}
	
	
	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(userGetPictureJsonObject(userId, picHash), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				Log.v(TAG, resultJson.toString());
				resultJson = resultJson.getJSONObject(HttpConnections.PIC_DATA);
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
					result = sendJson(userGetPictureJsonObject(userId, picHash), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						resultJson = resultJson.getJSONObject(HttpConnections.PIC_DATA);
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
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
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