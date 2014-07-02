package com.audiosnaps.http;

import java.io.File;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;

public class UploadPhoto extends HttpConnections {

	private final static String TAG = "UploadPhoto";
	private String userId, koeToken;
//	private Handler handler;
	private boolean success = false;
	private File photo;
//	private ProgressDialog progressDialog;
//	private Dialogos dialogos;

	public UploadPhoto(Context context, Handler handler, String userId, File photo, String koeToken) {
		super(context);
		this.userId = userId;
		this.koeToken = koeToken;
//		this.handler = handler;
		this.photo = photo;
//		dialogos = new Dialogos(context);
	}

	// Do json request
	private String doRequest() {
		String result = null;

		try {
			result = sendJson(userUploadPictureJsonObject(userId), photo, koeToken);
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
					result = sendJson(userUploadPictureJsonObject(userId), photo, koeToken);
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
		//progressDialog = dialogos.loadingProgressDialog();
	}

	protected void onPostExecute(String result) {
		//progressDialog.dismiss();
		final Message message = new Message();
		// success
		if (success) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request success");
			message.obj = result;
			//handler.dispatchMessage(message);
		}
		// error
		else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Request error");
			message.obj = ERROR;
			//handler.dispatchMessage(message);
			Toast.makeText(context, context.getResources().getString(R.string.noConnection), Toast.LENGTH_SHORT).show();
		}
		
		SharedPreferences prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		if(!prefs.getBoolean(BaseActivity.SAVE_IN_LIBRARY, true))
			photo.delete();
		else {
			// actualizar galeria
			//context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			MediaScannerConnection.scanFile(context, new String[] {
					photo.getAbsolutePath()},				
					null,
					new MediaScannerConnection.OnScanCompletedListener() {						
						public void onScanCompleted(String path, Uri uri){}			
					}
			);
		}
	}
}