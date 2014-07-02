package com.audiosnaps.http;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.classes.Dialogos;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;

public class CommentPicture extends HttpConnections {

	private final static String TAG = "AddComment";
	private Context context;
	private String userId, friendsId, koeToken, comment, picHash;
	private Handler handler;
	private boolean success = false;
	private boolean sendToFb, sendToTw;
	private ProgressDialog progressDialog;
	private Dialogos dialogos;

	public CommentPicture(Context context, Handler handler, String userId, String picHash, String comment, boolean sendToFb, boolean sendToTw, String koeToken) {
		super(context);
		this.context = context;
		this.userId = userId;
		this.picHash = picHash;
		this.comment = comment;
		this.sendToFb = sendToFb;
		this.sendToTw = sendToTw;
		this.handler = handler;
		this.koeToken = koeToken;
		dialogos = new Dialogos(context);
	}

	// Do json request
	private String doRequest() {
		String result = null;
		try {
			result = sendJson(userCommentPictureJsonObject(userId, picHash, comment, sendToFb, sendToTw), null, koeToken);
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
					result = sendJson(userCommentPictureJsonObject(userId, picHash, comment, sendToFb, sendToTw), null, koeToken);
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
		return doRequest();
	}

	protected void onPreExecute() {
		progressDialog = dialogos.loadingProgressDialog();
	}

	protected void onPostExecute(String result) {
		final Message message = new Message();
		progressDialog.dismiss();
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