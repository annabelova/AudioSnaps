package com.audiosnaps.tabs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.activities.UserFeedActivity;
import com.audiosnaps.adapters.DiscoverPeopleAdapter;
import com.audiosnaps.adapters.DiscoverPicturesAdapter;
import com.audiosnaps.classes.FontLoader;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.http.GetFeaturedPicturesAndUsers;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.SearchPictures;
import com.audiosnaps.http.SearchUsers;
import com.audiosnaps.json.model.FeaturedPicturesUsers;
import com.audiosnaps.json.model.Pic;
import com.audiosnaps.json.model.User;
import com.audiosnaps.json.model.UserComparator;
import com.audiosnaps.json.model.UserSimpleProfile;
import com.audiosnaps.log.MyLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("HandlerLeak")
public class DiscoverTab {

	private final static String TAG = "FriendsClass";
	private Activity activity;
	private Context context;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private GridView gridView;
	private ListView listView;
	private TextView lblTrendingUsers;
	private GetFeaturedPicturesAndUsers getFeaturedPicturesAndUsers;
	private Button btnRetryDiscover;
	private String hashtag;
	private List<User> usersList;
	private List<Pic> picturesList;
	private FeaturedPicturesUsers featuredPicturesUsers;
	
	// Adapters
	private DiscoverPicturesAdapter discoverPicturesAdapter;
	private DiscoverPeopleAdapter discoverPeopleAdapter;

	// Constructor
	public DiscoverTab(Context context, Activity activity, String hashtag) {
		this.activity = activity;
		this.context = context;
		this.hashtag = hashtag;
	}

