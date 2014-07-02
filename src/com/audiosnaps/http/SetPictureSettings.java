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

public class SetPictureSettings extends HttpConnections{

	private final static String TAG = "SetPictureSettings";
	private Context context;
	private String userId,picHash, koeToken;
	private boolean privatePic, deletePic, showLoading;
	private boolean success = false;
	private Dialogos dialogos;
	private Handler  handler;
	private ProgressDialog progressDialog;
	
	public SetPictureSettings(Context context, String userId, String picHash, boolean privatePic, boolean deletePic, String koeToken, Handler handler, boolean showLoading) {
		super(context);
		this.context = context;
		this.userId = userId;
		this.privatePic = privatePic;
		this.deletePic = deletePic;
		this.koeToken = koeToken;
		this.picHash = picHash;
		this.handler = handler;
		this.showLoading = showLoading;
		dialogos = new Dialogos(context);
	}

	@Override
	protected String doInBackground(Object... params) {
		return doRequest();
	}

	private String doRequest() {
		String result = null;
		
		try {
			result = sendJson(setPictureSettingsJsonObject(userId, picHash, privatePic, deletePic), null, koeToken);
			JSONObject resultJson;
			resultJson = new JSONObject(result);

			Log.v(TAG, result);
			
			// success
			if (resultJson.getBoolean(HttpConnections.SUCCES)) {
				success = true;
				result = resultJson.getString(PIC_HASH);
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
					result = sendJson(setPictureSettingsJsonObject(userId, picHash, privatePic, deletePic), null, koeToken);
					resultJson = new JSONObject(result);
					// success
					if (resultJson.getBoolean(SUCCES)) {
						success = true;
						result = resultJson.getString(PIC_HASH);
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
		message.obj = result;
		// success
		if (success) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request success");
			handler.dispatchMessage(message);
		}
		// error
		else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request error");
			//handler.dispatchMessage(message);
			Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
		}
	}
	
}
