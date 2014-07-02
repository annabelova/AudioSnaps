package com.audiosnaps.adapters;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.classes.FontLoader;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.http.AcceptFriendRequest;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.json.model.User;
import com.audiosnaps.log.MyLog;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("HandlerLeak")
public class FriendsListAdapter extends BaseAdapter {

	private static String TAG = "FriendsListAdapter";
	private Context context;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private List<User> friendsList;
	private LayoutInflater inflator;
	private String id;

	// Constructor
	public FriendsListAdapter(Context context, List<User> friendsList, String id) {
		this.context = context;
		this.friendsList = friendsList;
		this.id = id;
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View row = inflator.inflate(R.layout.item_lista_amigos, null);

		// Views
		TextView userName = (TextView) row.findViewById(R.id.lblUserNameFriendList);
		TextView userRelation = (TextView) row.findViewById(R.id.lblUserRelationFriendList);
		ImageView userAvatar = (ImageView) row.findViewById(R.id.imgAvatarFriendList);
		final Button pendingBtn = (Button) row.findViewById(R.id.btnRelation);
		final ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.progressBarRelation);

		// Set fonts
		FontLoader.setRobotoFont(userName, this.context, FontLoader.MEDIUM);
		FontLoader.setRobotoFont(userRelation, this.context, FontLoader.REGULAR);

		// Load data
		try {
			//JSONObject jsonObject = jsonArrayFriends.getJSONObject(position);
			User friend = friendsList.get(position);
			if(BaseActivity.DEBUG) MyLog.d(TAG, "---- FriendJson: " + friend.toString());

			if(LoggedUser.id == id){
			
				// Following relation
				switch (friend.is_follower) {
				case HttpConnections.RELATION_IS_NOT_FOLLOWING:
					userRelation.setText(context.getResources().getString(R.string.IS_NOT_FOLLOWER));
					break;
				case HttpConnections.RELATION_IS_FOLLOWING:
					userRelation.setText(context.getResources().getString(R.string.IS_FOLLOWER));
					break;
				case HttpConnections.RELATION_IS_PENDING:
					pendingBtn.setVisibility(View.VISIBLE);
					pendingBtn.setText(context.getResources().getString(R.string.PENDING));
	
					// Listener
					pendingBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if(BaseActivity.DEBUG) MyLog.d(TAG, "Pending accept");
							User friend;
							final Handler handler = new Handler() {
								public void handleMessage(Message msg) {
	
									// Friend request ok
									if ((Boolean) msg.obj) {
										try {
											// TODO comprobar que se actualiza bien
											User friend = friendsList.get(position);
											friend.is_friend = HttpConnections.RELATION_IS_FRIEND;
											if(BaseActivity.DEBUG) MyLog.d(TAG, "JSONObject reemplazado");
										} catch (Exception e) {
											e.printStackTrace();
										}
										// userRelation.setText(context.getResources().getString(R.string.IS_FRIEND));
										progressBar.setVisibility(View.INVISIBLE);
										pendingBtn.setVisibility(View.INVISIBLE);
										pendingBtn.setClickable(false);
									}
									// Friend request error
									else {
										progressBar.setVisibility(View.INVISIBLE);
										pendingBtn.setVisibility(View.VISIBLE);
										Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
									}
								};
							};
	
							friend = friendsList.get(position);
							// jsonObject = jsonObject.getJSONObject(HttpConnections.FRIEND_KOEPICS);
							pendingBtn.setVisibility(View.INVISIBLE);
							progressBar.setVisibility(View.VISIBLE);
							if(BaseActivity.DEBUG) MyLog.d(TAG, "Aceptamos amistad de id: " + friend.koepics.id);
	
							new AcceptFriendRequest(context, handler, LoggedUser.id, friend.koepics.id, LoggedUser.koeToken).execute();
						}
					});
	
					break;
				default:
					userRelation.setText(context.getResources().getString(R.string.IS_NOT_FOLLOWER));
					break;
				}
			
			}

			// User name and avatar
			userName.setText(friend.koepics.user_name);
			imageLoader.displayImage(friend.koepics.picture_url, userAvatar, BaseActivity.optionsAvatarImage, null);

			if(LoggedUser.id == id){
			
				// Friend relation
				switch (friend.is_friend) {
				case HttpConnections.RELATION_IS_PENDING:
					pendingBtn.setVisibility(View.VISIBLE);
					pendingBtn.setText(context.getResources().getString(R.string.PENDING));
					pendingBtn.setBackgroundResource(R.drawable.notification_friend_action_2x);
					pendingBtn.setTextColor(context.getResources().getColor(R.color.negro));
					pendingBtn.setClickable(false);
					break;
				}
			
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return row;
	}

	@Override
	public int getCount() {
		return friendsList.size();
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