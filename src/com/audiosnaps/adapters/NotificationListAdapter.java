package com.audiosnaps.adapters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.audiosnap.library.util.DateUtil;
import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.data.LoggedUserNotifications;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.json.model.Notification;
import com.audiosnaps.log.MyLog;
import com.nostra13.universalimageloader.core.ImageLoader;

public class NotificationListAdapter extends BaseAdapter {

	private static final String BASE_URL = "http://audiosnaps.com/cun/";
	private static String TAG = "NotificationListAdapter";
	private Context context;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private LayoutInflater inflator;

	// Constructor
	public NotificationListAdapter(Context context) {
		this.context = context;
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public static String getCustomUrlFromGCM(JSONObject jsonObject) throws JSONException, UnsupportedEncodingException {
		return BASE_URL + jsonObject.getString(HttpConnections.OBJECT_ID) + "/" + jsonObject.getString(HttpConnections.USER_RECEIVES_ID) + "/" + encodeIds(jsonObject.getString(HttpConnections.NOTIFICATION_IDS));
	}

	public static String getCustomUrl(Notification notification) throws UnsupportedEncodingException {
		return BASE_URL + notification.pic_hash + "/" + notification.receiver.user_id + "/" + encodeIds(arrayToString(notification.notification_id_array));
	}

	private static String arrayToString(String[] ids) {
		StringBuilder str = new StringBuilder();
		int i = 0;
		for (String id : Arrays.asList(ids)) {
			str.append(id);
			if (i != ids.length) str.append(",");
			i++;
		}
		return ids.toString();
	}

	private static String encodeIds(String ids) throws UnsupportedEncodingException {
		return URLEncoder.encode(Base64.encodeToString(ids.getBytes(), Base64.DEFAULT), "UTF-8");
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View row = inflator.inflate(R.layout.item_lista_notifications, null);

		// Views
		TextView lblNotification = (TextView) row.findViewById(R.id.lblNotification);
		TextView lblTimeNotification = (TextView) row.findViewById(R.id.lblTimeNotification);
		ImageView imgAvatarNotification = (ImageView) row.findViewById(R.id.imgAvatarNotification);

		// Load data
		try {
			Notification notification = LoggedUserNotifications.getNotifications().get(position);

			// Friend data
			lblNotification.setText(formatTextnotification(notification.type, notification));
			lblTimeNotification.setText(DateUtil.formatTimeAgo(notification.date, this.context));
			String thumbnail = notification.thumbnail;
			// jsonObject =
			// jsonObject.getJSONObject(HttpConnections.FRIEND_DATA);

			// User avatar
			if (thumbnail != null) {
				imageLoader.displayImage(thumbnail, imgAvatarNotification, BaseActivity.optionsAvatarImage, null);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return row;
	}

	private String formatTextnotification(int code, Notification notification) {

		String text = "";

		try {

			if (notification.text != null) {
				text = notification.text;
			}

			if (text.length() == 0) {

				switch (code) {
					case BaseActivity.kKPNotificationCode_IReceiveFriendRequest:
						text = context.getString(R.string.Notification_IReceiveFriendRequest, notification.maker.user_name);
						break;
					case BaseActivity.kKPNotificationCode_MyFriendRequestAccepted:
						text = context.getString(R.string.Notification_MyFriendRequestAccepted, notification.maker.user_name);
						break;
					case BaseActivity.kKPNotificationCode_FriendRequestAutoAccepted:
						text = context.getString(R.string.Notification_FriendRequestAutoAccepted, notification.maker.user_name);
						break;
					case BaseActivity.kKPNotificationCode_IHaveNewFollower:
						text = context.getString(R.string.Notification_IHaveNewFollower, notification.maker.user_name);
						break;
					case BaseActivity.kKPNotificationCode_MyPictureIsLiked:
						switch (notification.quantity) {
						case 1:
							text = context.getString(R.string.Notification_MyPictureIsLiked, notification.maker.user_name);
							break;
						case 2:
							text = context.getString(R.string.Notification_MyPictureIsLiked2, notification.maker.user_name);
							break;
	
						default:
							text = context.getString(R.string.Notification_MyPictureIsLikedP, notification.maker.user_name, String.valueOf(notification.quantity - 1));
							break;
						}
						break;
					case BaseActivity.kKPNotificationCode_MyPictureIsCommented:
						switch (notification.quantity) {
						case 1:
							text = context.getString(R.string.Notification_MyPictureIsCommented, notification.maker.user_name);
							break;
						case 2:
							text = context.getString(R.string.Notification_MyPictureIsCommented2, notification.maker.user_name);
							break;
	
						default:
							text = context.getString(R.string.Notification_MyPictureIsCommentedP, notification.maker.user_name, String.valueOf(notification.quantity - 1));
							break;
						}
						break;
					case BaseActivity.kKPNotificationCode_PictureICommentedIsCommented:
						switch (notification.quantity) {
						case 1:
							text = context.getString(R.string.Notification_PictureICommentedIsCommented, notification.maker.user_name);
							break;
						case 2:
							text = context.getString(R.string.Notification_PictureICommentedIsCommented2, notification.maker.user_name);
							break;
	
						default:
							text = context.getString(R.string.Notification_PictureICommentedIsCommentedP, notification.maker.user_name, String.valueOf(notification.quantity - 1));
							break;
						}
						break;
					case BaseActivity.kKPNotificationCode_FacebookFriendSignedIn:
						text = context.getString(R.string.Notification_FacebookFriendSignedIn, notification.maker.user_name);
						break;
					case BaseActivity.kKPNotificationCode_TwitterFriendSignedIn:
						text = context.getString(R.string.Notification_TwitterFriendSignedIn, notification.maker.user_name);
						break;
					case BaseActivity.kKPNotificationCode_YouWhereTagged:
						text = context.getString(R.string.Notification_YouWhereTagged, notification.maker.user_name);
						break;
					case BaseActivity.kKPNotificationCode_YouWhereTaggedInComment:
						text = context.getString(R.string.Notification_YouWhereTaggedInComment, notification.maker.user_name);
						break;
					case BaseActivity.kKPNotificationCode_DBFirstFriendHarvestDone:
					case BaseActivity.kKPNotificationCode_DBDeletedPicture:
					default:
						text = context.getResources().getString(R.string.Notification_RequiresUserAction);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Error en creación notificación!");
		}
		return text;
	}

	@Override
	public int getCount() {
		return LoggedUserNotifications.getNotifications().size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}