	// Init pantalla Me and Mine
	public void initDiscover() {

		// Views
		final LinearLayout layoutHeader = (LinearLayout) activity.findViewById(R.id.layoutHeader);
		final EditText txtBuscadorDiscoverPictures = (EditText) activity.findViewById(R.id.txtBuscadorDiscoverPictures);
		final EditText txtBuscadorDiscoverPeople = (EditText) activity.findViewById(R.id.txtBuscadorDiscoverPeople);
		final ImageView btnBorrarTextoPictures = (ImageView) activity.findViewById(R.id.btnBorrarTextoPictures);
		final ImageView btnBorrarTextoPeople = (ImageView) activity.findViewById(R.id.btnBorrarTextoPeople);
		btnRetryDiscover = (Button) activity.findViewById(R.id.btnRetryDiscover);
		gridView = (GridView) activity.findViewById(R.id.gridViewDiscover);
		listView = (ListView) activity.findViewById(R.id.listViewDiscover);
		lblTrendingUsers = (TextView) activity.findViewById(R.id.lblTrendingUsers);
		listView.setVisibility(View.INVISIBLE);
		gridView.setVisibility(View.INVISIBLE);
		lblTrendingUsers.setVisibility(View.INVISIBLE);
		final Button btnPicturesDiscover = (Button) activity.findViewById(R.id.btnPicturesDiscover);
		final Button btnPeopleDiscover = (Button) activity.findViewById(R.id.btnPeopleDiscover);

		// Set fonts
		FontLoader.setRobotoFont(btnPicturesDiscover, this.context, FontLoader.BOLD);
		FontLoader.setRobotoFont(btnPeopleDiscover, this.context, FontLoader.BOLD);
		FontLoader.setRobotoFont(txtBuscadorDiscoverPictures, this.context, FontLoader.REGULAR);
		FontLoader.setRobotoFont(txtBuscadorDiscoverPeople, this.context, FontLoader.REGULAR);

		// Tenemos hashtag?
		if (hashtag != null) {
			txtBuscadorDiscoverPictures.setText(Html.fromHtml(hashtag));
			layoutHeader.setVisibility(View.GONE);
			updateListPicturesFilter(hashtag);
		} else {
			updateListPicturesPeople(true);
		}

		// Retry update after newtwork error
		btnRetryDiscover.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnRetryDiscover.setVisibility(View.INVISIBLE);
				updateListPicturesPeople(true);
			}
		});

		// Listeners
		btnPicturesDiscover.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos botón pictures");
				gridView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.INVISIBLE);
				lblTrendingUsers.setVisibility(View.INVISIBLE);
				txtBuscadorDiscoverPictures.setVisibility(View.VISIBLE);
				txtBuscadorDiscoverPeople.setVisibility(View.INVISIBLE);
				btnBorrarTextoPictures.setVisibility(View.VISIBLE);
				btnBorrarTextoPeople.setVisibility(View.INVISIBLE);
				// update button color
				btnPicturesDiscover.setTextColor(context.getResources().getColor(R.color.blanco));
				btnPeopleDiscover.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
			}
		});

		btnPeopleDiscover.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos botón people");
				listView.setVisibility(View.VISIBLE);
				gridView.setVisibility(View.INVISIBLE);
				lblTrendingUsers.setVisibility(View.VISIBLE);
				txtBuscadorDiscoverPictures.setVisibility(View.INVISIBLE);
				txtBuscadorDiscoverPeople.setVisibility(View.VISIBLE);
				btnBorrarTextoPictures.setVisibility(View.INVISIBLE);
				btnBorrarTextoPeople.setVisibility(View.VISIBLE);
				// update button color
				btnPicturesDiscover.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
				btnPeopleDiscover.setTextColor(context.getResources().getColor(R.color.blanco));
			}
		});

		// Listeners buscadores
		txtBuscadorDiscoverPictures.setFocusable(true);
		txtBuscadorDiscoverPictures.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String texto = txtBuscadorDiscoverPictures.getText().toString();
				if (texto.length() > 2) {
					updateListPicturesFilter(texto);
				} else {
					if (hashtag == null) {
						picturesList = featuredPicturesUsers.pics_data;
						discoverPicturesAdapter = new DiscoverPicturesAdapter(context, picturesList);
						gridView.setAdapter(discoverPicturesAdapter);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

		// Botón borrar texto
		btnBorrarTextoPictures.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txtBuscadorDiscoverPictures.setText("");
				if (hashtag == null) {
					picturesList = featuredPicturesUsers.pics_data;
					discoverPicturesAdapter = new DiscoverPicturesAdapter(context, picturesList);
					gridView.setAdapter(discoverPicturesAdapter);
				}
			}
		});

		txtBuscadorDiscoverPeople.setFocusable(true);
		txtBuscadorDiscoverPeople.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String texto = txtBuscadorDiscoverPeople.getText().toString();
				if (texto.length() > 2) {
					updateListPeopleFilter(texto);
				} else {
					usersList = featuredPicturesUsers.users_data;
					if(usersList != null) Collections.sort(usersList, new UserComparator());
					discoverPeopleAdapter = new DiscoverPeopleAdapter(context, usersList);
					listView.setAdapter(discoverPeopleAdapter);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

		// Botón borrar texto
		btnBorrarTextoPeople.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txtBuscadorDiscoverPeople.setText("");
				discoverPeopleAdapter = new DiscoverPeopleAdapter(context, usersList);
				listView.setAdapter(discoverPeopleAdapter);
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsado item listView position: " + position);

					User user = usersList.get(position);
					if(BaseActivity.DEBUG) MyLog.d(TAG, "Consultando user: " + user.koepics.id);
					Intent intent = new Intent(context, UserFeedActivity.class);
					intent.putExtra(HttpConnections.USER_TARGET_ID, user.koepics.id);
					if (LoggedUser.id.equalsIgnoreCase(user.koepics.id)) {
						intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.MY_FEED);
					} else {
						intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
						intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
					}
					activity.startActivity(intent);
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsado item gridView position: " + position);

				Intent intent = new Intent(context, UserFeedActivity.class);
				Pic picture = picturesList.get(position);
				intent.putExtra(HttpConnections.USER_TARGET_ID, picture.user_data.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, picture.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.ONE_PICTURE_FEED);
				activity.startActivity(intent);
			}
		});

		btnPicturesDiscover.setTextColor(context.getResources().getColor(R.color.blanco));
	}

	// Get friends list y update list
	public void updateListPicturesPeople(final boolean first) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

					if(first) gridView.setVisibility(View.VISIBLE);
					
					Gson gson = new Gson();
					featuredPicturesUsers = gson.fromJson(result, FeaturedPicturesUsers.class);
					
					// update grid pictures
					picturesList = featuredPicturesUsers.pics_data;
					discoverPicturesAdapter = new DiscoverPicturesAdapter(context, picturesList);
					gridView.setAdapter(discoverPicturesAdapter);

					// update list people
					
					usersList = featuredPicturesUsers.users_data;
					if(usersList != null) Collections.sort(usersList, new UserComparator());
					//jsonArrayUsers = jsonObject.getJSONArray(HttpConnections.USERS_DATA);
					discoverPeopleAdapter = new DiscoverPeopleAdapter(context, usersList);
					listView.setAdapter(discoverPeopleAdapter);
					
				} else {
					
					if(first){
						gridView.setVisibility(View.INVISIBLE);
						btnRetryDiscover.setVisibility(View.VISIBLE);
					}
					
				}
			};
		};

		getFeaturedPicturesAndUsers = new GetFeaturedPicturesAndUsers(context, handler, LoggedUser.id, LoggedUser.koeToken);
		getFeaturedPicturesAndUsers.execute();
	}

	// Update list pictures con texto buscador
	private void updateListPicturesFilter(String texto) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
					gridView.setVisibility(View.VISIBLE);
					
					Gson gson = new Gson();
					Type picListType = new TypeToken<List<Pic>>(){}.getType();
					picturesList = gson.fromJson(result, picListType);
					discoverPicturesAdapter = new DiscoverPicturesAdapter(context, picturesList);
					gridView.setAdapter(discoverPicturesAdapter);
				} else {
					gridView.setVisibility(View.INVISIBLE);
					btnRetryDiscover.setVisibility(View.VISIBLE);
				}
			};
		};

		SearchPictures searchPictures = new SearchPictures(context, handler, LoggedUser.id, LoggedUser.koeToken, texto, false);
		searchPictures.execute();
	}

	// Update list users con texto buscador
	private void updateListPeopleFilter(String texto) {
		final Handler handler = new Handler() {

			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
					listView.setVisibility(View.VISIBLE);
					lblTrendingUsers.setVisibility(View.VISIBLE);
					
					Log.v(TAG, result);
					
					Gson gson = new Gson();
					Type userSimpleProfileListType = new TypeToken<List<UserSimpleProfile>>(){}.getType();
					List<UserSimpleProfile> userSimpleProfileList = gson.fromJson(result, userSimpleProfileListType);
					usersList = new ArrayList<User>();
					for(UserSimpleProfile userSimpleProfile : userSimpleProfileList){
						User user = new User();
						user.koepics.id = userSimpleProfile.user_id;
						user.koepics.user_name = userSimpleProfile.user_name;
						user.koepics.picture_url = userSimpleProfile.picture_url;
						user.is_friend = userSimpleProfile.is_friend;
						user.is_follower = userSimpleProfile.is_follower;
						usersList.add(user);
					}
					Collections.sort(usersList, new UserComparator());
					discoverPeopleAdapter = new DiscoverPeopleAdapter(context, usersList);
					listView.setAdapter(discoverPeopleAdapter);
				} else {
					listView.setVisibility(View.INVISIBLE);
					lblTrendingUsers.setVisibility(View.INVISIBLE);
					btnRetryDiscover.setVisibility(View.VISIBLE);
				}
			};
		};

		SearchUsers searchUsers = new SearchUsers(context, handler, LoggedUser.id, LoggedUser.koeToken, texto, false);
		searchUsers.execute();
	}
}