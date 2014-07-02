package com.audiosnaps.classes;

import org.json.JSONException;
import org.json.JSONObject;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.log.MyLog;

import com.audiosnaps.http.HttpConnections;

public class UserClass {

	private final static String TAG = "UserClass";
	private String userId, userName, privacyMode, userCaption, pictureUrl, numOfFollowers, numOfFriends, numOfPics;
	private int isFriend, isFollower;
	private String hadPassword, hadEmail, email, notificationFbInterval, notificationApnsInterval, notificationEmailInterval, shareLikesFb, shareLikesTw, pictureHash, notificationsJsonArray;

	// Constructor user without settings
	public UserClass(String userId, String userName, String privacyMode, String userCaption, String pictureUrl, String numOfFollowers, String numOfFriends, String numOfPics, int isFriend,
			int isFollower) {
		super();
		this.userId = userId;
		this.userName = userName;
		this.privacyMode = privacyMode;
		this.userCaption = userCaption;
		this.pictureUrl = pictureUrl;
		this.numOfFollowers = numOfFollowers;
		this.numOfFriends = numOfFriends;
		this.numOfPics = numOfPics;
		this.isFriend = isFriend;
		this.isFollower = isFollower;
	}

	// Constructor user with settings
	public UserClass(String userId, String userName, String privacyMode, String userCaption, String pictureUrl, String numOfFollowers, String numOfFriends, String numOfPics, String hadPassword,
			String hadEmail, String email, String notificationFbInterval, String notificationApnsInterval, String notificationEmailInterval, String shareLikesFb, String shareLikesTw,
			String pictureHash, int isFriend, int isFollower, String notificationsJsonArray) {
		super();
		this.userId = userId;
		this.userName = userName;
		this.privacyMode = privacyMode;
		this.userCaption = userCaption;
		this.pictureUrl = pictureUrl;
		this.numOfFollowers = numOfFollowers;
		this.numOfFriends = numOfFriends;
		this.numOfPics = numOfPics;
		this.hadPassword = hadPassword;
		this.hadEmail = hadEmail;
		this.email = email;
		this.notificationFbInterval = notificationFbInterval;
		this.notificationApnsInterval = notificationApnsInterval;
		this.notificationEmailInterval = notificationEmailInterval;
		this.shareLikesFb = shareLikesFb;
		this.shareLikesTw = shareLikesTw;
		this.pictureHash = pictureHash;
		this.isFriend = isFriend;
		this.isFollower = isFollower;
		this.notificationsJsonArray = notificationsJsonArray;
	}

