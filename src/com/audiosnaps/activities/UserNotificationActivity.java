package com.audiosnaps.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.WebviewActivity;
import com.audiosnaps.adapters.NotificationListAdapter;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.data.LoggedUserNotifications;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.NotificationClicked;
import com.audiosnaps.json.model.Notification;
import com.audiosnaps.log.MyLog;

public class UserNotificationActivity extends Activity {

	private final String TAG = "UserNotificationActivity";
	private NotificationListAdapter notificationListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_notifications);
		MyLog.d(TAG, "UserNotificationActivity");
		initUserNotifications();
	}
	
	private Activity getActivity()
	{
		return this;
	}

	// Inicializa notificaciones usuario
	private void initUserNotifications() {
		ListView listViewNotifications = (ListView) findViewById(R.id.listViewNotifications2);
		notificationListAdapter = new NotificationListAdapter(this);
		listViewNotifications.setAdapter(notificationListAdapter);

		// List items listeners
		listViewNotifications.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				if (BaseActivity.DEBUG)
					MyLog.d(TAG,
							"Pulsado item listViewNotifications position: "
									+ position);

				Notification notification = LoggedUserNotifications
						.getNotifications().get(position);
				notificationClicked(notification.notification_id);
				notificationAction(notification.type, notification);
			}
		});

	}

	// Notification cases
	private void notificationAction(int code, Notification notification) {
		try {
 
			Intent intent = null;

			switch (code) {
			case BaseActivity.kKPNotificationCode_IReceiveFriendRequest:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_MyFriendRequestAccepted:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_FriendRequestAutoAccepted:
				// Nada
				break;
			case BaseActivity.kKPNotificationCode_IHaveNewFollower:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_MyPictureIsLiked:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.receiver.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_MyPictureIsCommented:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.receiver.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_TOP);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_PictureICommentedIsCommented:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_TOP);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_FacebookFriendSignedIn:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_TwitterFriendSignedIn:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_YouWhereTagged:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_YouWhereTaggedInComment:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_TOP);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_DBFirstFriendHarvestDone:
				break;
			case BaseActivity.kKPNotificationCode_DBDeletedPicture:
				break;
			}

			if (code >= 500 || intent == null) {
				if (notification.open_url) {
					intent = new Intent(getActivity(), WebviewActivity.class);
					intent.putExtra("url",
							NotificationListAdapter.getCustomUrl(notification));
					getActivity().startActivity(intent);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Error en notification action");
		}
	}

	// Marca notification as clicked
	private void notificationClicked(String notificationId) {
		NotificationClicked notificationClicked = new NotificationClicked(
				getActivity(), null, LoggedUser.id, notificationId,
				String.valueOf(System.currentTimeMillis() / 1000L),
				LoggedUser.koeToken);
		notificationClicked.execute();
	}
}
