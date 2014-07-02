package com.audiosnaps.data;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.http.HttpConnections;

public class LoggedUser {

	// User data
	public static String id = "";
	public static String username = "";
	public static String avatarURL = "";
	public static String numOfPics = "";
	public static String numOfFriends = "";
	public static String numOfFollowers = "";
	public static String caption = "";
	public static String koeToken = "";
	public static String privacyMode = "";
	
	private static HashMap<String, Boolean> visibility = new HashMap<String, Boolean>();
	
	private static boolean update = false;
	
	// Save user data
	public static void save(Context context){
		SharedPreferences prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(HttpConnections.USER_ID, id);
		editor.putString(HttpConnections.KOE_TOKEN, koeToken);
		editor.putString(HttpConnections.USER_NAME, username);
		editor.putString(HttpConnections.PICTURE_URL, avatarURL);
		editor.putString(HttpConnections.NUM_OF_PICS, numOfPics);
		editor.putString(HttpConnections.NUM_OF_FRIENDS, numOfFriends);
		editor.putString(HttpConnections.NUM_OF_FOLLOWERS, numOfFollowers);
		editor.putString(HttpConnections.USER_CAPTION, caption);
		editor.putString(HttpConnections.PRIVACY_MODE, privacyMode);
		editor.commit();
	}
	
	// Restore saved user data
	public static void load(Context context){
		SharedPreferences prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		id = prefs.getString(HttpConnections.USER_ID, "-1");
		koeToken = prefs.getString(HttpConnections.KOE_TOKEN, "-1");
		username = prefs.getString(HttpConnections.USER_NAME, "-1");
		avatarURL = prefs.getString(HttpConnections.PICTURE_URL, "-1");
		numOfPics = prefs.getString(HttpConnections.NUM_OF_PICS, "0");
		numOfFriends = prefs.getString(HttpConnections.NUM_OF_FRIENDS, "0");
		numOfFollowers = prefs.getString(HttpConnections.NUM_OF_FOLLOWERS, "0");
		caption = prefs.getString(HttpConnections.USER_CAPTION, "0");
		privacyMode = prefs.getString(HttpConnections.PRIVACY_MODE, "-1");
	}
	
	public static void initPicturePublicOrPrivate(String picHash, boolean is_public){
		if(!visibility.containsKey(picHash)) visibility.put(picHash, is_public);
	}
	
	public static void setPicturePublicOrPrivate(String picHash, boolean is_public){
		visibility.put(picHash, is_public);
	}
	
	public static boolean getPublic(String picHash){
		if(visibility.containsKey(picHash)) return visibility.get(picHash);
		else return false;
	}

	// Guarda koeToken en prefs
	public static void saveKoeToken(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(HttpConnections.KOE_TOKEN, koeToken);
        editor.commit();
	}
	
	public static boolean hasToUpdate(){
		if(update){ 
			update = false;
			return true;
		}
		return false;
	}
	
	public static void update(){
		update = true;
	}
	
}