	public UserClass() {

	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPrivacyMode() {
		return privacyMode;
	}

	public void setPrivacyMode(String privacyMode) {
		this.privacyMode = privacyMode;
	}

	public String getUserCaption() {
		return userCaption;
	}

	public void setUserCaption(String userCaption) {
		this.userCaption = userCaption;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String getNumOfFollowers() {
		return numOfFollowers;
	}

	public void setNumOfFollowers(String numOfFollowers) {
		this.numOfFollowers = numOfFollowers;
	}

	public String getNumOfFriends() {
		return numOfFriends;
	}

	public void setNumOfFriends(String numOfFriends) {
		this.numOfFriends = numOfFriends;
	}

	public String getNumOfPics() {
		return numOfPics;
	}

	public void setNumOfPics(String numOfPicks) {
		this.numOfPics = numOfPicks;
	}

	public int getIsFriend() {
		return isFriend;
	}

	public void setIsFriend(int isFriend) {
		this.isFriend = isFriend;
	}

	public int getIsFollower() {
		return isFollower;
	}

	public void setIsFollower(int isFollower) {
		this.isFollower = isFollower;
	}

	public String getHadPassword() {
		return hadPassword;
	}

	public void setHadPassword(String hadPassword) {
		this.hadPassword = hadPassword;
	}

	public String getHadEmail() {
		return hadEmail;
	}

	public void setHadEmail(String hadEmail) {
		this.hadEmail = hadEmail;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNotificationFbInterval() {
		return notificationFbInterval;
	}

	public void setNotificationFbInterval(String notificationFbInterval) {
		this.notificationFbInterval = notificationFbInterval;
	}

	public String getNotificationApnsInterval() {
		return notificationApnsInterval;
	}

	public void setNotificationApnsInterval(String notificationApnsInterval) {
		this.notificationApnsInterval = notificationApnsInterval;
	}

	public String getNotificationEmailInterval() {
		return notificationEmailInterval;
	}

	public void setNotificationEmailInterval(String notificationEmailInterval) {
		this.notificationEmailInterval = notificationEmailInterval;
	}

	public String getShareLikesFb() {
		return shareLikesFb;
	}

	public void setShareLikesFb(String shareLikesFb) {
		this.shareLikesFb = shareLikesFb;
	}

	public String getShareLikesTw() {
		return shareLikesTw;
	}

	public void setShareLikesTw(String shareLikesTw) {
		this.shareLikesTw = shareLikesTw;
	}

	public String getPictureHash() {
		return pictureHash;
	}

	public void setPictureHash(String pictureHash) {
		this.pictureHash = pictureHash;
	}
	
	public String getNotificationsJsonArray() {
		return notificationsJsonArray;
	}

	public void setNotificationsJsonArray(String notificationsJsonArray) {
		this.notificationsJsonArray = notificationsJsonArray;
	}

	// Crea objeto UserClass a partir de la respuesta json
	public UserClass createUserClassFormJson(String result) {

		UserClass userClass = new UserClass();
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(result);
			userClass.setUserId(jsonObject.getString(HttpConnections.USER_ID));
			userClass.setUserName(jsonObject.getString(HttpConnections.USER_NAME));
			userClass.setPrivacyMode(jsonObject.getString(HttpConnections.PRIVACY_MODE));
			userClass.setUserCaption(jsonObject.getString(HttpConnections.USER_CAPTION));
			userClass.setPictureUrl(jsonObject.getString(HttpConnections.PICTURE_URL));
			userClass.setNumOfFollowers(jsonObject.getString(HttpConnections.NUM_OF_FOLLOWERS));
			userClass.setNumOfFriends(jsonObject.getString(HttpConnections.NUM_OF_FRIENDS));
			userClass.setNumOfPics(jsonObject.getString(HttpConnections.NUM_OF_PICS));
			userClass.setIsFollower(jsonObject.getInt(HttpConnections.IS_FOLLOWER));
			userClass.setIsFriend(jsonObject.getInt(HttpConnections.IS_FRIEND));
		} catch (Exception e) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Exception");
			e.printStackTrace();
		}

		return userClass;
	}

	// Crea objeto UserClass withs settings a partir de la respuesta json
	public UserClass createUserClassWithSettingsFormJson(String result, String notifications) {

		UserClass userClass = new UserClass();
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(result);
			userClass.setUserId(jsonObject.getString(HttpConnections.USER_ID));
			userClass.setUserName(jsonObject.getString(HttpConnections.USER_NAME));
			userClass.setPrivacyMode(jsonObject.getString(HttpConnections.PRIVACY_MODE));
			userClass.setUserCaption(jsonObject.getString(HttpConnections.USER_CAPTION));
			userClass.setPictureUrl(jsonObject.getString(HttpConnections.PICTURE_URL));
			userClass.setNumOfFollowers(jsonObject.getString(HttpConnections.NUM_OF_FOLLOWERS));
			userClass.setNumOfFriends(jsonObject.getString(HttpConnections.NUM_OF_FRIENDS));
			userClass.setNumOfPics(jsonObject.getString(HttpConnections.NUM_OF_PICS));
			userClass.setIsFollower(jsonObject.getInt(HttpConnections.IS_FOLLOWER));
			userClass.setIsFriend(jsonObject.getInt(HttpConnections.IS_FRIEND));
			userClass.setHadPassword(jsonObject.getString(HttpConnections.HAD_PASSWORD));
			userClass.setHadEmail(jsonObject.getString(HttpConnections.HAD_EMAIL));
			userClass.setEmail(jsonObject.getString(HttpConnections.EMAIL));
			userClass.setNotificationFbInterval(jsonObject.getString(HttpConnections.NOTIFICATION_FB_INTERVAL));
			userClass.setNotificationApnsInterval(jsonObject.getString(HttpConnections.NOTIFICATION_APNS_INTERVAL));
			userClass.setNotificationEmailInterval(jsonObject.getString(HttpConnections.NOTIFICATION_EMAIL_INTERVAL));
			userClass.setShareLikesFb(jsonObject.getString(HttpConnections.SHARE_LIKES_FB));
			userClass.setShareLikesTw(jsonObject.getString(HttpConnections.SHARE_LIKES_TW));
			userClass.setPictureHash(jsonObject.getString(HttpConnections.PICTURE_HASH));
			userClass.setNotificationsJsonArray(notifications);
		} catch (Exception e) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Exception");
			e.printStackTrace();
		}

		return userClass;
	}

	public UserClass createUserClassFormPic(JSONObject jsonObject) {
		
		try {
			jsonObject = jsonObject.getJSONObject(HttpConnections.OWNER_DATA);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		UserClass userClass = new UserClass();
		try {
			userClass.setUserId(jsonObject.getString(HttpConnections.USER_ID));
			userClass.setUserName(jsonObject.getString(HttpConnections.USER_NAME));
			userClass.setPrivacyMode(jsonObject.getString(HttpConnections.PRIVACY_MODE));
			userClass.setUserCaption(jsonObject.getString(HttpConnections.USER_CAPTION));
			userClass.setPictureUrl(jsonObject.getString(HttpConnections.PICTURE_URL));
			userClass.setNumOfFollowers(jsonObject.getString(HttpConnections.NUM_OF_FOLLOWERS));
			userClass.setNumOfFriends(jsonObject.getString(HttpConnections.NUM_OF_FRIENDS));
			userClass.setNumOfPics(jsonObject.getString(HttpConnections.NUM_OF_PICS));
			userClass.setIsFollower(jsonObject.getInt(HttpConnections.IS_FOLLOWER));
			//userClass.setIsFriend(jsonObject.getInt(HttpConnections.IS_FRIEND));
		} catch (Exception e) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Exception");
			e.printStackTrace();
		}

		return userClass;
	}
}
