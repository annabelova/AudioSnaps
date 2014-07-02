package com.audiosnaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;

import com.audiosnaps.activities.UserFeedActivity;
import com.audiosnaps.adapters.NotificationListAdapter;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.login.LoginRegisterUtil;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

	// PROJECT ID from Google API into SENDER_ID
	public static final String SENDER_ID = "614970503154";

	public static final String FROM_PUSH_NOTIFICATION = "from_push_notification";

	private JSONObject jsonObject;
	private Context contexto;

	// Constructor
	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		if (BaseActivity.DEBUG)
			MyLog.i(TAG, "onRegistered: registrationId=" + registrationId);
		LoginRegisterUtil.GMC_ID = registrationId;
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		if (BaseActivity.DEBUG)
			MyLog.i(TAG, "onUnregistered: registrationId=" + registrationId);
	}

	@Override
	protected void onMessage(Context context, Intent data) {
		String message;

		// Message from PHP server
		message = data.getStringExtra("message");
		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "---- Mensaje GMC: " + message);

		try {
			jsonObject = new JSONObject(message);

			// Show notification and wakeup device
			wakeUpDevice(context);
			createNotification(context);

		} catch (JSONException e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- GMC Notification Error ----");
		}

	}

	@Override
	protected void onError(Context arg0, String errorId) {
		if (BaseActivity.DEBUG)
			MyLog.e(TAG, "onError: errorId=" + errorId);
	}

	// Create notification
	public void createNotification(Context context) {

		String descripcion;
		try {
			// Obtenemos una referencia al servicio de notificaciones
			String notificationService = Context.NOTIFICATION_SERVICE;
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(notificationService);

			descripcion = jsonObject.getString(HttpConnections.TEMPLATE);

			// Config notificación
			Notification notification = new Notification(R.drawable.ic_launcher, descripcion, System.currentTimeMillis());

			// Configuramos el Intent
			contexto = context.getApplicationContext();

			/**** CONFIGURAR AQUI EL INTENT DE LA NOTIFICATION ****/
			Intent notificationIntent = notificationAction();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- GMC Notification type: " + jsonObject.getString(HttpConnections.NOTIFICATION_TYPE));
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- GMC Object ID: " + jsonObject.getString(HttpConnections.OBJECT_ID));

			if (notificationIntent != null) {
				notificationIntent.putExtra(FROM_PUSH_NOTIFICATION, true);
				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent pendingIntent = PendingIntent.getActivity(contexto, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				notification.setLatestEventInfo(contexto, "AudioSnaps", descripcion, pendingIntent);

				// AutoCancel: cuando se pulsa la notificaión ésta desaparece
				notification.flags |= Notification.FLAG_AUTO_CANCEL;

				// Enviar notificación
				notificationManager.notify(getId(), notification);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Error creando la notificación");
		}
	}

	private Integer getId() throws JSONException {
		return Collections.min(fromStringArray(jsonObject.getString(HttpConnections.NOTIFICATION_IDS).split(",")));
	}

	private ArrayList<Integer> fromStringArray(String[] strings){
		ArrayList<Integer> ints = new ArrayList<Integer>();
		for(int i = 0; i < strings.length; i++) ints.add(Integer.valueOf(strings[i]));
		return ints;
	}
	
	// Notification cases
	private Intent notificationAction() {

		// Restore saved user data
		LoggedUser.load(this);

		Intent intent = null;
		/*
		 * id del user viene en OBJECT_ID para NOTIFICATIONS_TYPE: 10, 20, 30,
		 * 21, 100, 110 para estas vendrá en PIC_OWNER_ID: 90, 120, 130 para 40
		 * y 50 no hay user_id ya que es mi foto mi user_id
		 */
		try {
			
			int code = Integer.valueOf(jsonObject.getString(HttpConnections.NOTIFICATION_TYPE));
			
			switch (code) {
			case BaseActivity.kKPNotificationCode_IReceiveFriendRequest:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
				break;
			case BaseActivity.kKPNotificationCode_MyFriendRequestAccepted:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
				break;
			case BaseActivity.kKPNotificationCode_IHaveNewFollower:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
				break;
			case BaseActivity.kKPNotificationCode_MyPictureIsLiked:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, LoggedUser.id);
				intent.putExtra(HttpConnections.PIC_HASH, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.ONE_PICTURE_FEED);
				break;
			case BaseActivity.kKPNotificationCode_MyPictureIsCommented:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, LoggedUser.id);
				intent.putExtra(HttpConnections.PIC_HASH, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_TOP);
				break;
			case BaseActivity.kKPNotificationCode_PictureICommentedIsCommented:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, jsonObject.getString(HttpConnections.PIC_OWNER_ID));
				intent.putExtra(HttpConnections.PIC_HASH, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_TOP);
				break;
			case BaseActivity.kKPNotificationCode_FacebookFriendSignedIn:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
				break;
			case BaseActivity.kKPNotificationCode_TwitterFriendSignedIn:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
				break;
			case BaseActivity.kKPNotificationCode_YouWhereTagged:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, jsonObject.getString(HttpConnections.PIC_OWNER_ID));
				intent.putExtra(HttpConnections.PIC_HASH, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.ONE_PICTURE_FEED);
				break;
			case BaseActivity.kKPNotificationCode_YouWhereTaggedInComment:
				/*** OK ***/
				intent = new Intent(contexto, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, jsonObject.getString(HttpConnections.PIC_OWNER_ID));
				intent.putExtra(HttpConnections.PIC_HASH, jsonObject.getString(HttpConnections.OBJECT_ID));
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_TOP);
				break;
			case BaseActivity.kKPNotificationCode_DBFirstFriendHarvestDone:
			case BaseActivity.kKPNotificationCode_DBDeletedPicture:
			case BaseActivity.kKPNotificationCode_FriendRequestAutoAccepted:
				break;
			}
			
			if(code >= 500 || intent == null){
				if(jsonObject.getInt(HttpConnections.OPEN_URL) == 1){
					intent = new Intent(contexto, WebviewActivity.class);
					intent.putExtra("url", NotificationListAdapter.getCustomUrlFromGCM(jsonObject));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Error en notification action");
		}

		// Comprobamos si estamos logueados
		LoginRegisterUtil loginRegisterUtil = new LoginRegisterUtil(this);
		if (!loginRegisterUtil.isLogged(this)) {
			intent = new Intent(this, WelcomeActivity.class);
		}

		return intent;
	}

	// WakeUp device from sleep
	private void wakeUpDevice(Context context) {
		// Wake Android Device when notification received
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		final PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "GCM_PUSH");
		mWakelock.acquire();

		// Timer before putting Android Device to sleep mode.
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				mWakelock.release();
			}
		};
		timer.schedule(task, 5000);
	}
}