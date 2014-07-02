package com.audiosnaps.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.http.GetNewNotifications;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.json.model.Notification;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LoggedUserNotifications {

	protected static final String TAG = "LoggedUserNotifications";
	
	private static List<Notification> notificationsList;
	
	public static void update(Context context){
		update(context, null);
	}
	
	public static void update(Context context, final Handler handler) {
		
		final Handler notificationsHandler = new Handler() {

			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				try {
					if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
						
						Log.v(TAG, result);
						
						Gson gson = new Gson();
						Type notificationsListType = new TypeToken<List<Notification>>(){}.getType();
						notificationsList = gson.fromJson(result, notificationsListType);
						notificationsList = filterNotifications(notificationsList);
						
						if(handler != null) handler.dispatchMessage(msg);
						
						//  check if is necessary to update lists
						if(update()) LoggedUser.update();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		GetNewNotifications getNewNotifications = new GetNewNotifications(context, notificationsHandler, LoggedUser.id, BaseActivity.NOTIFICATIONS_REQUESTED, null, LoggedUser.koeToken);
		getNewNotifications.execute();
	}
	
	private static List<Notification> filterNotifications(List<Notification> notificationList) {
		ArrayList<Notification> filteredList = new ArrayList<Notification>();
		for (Notification notification : notificationList) {
			if(!(notification.type == BaseActivity.kKPNotificationCode_DBFirstFriendHarvestDone || notification.type == BaseActivity.kKPNotificationCode_DBDeletedPicture))
				filteredList.add(notification);
		}
		return filteredList;
	}
	
	public static List<Notification> getNotifications() {
		return notificationsList;
	}

	private static boolean update(){
		for(Notification notification : notificationsList) if(!notification.was_seen && update(notification)) return true;
		return false;
	}
	
	private static boolean update(Notification notification){
		switch (notification.type) {
			case BaseActivity.kKPNotificationCode_IReceiveFriendRequest:
			case BaseActivity.kKPNotificationCode_MyFriendRequestAccepted:
			case BaseActivity.kKPNotificationCode_FriendRequestAutoAccepted:
			case BaseActivity.kKPNotificationCode_IHaveNewFollower:
			case BaseActivity.kKPNotificationCode_FacebookFriendSignedIn:
			case BaseActivity.kKPNotificationCode_TwitterFriendSignedIn:
			case BaseActivity.kKPNotificationCode_DBFirstFriendHarvestDone:
				return true;
			default:
				return false;
		}
	}
}
