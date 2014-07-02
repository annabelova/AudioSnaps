package com.audiosnaps.adapters;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
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
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.SendFriendRequests;
import com.audiosnaps.json.model.User;
import com.audiosnaps.log.MyLog;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("HandlerLeak")
public class FollowersListAdapter extends BaseAdapter {

	private static String TAG = "FriendsListAdapter";
	private Context context;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private List<User> followersList;
	private LayoutInflater inflator;
	private String id;

	// Constructor
	public FollowersListAdapter(Context context, List<User> followersList, String id) {
		this.context = context;
		this.followersList = followersList;
		this.id = id;
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View row = inflator.inflate(R.layout.item_lista_amigos, null);

		// Views
		TextView userName = (TextView) row.findViewById(R.id.lblUserNameFriendList);
		final TextView userRelation = (TextView) row.findViewById(R.id.lblUserRelationFriendList);
		ImageView userAvatar = (ImageView) row.findViewById(R.id.imgAvatarFriendList);
		final Button btnRelation = (Button) row.findViewById(R.id.btnRelation);
		final ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.progressBarRelation);

		// Set fonts
		FontLoader.setRobotoFont(userName, this.context, FontLoader.MEDIUM);
		FontLoader.setRobotoFont(userRelation, this.context, FontLoader.REGULAR);

		// Load data
		try {
			User follower = followersList.get(position);

			if(LoggedUser.id == id){
			
				// Friend-Follower relation
				// si es amigo is_friend=1 es que yo ya le estoy siguiendo
				// me pueden estar siguiendo pero yo no los he agregado aún como
				// amigos
				switch (follower.is_friend) {
					case HttpConnections.RELATION_IS_NOT_FRIEND:
						btnRelation.setVisibility(View.VISIBLE);
						btnRelation.setText(context.getResources().getString(R.string.ADD_FRIEND));
						userRelation.setText(context.getResources().getString(R.string.IS_NOT_FRIEND));
						break;
					case HttpConnections.RELATION_IS_FRIEND:
						btnRelation.setVisibility(View.GONE);
						userRelation.setText(context.getResources().getString(R.string.IS_FRIEND));
						break;
					case HttpConnections.RELATION_IS_PENDING:
						btnRelation.setVisibility(View.VISIBLE);
						btnRelation.setBackgroundResource(R.drawable.button_accept_selector);
						btnRelation.setText(context.getResources().getString(R.string.ACCEPT));
						break;
					default:
						btnRelation.setVisibility(View.GONE);
						userRelation.setText(context.getResources().getString(R.string.IS_FRIEND));
						break;
				}
			
			}

			// Friend data
			userName.setText(follower.koepics.user_name);

			// User avatar
			imageLoader.displayImage(follower.koepics.picture_url, userAvatar, BaseActivity.optionsAvatarImage, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Listener
		btnRelation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Amistad solicitada");
				User follower;
				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {

						// Friend request ok
						if ((Boolean) msg.obj) {
							try {
								User follower = followersList.get(position);
								follower.is_friend = HttpConnections.RELATION_IS_FRIEND;
								if(BaseActivity.DEBUG) MyLog.d(TAG, "JSONObject reemplazado");
							} catch (Exception e) {
								e.printStackTrace();
							}
							userRelation.setText(context.getResources().getString(R.string.IS_FRIEND));
							progressBar.setVisibility(View.INVISIBLE);
							btnRelation.setVisibility(View.INVISIBLE);
							btnRelation.setClickable(false);
						}
						// Friend request error
						else {
							progressBar.setVisibility(View.INVISIBLE);
							btnRelation.setVisibility(View.VISIBLE);
							Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
						}
					};
				};

				follower = followersList.get(position);
				btnRelation.setVisibility(View.INVISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Solicitamos amistad a id: " + follower.koepics.id);

				new SendFriendRequests(context, handler, LoggedUser.id, follower.koepics.id, LoggedUser.koeToken).execute();
			}
		});

		return row;
	}

	@Override
	public int getCount() {
		return followersList.size();
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