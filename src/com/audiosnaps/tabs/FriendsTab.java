package com.audiosnaps.tabs;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.activities.UserFeedActivity;
import com.audiosnaps.adapters.FollowersListAdapter;
import com.audiosnaps.adapters.FriendsListAdapter;
import com.audiosnaps.adapters.SuggestedListAdapter;
import com.audiosnaps.classes.FontLoader;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.http.GestionListasAmigos;
import com.audiosnaps.http.GetCompleteFriendsList;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.json.model.User;
import com.audiosnaps.json.model.UserComparator;
import com.audiosnaps.log.MyLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("HandlerLeak")
public class FriendsTab {

	private final static String TAG = "FriendsClass";
	private Activity activity;
	private Context context;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private PullToRefreshListView listViewFriends, listViewFollowers, listViewSuggested;
	private List<User> friendsFollowersSuggestedList, filteredList;
	private GetCompleteFriendsList getCompleteFriendsList;
	private Button btnRetryFriends, btnListFriends, btnListFollowers, btnListSuggested;
	public static boolean firstFeedAnimationDone = false;
	private String targetId;
	private int feedMode;
	private boolean showFriends = true;

	// Adapters
	private FriendsListAdapter friendsListAdapter;
	private FollowersListAdapter followersListAdapter;
	private SuggestedListAdapter suggestedListAdapter;

	// Constructor
	public FriendsTab(Context context, Activity activity) {
		this.activity = activity;
		this.context = context;
	}

