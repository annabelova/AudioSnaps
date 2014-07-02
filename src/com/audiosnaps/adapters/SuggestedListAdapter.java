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
public class SuggestedListAdapter extends BaseAdapter {

	private static String TAG = "SuggestedListAdapter";
	private Context context;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private List<User> suggestedList;
	private LayoutInflater inflator;
	private String id;

	// Constructor
	public SuggestedListAdapter(Context context, List<User> suggestedList, String id) {
		this.context = context;
		this.suggestedList = suggestedList;
		this.id = id;
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View row = inflator.inflate(R.layout.item_lista_amigos, null);

		// Views
		TextView userName = (TextView) row.findViewById(R.id.lblUserNameFriendList);
		ImageView userAvatar = (ImageView) row.findViewById(R.id.imgAvatarFriendList);
		final Button btnRelation = (Button) row.findViewById(R.id.btnRelation);
		btnRelation.setVisibility(View.VISIBLE);
		final ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.progressBarRelation);

		// Set fonts
		FontLoader.setRobotoFont(userName, this.context, FontLoader.MEDIUM);
		FontLoader.setRobotoFont(btnRelation, this.context, FontLoader.BOLD);
		
		// Load data
		try {
			User suggested = suggestedList.get(position);

			if(LoggedUser.id == id){
			
				// Friendship status
				switch (suggested.is_friend) {
					case HttpConnections.RELATION_IS_FRIEND:
						btnRelation.setText(context.getResources().getString(R.string.ACCEPTED));
						btnRelation.setClickable(false);
						break;
					default:
						btnRelation.setText(context.getResources().getString(R.string.ADD_FRIEND));
						break;
				}
			
			}

			// Friend data
			userName.setText(suggested.koepics.user_name);

			// User avatar
			imageLoader.displayImage(suggested.koepics.picture_url, userAvatar, BaseActivity.optionsAvatarImage, null);

		} catch (Exception e) {
			e.printStackTrace();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Exception");
		}

		// Listener
		btnRelation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Amistad solicitada");
				User suggested;
				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {

						// Friend request ok
						if ((Boolean) msg.obj) {
							try {
								User suggested = suggestedList.get(position);
								suggested.is_friend = HttpConnections.RELATION_IS_FRIEND;
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

				suggested = suggestedList.get(position);
				btnRelation.setVisibility(View.INVISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Solicitamos amistad a id: " + suggested.koepics.id);

				SendFriendRequests sendFriendRequests = new SendFriendRequests(context, handler, LoggedUser.id, suggested.koepics.id, LoggedUser.koeToken);
				sendFriendRequests.execute();
			}
		});

		return row;
	}

	@Override
	public int getCount() {
		return suggestedList.size();
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