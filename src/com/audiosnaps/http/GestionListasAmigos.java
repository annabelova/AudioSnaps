package com.audiosnaps.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.json.model.User;
import com.audiosnaps.log.MyLog;

public class GestionListasAmigos {

	private final static String TAG = "GestionListasAmigos";

	// Filtra lista de amigos devolviendo sólo is_friends=1
	public static List<User> getMyFriends(List<User> friendsFollowersSuggested) {
		ArrayList<User> friends = new ArrayList<User>();

		for (User friend : friendsFollowersSuggested) {
			if (friend.is_friend == HttpConnections.RELATION_IS_FRIEND)
				if(exists(friend)) friends.add(friend);
		}

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "Filtrados friends del user, total friends: " + friends.size());

		return friends;
	}

	// Filtra lista de amigos devolviendo sólo is_friends=1
	public static List<User> getMyPendingFriends(List<User> friendsFollowersSuggested) {
		ArrayList<User> pending = new ArrayList<User>();

		for (User friend : friendsFollowersSuggested) {
			if (friend.is_friend == HttpConnections.RELATION_IS_PENDING)
				if(exists(friend)) pending.add(friend);
		}

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "Filtrados pending friends del user, total pending friends: " + pending.size());

		return pending;
	}

	public static List<User> getMyFriendsAndPendingFriends(List<User> friendsFollowersSuggested) {
		ArrayList<User> pending = new ArrayList<User>();
		for (User friend : friendsFollowersSuggested) {
			if (friend.is_friend == HttpConnections.RELATION_IS_FRIEND || friend.is_friend == HttpConnections.RELATION_IS_PENDING){
				if(exists(friend)) pending.add(friend);
			}
		}

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "Filtrados pending friends del user, total pending friends: " + pending.size());

		return pending;
	}

	// Filtra lista de amigos devolviendo mis followers
	public static List<User> getMyFollowers(List<User> friendsFollowersSuggested) {
		ArrayList<User> followers = new ArrayList<User>();

		for (User friend : friendsFollowersSuggested) {
			if (friend.is_follower == HttpConnections.RELATION_IS_FOLLOWING)
				if(exists(friend)) followers.add(friend);
		}

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "Filtrados followers del user, total followers: " + followers.size());

		return followers;
	}
	
	public static List<User> getMyPendingFollowers(List<User> friendsFollowersSuggested) {
		ArrayList<User> pending = new ArrayList<User>();

		for (User friend : friendsFollowersSuggested) {
			if (friend.is_follower == HttpConnections.RELATION_IS_PENDING)
				if(exists(friend)) pending.add(friend);
		}

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "Filtrados pending friends del user, total pending friends: " + pending.size());

		return pending;
	}

	public static List<User> getMyFollowersAndPendingFollowers(List<User> friendsFollowersSuggested) {
		ArrayList<User> pending = new ArrayList<User>();

		for (User friend : friendsFollowersSuggested) {
			if (friend.is_follower == HttpConnections.RELATION_IS_FOLLOWING || friend.is_follower == HttpConnections.RELATION_IS_PENDING)
				if(exists(friend)) pending.add(friend);
		}

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "Filtrados pending friends del user, total pending friends: " + pending.size());

		return pending;
	}

	// Filtra lista de suggested users devolviendo sólo is_friends=3 o
	// is_follower=3
	public static List<User> getMySuggestedFriendsFollowers(List<User> friendsFollowersSuggested) {
		ArrayList<User> suggestedFriendsFollowers = new ArrayList<User>();

		for (User friend : friendsFollowersSuggested) {
			if (friend.is_friend == HttpConnections.RELATION_SUGGESTED || friend.is_follower == HttpConnections.RELATION_SUGGESTED)
				if(exists(friend)) suggestedFriendsFollowers.add(friend);
		}

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "Filtrados suggested users del user, total suggested users: " + suggestedFriendsFollowers.size());

		return suggestedFriendsFollowers;

	}

	// Filtra lista completa de amigos-followers-suggested con texto del
	// buscador
	public static List<User> getFilterSuggestedFriendsFollowers(List<User> list, String texto, Locale locale) {
		
		ArrayList<User> listFiltered = new ArrayList<User>();
		
		if (texto.length() > 0) {
			
			for (User friend : list) {
				if(friend.koepics.user_name != null){
					if (friend.koepics.user_name.toLowerCase(locale).contains(texto.toLowerCase(locale))) {
						if(exists(friend)) listFiltered.add(friend);
					}
				}
			}
			
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Filtrados por buscador: " + listFiltered.size());
			
			return listFiltered;
		} else {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "No se filtra nada");
			return list;
		}

	}
	
	private static boolean exists(User friend) {
		return (friend.koepics.user_name != null && !friend.koepics.user_name.equals("")) || 
				(friend.facebook.user_name != null && !friend.facebook.user_name.equals("")) || 
				(friend.twitter.user_name != null && !friend.twitter.user_name.equals(""));
	}
}