	// Init pantalla Me and Mine
	public void initFriends(final int mode, final String id, boolean toShowFriends) {
		
		feedMode = mode;
		showFriends = toShowFriends;

		// Views
		listViewFriends = (PullToRefreshListView) activity.findViewById(R.id.listViewFriends);
		listViewFollowers = (PullToRefreshListView) activity.findViewById(R.id.listViewFollowers);
		listViewSuggested = (PullToRefreshListView) activity.findViewById(R.id.listViewSuggested);
		btnRetryFriends = (Button) activity.findViewById(R.id.btnRetryFriends);
		btnListFriends = (Button) activity.findViewById(R.id.btnListFriends);
		btnListFollowers = (Button) activity.findViewById(R.id.btnListFollowers);
		btnListSuggested = (Button) activity.findViewById(R.id.btnListSuggested);
		final EditText txtBuscadorFriends = (EditText) activity.findViewById(R.id.txtBuscadorFriends);
		ImageView btnBorrarTexto = (ImageView) activity.findViewById(R.id.btnBorrarTexto);
		listViewFriends.setVisibility(View.INVISIBLE);
		listViewFollowers.setVisibility(View.INVISIBLE);
		listViewSuggested.setVisibility(View.INVISIBLE);

		// Friends list mode
		switch (feedMode) {
			case BaseActivity.MAIN_LISTS:
				targetId = LoggedUser.id;
				break;
			case BaseActivity.FRIEND_LIST:
				targetId = id;
				btnListSuggested.setVisibility(View.GONE);
				listViewSuggested.setVisibility(View.GONE);
				break;
		}

		// Set fonts
		FontLoader.setRobotoFont(btnListFriends, this.context, FontLoader.BOLD);
		FontLoader.setRobotoFont(btnListFollowers, this.context, FontLoader.BOLD);
		FontLoader.setRobotoFont(btnListSuggested, this.context, FontLoader.BOLD);
		FontLoader.setRobotoFont(txtBuscadorFriends, this.context, FontLoader.REGULAR);

		// Update lists
		updateLists(id, true);

		// Retry update after newtwork error
		btnRetryFriends.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnRetryFriends.setVisibility(View.INVISIBLE);
				updateLists(id, true);
			}
		});

		// Listeners
		btnListFriends.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos botón friends");
				// updateListFriends();
				listViewFriends.setVisibility(View.VISIBLE);
				listViewFollowers.setVisibility(View.INVISIBLE);
				listViewSuggested.setVisibility(View.INVISIBLE);
				// update button color
				btnListFriends.setTextColor(context.getResources().getColor(R.color.blanco));
				btnListFollowers.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
				btnListSuggested.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
			}
		});

		btnListFollowers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos botón followers");
				// updateListFollowers();
				listViewFriends.setVisibility(View.INVISIBLE);
				listViewFollowers.setVisibility(View.VISIBLE);
				listViewSuggested.setVisibility(View.INVISIBLE);
				// update button color
				btnListFriends.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
				btnListFollowers.setTextColor(context.getResources().getColor(R.color.blanco));
				btnListSuggested.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
			}
		});

		btnListSuggested.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos botón suggested");
				// updateListSuggested();
				listViewFriends.setVisibility(View.INVISIBLE);
				listViewFollowers.setVisibility(View.INVISIBLE);
				listViewSuggested.setVisibility(View.VISIBLE);
				listViewSuggested.setClickable(true);
				// update button color
				btnListFriends.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
				btnListFollowers.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
				btnListSuggested.setTextColor(context.getResources().getColor(R.color.blanco));
			}
		});

		// RefreshToPull listView listeners
		listViewFriends.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				updateListFriends(id);
			}
		});

		listViewFollowers.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				updateListFollowers(id);
			}
		});

		listViewSuggested.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				updateListSuggested(id);
			}
		});

		// List items listeners
		listViewFriends.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsado item listViewFriends position: " + position);

				User friend;
				List<User> list;
				if(txtBuscadorFriends.getText().length() > 0){
					if(id == LoggedUser.id){
						list = GestionListasAmigos.getMyFriendsAndPendingFriends(filteredList);
					}else{
						list = GestionListasAmigos.getMyFriends(filteredList);
					}
				}
				else{
					if(id == LoggedUser.id){
						list = GestionListasAmigos.getMyFriendsAndPendingFriends(friendsFollowersSuggestedList);
					}else{
						list = GestionListasAmigos.getMyFriends(friendsFollowersSuggestedList);
					}
				}
				// Porqué me pilla position+1??
				friend = list.get(position - 1);
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Consultando user: " + friend.koepics.id);
				Intent intent = new Intent(context, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, friend.koepics.id);
				if(LoggedUser.id.equalsIgnoreCase(friend.koepics.id)){
					intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.MY_FEED);
				}
				else{
					intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
					intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
				}
				activity.startActivity(intent);
			}
		});

		listViewFollowers.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsado item listViewFollowers position: " + position);

				User follower;
				List<User> list;
				if(txtBuscadorFriends.getText().length() > 0){
					if(id == LoggedUser.id){
						list = GestionListasAmigos.getMyFollowersAndPendingFollowers(filteredList);
					}else{
						list = GestionListasAmigos.getMyFollowers(filteredList);
					}
				}
				else{
					if(id == LoggedUser.id){
						list = GestionListasAmigos.getMyFollowersAndPendingFollowers(friendsFollowersSuggestedList);
					}else{
						list = GestionListasAmigos.getMyFollowers(friendsFollowersSuggestedList);
					}
				}
				// Porqué me pilla position+1??
				follower = list.get(position - 1);
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Consultando user: " + follower.koepics.id);
				Intent intent = new Intent(context, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, follower.koepics.id);
				if(LoggedUser.id.equalsIgnoreCase(follower.koepics.id)){
					intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.MY_FEED);
				}
				else{
					intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
					intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
				}
				activity.startActivity(intent);
			}
		});

		listViewSuggested.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsado item listViewSuggested position: " + position);
				
				User suggested;
				List<User> list;
				if(txtBuscadorFriends.getText().length() > 0){
					list = GestionListasAmigos.getMySuggestedFriendsFollowers(filteredList);
				}
				else{
					list = GestionListasAmigos.getMySuggestedFriendsFollowers(friendsFollowersSuggestedList);
				}
				// Porqué me pilla position+1??
				suggested = list.get(position - 1);
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Consultando user: " + suggested.koepics.id);
				Intent intent = new Intent(context, UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID, suggested.koepics.id);
				if(LoggedUser.id.equalsIgnoreCase(suggested.koepics.id)){
					intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.MY_FEED);
				}
				else{
					intent.putExtra(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
					intent.putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
				}
				activity.startActivity(intent);
			}
		});

		// Buscador para filtrar listas
		txtBuscadorFriends.setFocusable(true);
		txtBuscadorFriends.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String texto = txtBuscadorFriends.getText().toString();
				updateListsFilter(texto, id);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		// Botón borrar texto
		btnBorrarTexto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txtBuscadorFriends.setText("");
				updateListsFilter("", id);
				updateListsWithoutFilter(id);
			}
		});
		
		
		// Activar texto blanco botón
		if(toShowFriends){
			btnListFriends.setTextColor(context.getResources().getColor(R.color.blanco));
		}
		else{
			btnListFollowers.setTextColor(context.getResources().getColor(R.color.blanco));
		}
	}

	// Get friends list and update lists
	public void updateLists(final String id, final boolean first) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				if(feedMode == BaseActivity.MAIN_FEED){
					if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
						
						if(first) listViewFriends.setVisibility(View.VISIBLE);
						
						Gson gson = new Gson();
						Type friendsListType = new TypeToken<List<User>>(){}.getType();
						
						friendsFollowersSuggestedList = gson.fromJson(result, friendsListType);
						if(friendsFollowersSuggestedList != null) Collections.sort(friendsFollowersSuggestedList, new UserComparator());
						
						for(User user : friendsFollowersSuggestedList){
							Log.v(TAG, "name: " + user.koepics.user_name);
							Log.v(TAG, "id: " + user.koepics.id);
						}
						
						if(id == LoggedUser.id){
							friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriendsAndPendingFriends(friendsFollowersSuggestedList), id);
						}else{
							friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriends(friendsFollowersSuggestedList), id);
						}
						
						listViewFriends.setAdapter(friendsListAdapter);
						
						if(id == LoggedUser.id){
							followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowersAndPendingFollowers(friendsFollowersSuggestedList), id);
						}else{
							followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowers(friendsFollowersSuggestedList), id);
						}
						
						listViewFollowers.setAdapter(followersListAdapter);
						
						suggestedListAdapter = new SuggestedListAdapter(context, GestionListasAmigos.getMySuggestedFriendsFollowers(friendsFollowersSuggestedList), id);
						listViewSuggested.setAdapter(suggestedListAdapter);
					} else {
						
						if(first) {
							listViewFriends.setVisibility(View.INVISIBLE);
							btnRetryFriends.setVisibility(View.VISIBLE);
						}
					}
				}
				else{
					if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
						if (showFriends) {
							if(first) {
								listViewFriends.setVisibility(View.VISIBLE);
								listViewFollowers.setVisibility(View.INVISIBLE);
							}
							btnListFriends.setTextColor(context.getResources().getColor(R.color.blanco));
							btnListFollowers.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
						} else {
							if(first) {
								listViewFriends.setVisibility(View.INVISIBLE);
								listViewFollowers.setVisibility(View.VISIBLE);
							}
							btnListFriends.setTextColor(context.getResources().getColor(R.color.bt_multi_text_color));
							btnListFollowers.setTextColor(context.getResources().getColor(R.color.blanco));
						}
						
						Gson gson = new Gson();
						Type friendsListType = new TypeToken<List<User>>(){}.getType();
						friendsFollowersSuggestedList = gson.fromJson(result, friendsListType);
						if(friendsFollowersSuggestedList != null) Collections.sort(friendsFollowersSuggestedList, new UserComparator());
						
						for(User user : friendsFollowersSuggestedList){
							Log.v(TAG, "name: " + user.koepics.user_name);
							Log.v(TAG, "id: " + user.koepics.id);
						}
						
						if(id == LoggedUser.id){
							friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriendsAndPendingFriends(friendsFollowersSuggestedList), id);
						}else{
							friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriends(friendsFollowersSuggestedList), id);
						}
						
						listViewFriends.setAdapter(friendsListAdapter);
						
						if(id == LoggedUser.id){
							followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowersAndPendingFollowers(friendsFollowersSuggestedList), id);
						}else{
							followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowers(friendsFollowersSuggestedList), id);
						}
						
						listViewFollowers.setAdapter(followersListAdapter);
					} else {
						if(first) {
							listViewFriends.setVisibility(View.INVISIBLE);
							btnRetryFriends.setVisibility(View.VISIBLE);
						}
						
					}
				}
			};
		};

		getCompleteFriendsList = new GetCompleteFriendsList(context, handler, LoggedUser.id, targetId, false, true, true, LoggedUser.koeToken, true);
		getCompleteFriendsList.execute();
	}

	// Update list con texto buscador
	private void updateListsFilter(String texto, String id) {
		filteredList = GestionListasAmigos.getFilterSuggestedFriendsFollowers(friendsFollowersSuggestedList, texto, context.getResources().getConfiguration().locale);
	
		if(id == LoggedUser.id){
			friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriendsAndPendingFriends(filteredList), id);
		}else{
			friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriends(filteredList), id);
		}
		
		listViewFriends.setAdapter(friendsListAdapter);
		
		if(id == LoggedUser.id){
			followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowersAndPendingFollowers(filteredList), id);
		}else{
			followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowers(filteredList), id);
		}
		
		listViewFollowers.setAdapter(followersListAdapter);
		
		suggestedListAdapter = new SuggestedListAdapter(context, GestionListasAmigos.getMySuggestedFriendsFollowers(filteredList), id);
		
		listViewSuggested.setAdapter(suggestedListAdapter);
	}
	
	// Update list sin filtros
	private void updateListsWithoutFilter(String id) {
		
		if(id == LoggedUser.id){
			friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriendsAndPendingFriends(friendsFollowersSuggestedList), id);
		}else{
			friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriends(friendsFollowersSuggestedList), id);
		}
		
		listViewFriends.setAdapter(friendsListAdapter);
		
		if(id == LoggedUser.id){
			followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowersAndPendingFollowers(friendsFollowersSuggestedList), id);
		}else{
			followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowers(friendsFollowersSuggestedList), id);
		}
		
		listViewFollowers.setAdapter(followersListAdapter);
		
		suggestedListAdapter = new SuggestedListAdapter(context, GestionListasAmigos.getMySuggestedFriendsFollowers(friendsFollowersSuggestedList), id);
		listViewSuggested.setAdapter(suggestedListAdapter);
	}

	// Get friends list y update list
	private void updateListFriends(final String id) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				listViewFriends.onRefreshComplete();
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
					Gson gson = new Gson();
					Type friendsListType = new TypeToken<List<User>>(){}.getType();
					friendsFollowersSuggestedList = gson.fromJson(result, friendsListType);
					if(friendsFollowersSuggestedList != null) Collections.sort(friendsFollowersSuggestedList, new UserComparator());
					
					if(id == LoggedUser.id){
						friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriendsAndPendingFriends(friendsFollowersSuggestedList), id);
					}else{
						friendsListAdapter = new FriendsListAdapter(context, GestionListasAmigos.getMyFriends(friendsFollowersSuggestedList), id);
					}
					
					listViewFriends.setAdapter(friendsListAdapter);
				}
			};
		};

		getCompleteFriendsList = new GetCompleteFriendsList(context, handler, LoggedUser.id, targetId, false, false, true, LoggedUser.koeToken, true);
		getCompleteFriendsList.execute();
	}

	// Get followers list y update list
	private void updateListFollowers(final String id) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				listViewFollowers.onRefreshComplete();
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
					Gson gson = new Gson();
					Type friendsListType = new TypeToken<List<User>>(){}.getType();
					List<User> friendsList = gson.fromJson(result, friendsListType);
					if(friendsList != null) Collections.sort(friendsList, new UserComparator());
					
					if(id == LoggedUser.id){
						followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowersAndPendingFollowers(friendsList), id);
					}else{
						followersListAdapter = new FollowersListAdapter(context, GestionListasAmigos.getMyFollowers(friendsList), id);
					}
					
					listViewFollowers.setAdapter(followersListAdapter);
				}
			};
		};

		getCompleteFriendsList = new GetCompleteFriendsList(context, handler, LoggedUser.id, targetId, false, false, true, LoggedUser.koeToken, true);
		getCompleteFriendsList.execute();
	}

	// Get suggested list y update list
	private void updateListSuggested(final String id) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				listViewSuggested.onRefreshComplete();
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
					Gson gson = new Gson();
					Type friendsListType = new TypeToken<List<User>>(){}.getType();
					List<User> friendsList = gson.fromJson(result, friendsListType);
					if(friendsList != null) Collections.sort(friendsList, new UserComparator());
					SuggestedListAdapter suggestedListAdapter = new SuggestedListAdapter(context, GestionListasAmigos.getMySuggestedFriendsFollowers(friendsList), id);
					listViewSuggested.setAdapter(suggestedListAdapter);
				}
			};
		};

		getCompleteFriendsList = new GetCompleteFriendsList(context, handler, LoggedUser.id, targetId, false, true, false, LoggedUser.koeToken, true);
		getCompleteFriendsList.execute();
	}
}
