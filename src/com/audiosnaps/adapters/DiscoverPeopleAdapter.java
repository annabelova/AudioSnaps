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
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.SendFriendRequests;
import com.audiosnaps.json.model.User;
import com.audiosnaps.log.MyLog;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("HandlerLeak")
public class DiscoverPeopleAdapter extends BaseAdapter {

	private static String TAG = "DiscoverPeopleAdapter";
	private Context context;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private List<User> usersList;
	private LayoutInflater inflator;

	// Constructores
	public DiscoverPeopleAdapter(Context context, List<User> usersList) {
		this.context = context;
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// Get only users array
		this.usersList = usersList;
	}
	

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View row = inflator.inflate(R.layout.item_lista_amigos, null);

		// Views
		TextView userName = (TextView) row.findViewById(R.id.lblUserNameFriendList);
		TextView userRelation = (TextView) row.findViewById(R.id.lblUserRelationFriendList);
		ImageView userAvatar = (ImageView) row.findViewById(R.id.imgAvatarFriendList);
		final Button btnRelation = (Button) row.findViewById(R.id.btnRelation);
		btnRelation.setVisibility(View.VISIBLE);
		final ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.progressBarRelation);
		
		// Load data
		User user = usersList.get(position);

		// Friendship status
		switch (user.is_friend) {
			case HttpConnections.RELATION_IS_FRIEND:
				btnRelation.setText(context.getResources().getString(R.string.ACCEPTED));
				userRelation.setText(context.getResources().getString(R.string.IS_FRIEND));
			break;
			default:
				btnRelation.setText(context.getResources().getString(R.string.ADD_FRIEND));
				userRelation.setText(context.getResources().getString(R.string.IS_NOT_FRIEND));
				break;
		}

		// Friend data
		userName.setText(user.koepics.user_name);

		// User avatar
		imageLoader.displayImage(user.koepics.picture_url, userAvatar, BaseActivity.optionsAvatarImage, null);

		// Listener
		btnRelation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Amistad solicitada");
				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						// Friend request ok
						if ((Boolean) msg.obj) {
							try {
								User user = usersList.get(position);
								user.is_friend = HttpConnections.RELATION_IS_FRIEND;
								if(BaseActivity.DEBUG) MyLog.d(TAG, "JSONObject reemplazado");
							} catch (Exception e) {
								e.printStackTrace();
							}
							progressBar.setVisibility(View.INVISIBLE);
							btnRelation.setVisibility(View.VISIBLE);
							btnRelation.setClickable(false);
							btnRelation.setText(context.getResources().getString(R.string.ACCEPTED));
						}
						// Friend request error
						else {
							progressBar.setVisibility(View.INVISIBLE);
							btnRelation.setVisibility(View.VISIBLE);
							Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
						}
					};
				};

				User user = usersList.get(position);
				btnRelation.setVisibility(View.INVISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Solicitamos amistad a id: " + user.koepics.id);

				SendFriendRequests sendFriendRequests = new SendFriendRequests(context, handler, LoggedUser.id, user.koepics.id, LoggedUser.koeToken);
				sendFriendRequests.execute();
			}
		});

		return row;
	}

	@Override
	public int getCount() {
		return usersList.size();
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