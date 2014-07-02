package com.audiosnap.library.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.audiosnaps.http.HttpConnections;

public class MentionUtil {
	
	public static String getKoepicsId(JSONObject jsonObject) throws JSONException{
		return jsonObject.getJSONObject(HttpConnections.KOEPICS).getString(HttpConnections.ID);
	}
	
	public static String getKoepicsUserName(JSONObject jsonObject) throws JSONException{
		return jsonObject.getJSONObject(HttpConnections.KOEPICS).getString(HttpConnections.USER_NAME);
	}
	
	public static String getFacebookId(JSONObject jsonObject) throws JSONException{
		return jsonObject.getJSONObject(HttpConnections.FACEBOOK).getString(HttpConnections.ID);
	}
	
	public static String getFacebookUserName(JSONObject jsonObject) throws JSONException{
		return jsonObject.getJSONObject(HttpConnections.FACEBOOK).getString(HttpConnections.USER_NAME);
	}
	
	public static String getTwitterId(JSONObject jsonObject) throws JSONException{
		return jsonObject.getJSONObject(HttpConnections.TWITTER).getString(HttpConnections.ID);
	}
	
	public static String getTwitterUserName(JSONObject jsonObject) throws JSONException{
		return jsonObject.getJSONObject(HttpConnections.TWITTER).getString(HttpConnections.USER_NAME);
	}
}
