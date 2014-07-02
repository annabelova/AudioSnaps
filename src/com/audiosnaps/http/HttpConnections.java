package com.audiosnaps.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.classes.DeviceUuidFactory;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.log.MyLog;

public abstract class HttpConnections extends AsyncTask<Object, String, String> {

	private static final String TAG = "HttpConnections";

	public static final String URL_SERVER = "https://audiosnaps.com/users_mobile/";
	//public static final String URL_SERVER_DEV = "http://dev.koepics.com/users_mobile/";
	static BasicCookieStore cookieStore = new BasicCookieStore();
	static HttpContext httpContext = new BasicHttpContext();

	public static String recover_user = "";

	Context context;

	// JSON TAGS
	public final static String FUNCTION_NAME = "action";
	public final static String ACTION_DATA = "action_data";
	public final static String SUCCES = "success";
	public final static String USER_ID = "user_id";
	public final static String USER_TARGET_ID = "target_user_id";
	public final static String USER_DATA = "user_data";
	public final static String USERS_DATA = "users_data";
	public final static String USER_NAME = "user_name";
	public final static String PICTURE_URL = "picture_url";
	public final static String NUM_OF_PICS = "num_of_pics";
	public final static String NUM_OF_FRIENDS = "num_of_friends";
	public final static String NUM_OF_FOLLOWERS = "num_of_followers";
	public final static String PRIVACY_MODE = "privacy_mode";
	public final static String USER_CAPTION = "user_caption";
	public final static String IS_FRIEND = "is_friend";
	public final static String IS_FOLLOWER = "is_follower";
	public final static String HAD_PASSWORD = "had_password";
	public final static String HAD_EMAIL = "had_email";
	public final static String EMAIL = "email";
	public final static String NOTIFICATION_FB_INTERVAL = "notification_fb_interval";
	public final static String NOTIFICATION_APNS_INTERVAL = "notification_apns_interval";
	public final static String NOTIFICATION_EMAIL_INTERVAL = "notification_email_interval";
	public final static String NOTI_FB_INTERVAL = "noti_fb_interval";
	public final static String NOTI_APNS_INTERVAL = "noti_apns_interval";
	public final static String NOTI_EMAIL_INTERVAL = "noti_email_interval";
	public final static String SHARE_LIKES_FB = "share_likes_fb";
	public final static String SHARE_LIKES_TW = "share_likes_tw";
	public final static String PICTURE_HASH = "picture_hash";
	public final static String ROCKSTAR = "rockstar";
	public final static String GRANDMA = "grandma";
	public final static String USER_SETTINGS = "user_settings";
	public final static String SUCCESSFUL_USER_SETTINGS = "successful_user_settings";
	public final static String FRIENDS_DATA = "friends_data";
	public final static String FRIEND_KOEPICS = "koepics";
	public final static String NAME = "name";
	public final static String FRIENDS = "friends";
	public final static String INCLUDE_SOCIAL_FRIENDS = "include_social_friends";
	public final static String INCLUDE_SUGGESTED_FRIENDS = "include_suggested_friends";
	public final static String INCLUDE_PENDING_FRIENDS = "include_pending_friends";
	public final static String FROM_HASH = "from_hash";
	public final static String FROM_TIMESTAMP = "from_timestamp";
	public final static String TIMESTAMP = "timestamp";
	public final static String DIRECTION = "direction";
	public final static String NUM_PICTURES = "num_pictures";
	public final static String FEED_DATA = "feed_data";
	public final static String FEED_KOEPIC = "koepic";
	public final static String URL = "url";
	public final static String LIKE_DATA = "like_data";
	public final static String COMMENT_DATA = "comment_data";
	public final static String TOTAL_LIKES = "total_likes";
	public final static String HAS_LIKED = "has_liked";
	public final static String TOTAL_COMMENTS = "total_comments";
	public final static String PICS_DATA = "pics_data";
	public final static String PIC_DATA = "pic_data";
	public final static String FRIENDS_IDS = "friend_ids";
	public final static String FRIENDS_RESULT = "friends_result";
	public final static String ID = "id";
	public final static String MICRO_URL = "micro_url";
	public final static String EXTENDED = "extended";
	public final static String SEARCH_STRING = "search_string";
	public final static String SEARCH_RESULT = "search_result";
	public final static String NETWORK = "network";
	public final static String PERMISSIONS = "permissions";
	public final static String ACCESS_TOKEN = "access_token";
	public final static String ACCESS_TOKEN_SECRET = "access_token_secret";
	public final static String APNS_TOKEN = "apns_token";
	public final static String PASSWORD = "password";
	public final static String KOE_TOKEN = "koe_token";
	public final static String META = "meta";
	public final static String ERROR = "error";
	public final static String FORCE_NUM = "force_num";
	public final static String PIC_HASH = "pic_hash";
	public final static String NUM_COMMENTS = "num_comments";
	public final static String ORIGIN = "origin";
	public final static String FROM_ID = "from_id";
	public final static String COMMENT_ID = "comment_id";
	public final static String COMMENT = "comment";
	public final static String COMMENTS = "comments";
	public final static String COMMENT_APP = "comment_app";
	public final static String PROFILE_PIC_URL = "profile_pic_url";
	public final static String ADDED = "added";
	public final static String SEND_TO_FB = "send_to_fb";
	public final static String SEND_TO_TW = "send_to_tw";
	public final static String NOTIFICATIONS = "notifications";
	public final static String TYPE = "type";
	public final static String FRIEND_DATA = "friend_data";
	public final static String DATE = "date";
	public final static String THUMBNAIL = "thumbnail";
	public final static int FEED_OLD_ORDER = 1;
	public final static int FEED_NEW_ORDER = -1;
	public final static String FOLLOWER_ID = "follower_id";
	public final static int LIKE_PICTURE = 1;
	public final static int DISLIKE_PICTURE = 0;
	public final static String USER_NO_ID = "-1";
	public final static String PHOTO_URL = "photo_url";
	public final static String FACEBOOK_DATA = "facebook_data";
	public final static String TWITTER_DATA = "twitter_data";
	public final static String PHOTO = "photo";
	public final static String UPLOAD_HASH = "upload_hash";
	public final static String FILTER_ID = "filter_id";
	public final static String GPS = "upload_hash";
	public final static String IS_PRIVATE = "is_private";
	public final static String CAPTION = "caption";
	public final static String LAT = "lat";
	public final static String LON = "lon";
	public final static String MAKER = "maker";
	public final static String QUANTITY = "quantity";
	public final static String RECEIVER = "receiver";
	public final static String STR = "str";
	public final static String APP = "app";
	public final static String FEED_MODE = "feedMode";
	public final static String PIC_DATE = "pic_date";
	public final static String HASHTAG = "hashtag";
	public final static String FRIEND_ID = "friend_id";
	public final static String NOTIFICATION_ID = "notification_id";
	public final static String NOTIFICATION_ID_ARRAY = "notification_id_array";
	public final static String OWNER_DATA = "owner_data";
	public final static String NO_PHOTOS = "no_photos";
	public final static String FIRST_PICTURE = "first_picture";
	public static final String IS_PUBLIC = "is_public";
	
	// Notification tags
	public final static String NOTIFICATION_TYPE = "notification_type";
	public final static String NOTIFICATION_IDS = "notification_ids";
	public final static String OBJECT_ID = "object_id";
	public final static String PIC_OWNER_ID = "pic_owner_id";
	public final static String USER_RECEIVES_ID = "user_receives_id";
	public final static String OPEN_URL = "open_url";
	public final static String TEMPLATE = "template";
	public final static String TEXT = "text";

	// Login modes
	public final static String LOGIN_EMAIL_MODE = "email";
	public final static String LOGIN_FACEBOOK_MODE = "facebook";
	public final static String LOGIN_TWITTER_MODE = "twitter";

	// Friendship
	public final static int RELATION_IS_NOT_FRIEND = 0;
	public final static int RELATION_IS_FRIEND = 1;
	public final static int RELATION_IS_PENDING = 2;
	public final static int RELATION_SUGGESTED = 3;
	public final static int RELATION_BLOCKED = 4;

	// Following
	public final static int RELATION_IS_NOT_FOLLOWING = 0;
	public final static int RELATION_IS_FOLLOWING = 1;

	public final static int FIRST_FROM_TIMESTAMP = 2000000000;

	public final static int AS_IT_HAPPENS = 0;
	public final static int HOURLY = 60;
	public final static int DAILY = 1440;
	public final static int MONTHLY = 10080;
	public final static int NEVER = -1;
	public final static int BACKEND_DEFAULT_INTERVAL = 720;

	public final static int POSITION_AS_IT_HAPPENS = 0;
	public final static int POSITION_HOURLY = 1;
	public final static int POSITION_DAILY = 2;
	public final static int POSITION_MONTHLY = 3;
	public final static int POSITION_NEVER = 4;

	// Friends data types
	public final static String KOEPICS = "koepics";
	public final static String FACEBOOK = "facebook";
	public final static String TWITTER = "twitter";

	// Block user
	public final static String BLOCKED_USER = "blocked_user";

	// Unblock User
	public final static String UNBLOCKED_USER = "unblocked_user";
	
	// Report
	public final static String REPORT_REASON = "report_reason";
	
	public final static int GENERAL = 10;
	public final static int COPYRIGHT = 20;
	
	// Picture settings
	private final static String PICTURE_SETTINGS = "picture_settings";
	
	private final static String DELETE = "delete";
	private final static String PRIVATE = "private";
	
	// Function Strings
	private final static String FUNCTION_REGISTER_USER = "register";
	private final static String FUNCTION_USER_SIMPLE_PROFILE = "get_user_simple_profile";
	private final static String FUNCTION_USER_SIMPLE_PROFILE_WITH_SETTINGS = "get_user_simple_profile_with_settings";
	private final static String FUNCTION_SET_SETTINGS = "set_user_settings";
	private final static String FUNCTION_CHECK_UNIQUE_EMAIL = "check_unique_email";
	private final static String FUNCTION_CHECK_UNIQUE_USER_NAME = "check_unique_user_name";
	private final static String FUNCTION_GET_FRIENDS_OR_FOLLOWER_OR_PENDING = "get_friends_or_followers_or_pending";
	private final static String FUNCTION_FEED = "feed";
	private final static String FUNCTION_FEATURED_PICTURES_AND_USERS = "get_featured_pictures_and_users";
	private final static String FUNCTION_SEND_FRIEND_REQUEST = "send_friend_requests";
	private final static String FUNCTION_SEARCH_USERS = "search_users";
	private final static String FUNCTION_SEARCH_PICTURES = "search_pictures";
	private final static String FUNCTION_LOGIN = "login";
	private final static String FUNCTION_GET_NEW_NOTIFICATIONS = "get_new_notifications";
	private final static String FUNCTION_UNFOLLOW_USER = "unfollow_user";
	private final static String FUNCTION_GET_COMMENTS_FOR_PICTURE = "get_comments_for_picture";
	private final static String FUNCTION_COMMENT_PICTURE = "comment_picture";
	private final static String FUNCTION_SINGLE_USER_FEED = "single_user_feed";
	private final static String FUNCTION_ACCEPT_FRIEND_REQUEST = "accept_friend_request";
	private final static String FUNCTION_LIKE_PICTURE = "like_picture";
	private final static String FUNCTION_UPLOAD_KOEPIC = "upload_koepic";
	private final static String FUNCTION_UPLOAD_METADATA = "upload_metadata";
	private final static String FUNCTION_NOTIFICATION_SEEN = "notification_seen";
	private final static String FUNCTION_NOTIFICATION_CLICKED = "notification_clicked";
	private final static String FUNCTION_GET_PICTURE = "get_picture";
	private final static String FUNCTION_APP_LOGOUT = "app_logout";
	private final static String FUNCTION_ADD_SOC_NETWORK_FROM_MOBILE = "add_soc_network_from_mobile";
	private final static String FUNCTION_BLOCK_USER = "block_user";
	private final static String FUNCTION_REPORT_PICTURE = "report_picture";
	private final static String FUNCTION_UNBLOCK_USER = "unblock_user";
	private final static String FUNCTION_SET_PICTURE_SETTINGS = "set_picture_settings";
	private static final Object FUNCTION_SHARE_PICTURE_IN_SOC = "share_picture_in_soc";
	
	// Signature
	public final static String FUNCTION_SIGNATURE = "signature";
	public final static String SIGNATURE = "fjEKAf$gjgk4e4q-i23";

	// Constructor
	public HttpConnections(Context context) {
		this.context = context;
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	/*****************
	 * JSON FUNCTIONS
	 *****************/

	// JSON Login User
	public JSONObject loginJsonObject(String email, String userName, String password, String network, String permissions, String access_token, String access_token_secret, String apns_token) {

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "buildLoginJsonObject login:" + userName);
		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "buildLoginJsonObject pass:" + password);

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_LOGIN);
			jsonObject.put(NETWORK, network);
			if (permissions != null) {
				jsonObject.put(PERMISSIONS, permissions);
			}
			if (access_token != null) {
				jsonObject.put(ACCESS_TOKEN, access_token);
			}
			if (access_token_secret != null) {
				jsonObject.put(ACCESS_TOKEN_SECRET, access_token_secret);
			}
			if (apns_token != null) {
				jsonObject.put(APNS_TOKEN, apns_token);
			}
			if (network.equalsIgnoreCase(EMAIL)) {
				jsonObject.put(EMAIL, email);
				if (userName != null) {
					jsonObject.put(USER_NAME, userName);
				}
				jsonObject.put(PASSWORD, password);
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Login Exception");
		}

		return jsonObject;
	}

	// JSON Login User
	public JSONObject addSocNetworkFromMobileJsonObject(String userId, String network, String permissions, String access_token, String access_token_secret) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_ADD_SOC_NETWORK_FROM_MOBILE);
			jsonObject.put(NETWORK, network);
			if (permissions != null) {
				jsonObject.put(PERMISSIONS, permissions);
			}
			if (access_token != null) {
				jsonObject.put(ACCESS_TOKEN, access_token);
			}
			if (access_token_secret != null) {
				jsonObject.put(ACCESS_TOKEN_SECRET, access_token_secret);
			}
			if (userId != null) {
				jsonObject.put(USER_ID, userId);
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Login Exception");
		}

		return jsonObject;
	}

	// JSON App Logout
	public JSONObject appLogoutJsonObject(String userId, String apns_token) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_APP_LOGOUT);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(APNS_TOKEN, apns_token);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Logout Exception");
		}

		return jsonObject;
	}

	// JSON Registro usuario
	public JSONObject registerJsonObject() {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_REGISTER_USER);
			// jsonObject.put("name", getValue(R.id.et_register_form_name));
			// jsonObject.put("email", getValue(R.id.et_register_form_email));
			// jsonObject.put("email2", getValue(R.id.et_register_form_email2));
			// jsonObject.put("telephone",
			// getValue(R.id.et_register_form_telephone));
			// jsonObject.put("userType",
			// getValue(R.id.et_register_form_userType));
			// jsonObject.put("country",
			// getValue(R.id.et_register_form_country));
			// jsonObject.put("userName",
			// getValue(R.id.et_register_form_username));
			// jsonObject.put("password",
			// getValue(R.id.et_register_form_password));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonObject;
	}

	// JSON check unique email
	public JSONObject userCheckUniqueEmailJsonObject(String userId, String email) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_CHECK_UNIQUE_EMAIL);
			if (userId != null) {
				jsonObject.put(USER_ID, userId);
			}
			jsonObject.put(EMAIL, email);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON check unique user name
	public JSONObject userCheckUniqueUserNameJsonObject(String userId, String userName) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_CHECK_UNIQUE_USER_NAME);
			if (userId != null) {
				jsonObject.put(USER_ID, userId);
			}
			jsonObject.put(USER_NAME, userName);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON user simple profile
	public JSONObject userSimpleProfileJsonObject(String userId, String targetUserId) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_USER_SIMPLE_PROFILE);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(USER_TARGET_ID, targetUserId);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON user simple profile with settings
	public JSONObject userSimpleProfileWithSettingsJsonObject(String userId, String targetUserId) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_USER_SIMPLE_PROFILE_WITH_SETTINGS);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(USER_TARGET_ID, targetUserId);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON user set settings
	public JSONObject userSetSettingsJsonObject(String userId, String caption, String privacy, String sharesLikesFacebook, String sharesLikesTwitter, int notificationsInFacebook, int notificationsInMyPhone,
			int notificationsByEmail, String email, String userName) {

		JSONObject jsonObject = new JSONObject();
		JSONObject jsonSettings = new JSONObject();

		try {
			// jsonObjeto con settings a enviar
			jsonSettings.put(USER_CAPTION, caption);
			jsonSettings.put(PRIVACY_MODE, privacy);
			jsonSettings.put(SHARE_LIKES_FB, sharesLikesFacebook);
			jsonSettings.put(SHARE_LIKES_TW, sharesLikesTwitter);
			jsonSettings.put(NOTI_FB_INTERVAL, notificationsInFacebook);
			jsonSettings.put(NOTI_APNS_INTERVAL, notificationsInMyPhone);
			jsonSettings.put(NOTI_EMAIL_INTERVAL, notificationsByEmail);
			jsonSettings.put(EMAIL, email);
			jsonSettings.put(USER_NAME, userName);

			jsonObject.put(FUNCTION_NAME, FUNCTION_SET_SETTINGS);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(USER_SETTINGS, jsonSettings);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}
	
	// JSON user set settings
	public JSONObject userSetUserPicJsonObject(String userId, String pictureHash) {

		JSONObject jsonObject = new JSONObject();
		JSONObject jsonSettings = new JSONObject();

		try {
			// jsonObjeto con settings a enviar
			jsonSettings.put(PICTURE_HASH, pictureHash);

			jsonObject.put(FUNCTION_NAME, FUNCTION_SET_SETTINGS);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(USER_SETTINGS, jsonSettings);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG) MyLog.d(TAG, "JSONException");
		}

		Log.v(TAG, jsonObject.toString());
		
		return jsonObject;
	}

	// JSON get friends
	public JSONObject userGetFriendsFollowersPendingJsonObject(String userId, String userTargetId, boolean includeSocial, boolean includeSuggested, boolean includePending) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_GET_FRIENDS_OR_FOLLOWER_OR_PENDING);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(USER_TARGET_ID, userTargetId);
			jsonObject.put(INCLUDE_SOCIAL_FRIENDS, includeSocial);
			jsonObject.put(INCLUDE_SUGGESTED_FRIENDS, includeSuggested);
			jsonObject.put(INCLUDE_PENDING_FRIENDS, includePending);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON get feed
	public JSONObject feedJsonObject(String userId, int fromHash, String fromTimestamp, int direction, int numPictures) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_FEED);
			jsonObject.put(USER_ID, userId);
			if (fromHash != -1) {
				jsonObject.put(FROM_HASH, fromHash);
			}
			if (fromTimestamp.length() > 0) {
				jsonObject.put(FROM_TIMESTAMP, fromTimestamp);
			}
			jsonObject.put(DIRECTION, direction);
			jsonObject.put(NUM_PICTURES, numPictures);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON get feed
	public JSONObject userFeedJsonObject(String userId, String targetUserId, String fromHash, String fromTimestamp, int direction, int numPictures) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_SINGLE_USER_FEED);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(USER_TARGET_ID, targetUserId);
			if (fromHash != null) {
				jsonObject.put(FROM_HASH, fromHash);
			}
			if (fromTimestamp != null) {
				jsonObject.put(FROM_TIMESTAMP, fromTimestamp);
			}
			jsonObject.put(DIRECTION, direction);
			jsonObject.put(NUM_PICTURES, numPictures);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON get picture
	public JSONObject userGetPictureJsonObject(String userId, String picHash) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_GET_PICTURE);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(PIC_HASH, picHash);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON get featured pictures and users
	public JSONObject userGetFeaturedPicturesAndUsersJsonObject(String userId) {

		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put(FUNCTION_NAME, FUNCTION_FEATURED_PICTURES_AND_USERS);
			jsonObject.put(USER_ID, userId);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON send friend request
	public JSONObject userSendFriendRequestJsonObject(String userId, String friendsId) {

		JSONObject jsonObject = new JSONObject();

		try {
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(friendsId);
			jsonObject.put(FUNCTION_NAME, FUNCTION_SEND_FRIEND_REQUEST);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(FRIENDS_IDS, jsonArray);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON accept friend request
	public JSONObject userAcceptdFriendRequestJsonObject(String userId, String followerId) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_ACCEPT_FRIEND_REQUEST);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(FOLLOWER_ID, followerId);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON send search users
	public JSONObject userSearchUsersJsonObject(String userId, String texto, boolean extended) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_SEARCH_USERS);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(SEARCH_STRING, texto);
			jsonObject.put(EXTENDED, extended);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON send search pictures
	public JSONObject userSearchPicturesJsonObject(String userId, String texto, boolean extended) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_SEARCH_PICTURES);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(SEARCH_STRING, texto);
			jsonObject.put(EXTENDED, extended);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON get new notifications
	public JSONObject userGetNewNotificationsJsonObject(String userId, String forceNum, String timeStamp) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_GET_NEW_NOTIFICATIONS);
			jsonObject.put(USER_ID, userId);
			if (forceNum != null) {
				jsonObject.put(FORCE_NUM, forceNum);
			}
			if (timeStamp != null) {
				jsonObject.put(TIMESTAMP, timeStamp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON send search pictures
	public JSONObject userUnfollowUserJsonObject(String userId, String targetId) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_UNFOLLOW_USER);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(FRIEND_ID, targetId);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON get comments for picture
	public JSONObject userGetCommentsForPictureJsonObject(String userId, String picHash, String fromId, String numComments, String origin) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_GET_COMMENTS_FOR_PICTURE);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(PIC_HASH, picHash);
			jsonObject.put(FROM_ID, fromId);
			if (numComments != null) {
				jsonObject.put(NUM_COMMENTS, numComments);
			}
			if (origin != null) {
				jsonObject.put(ORIGIN, origin);
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON add comment
	public JSONObject userCommentPictureJsonObject(String userId, String picHash, String comment, boolean sendToFb, boolean sendToTw) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_COMMENT_PICTURE);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(PIC_HASH, picHash);
			jsonObject.put(COMMENT, comment);
			jsonObject.put(SEND_TO_FB, sendToFb);
			jsonObject.put(SEND_TO_TW, sendToTw);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON like picture
	public JSONObject userLikePictureJsonObject(String userId, String picHash, int direction) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_LIKE_PICTURE);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(DIRECTION, direction);
			jsonObject.put(PIC_HASH, picHash);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG) MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON upload picture
	public JSONObject userUploadPictureJsonObject(String userId) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_UPLOAD_KOEPIC);
			jsonObject.put(USER_ID, userId);
			long unixTimestamp = System.currentTimeMillis() / 1000L;
			String uploadHash = md5(new DeviceUuidFactory(context).getDeviceUuid() + String.valueOf(unixTimestamp)) + String.valueOf(unixTimestamp);
			jsonObject.put(UPLOAD_HASH, uploadHash);

			// save upload hash para subir metadata posteriormente
			SharedPreferences prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
			Editor editor = prefs.edit();
			editor.putString(HttpConnections.UPLOAD_HASH, uploadHash);
			editor.commit();

			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- UPLOAD HASH: " + uploadHash);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON upload meta data picture
	public JSONObject userUploadMetaDataPictureJsonObject(String userId, String uploadHash, String caption, boolean sendToFb, boolean sendToTw, boolean isPrivate, String filterId, double latitud, double longitud) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_UPLOAD_METADATA);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(UPLOAD_HASH, uploadHash);
			jsonObject.put(CAPTION, caption);
			jsonObject.put(SEND_TO_FB, sendToFb);
			jsonObject.put(SEND_TO_TW, sendToTw);
			jsonObject.put(IS_PRIVATE, isPrivate);
			jsonObject.put(FILTER_ID, filterId);
			// JSONObject jsonObjectGPS = new JSONObject();
			// jsonObjectGPS.put(LAT, latitud);
			// jsonObjectGPS.put(LON, longitud);
			// jsonObject.put(GPS, jsonObjectGPS);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON notification seen
	public JSONObject userNotificationSeenJsonObject(String userId, JSONArray notificationId, String timestamp) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_NOTIFICATION_SEEN);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(NOTIFICATION_ID, notificationId);
			jsonObject.put(TIMESTAMP, timestamp);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON notification clicked
	public JSONObject userNotificationClickedJsonObject(String userId, String notificationId, String timestamp) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_NOTIFICATION_CLICKED);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(NOTIFICATION_ID, notificationId);
			jsonObject.put(TIMESTAMP, timestamp);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON block user
	public JSONObject blockUserJsonObject(String userId, String blockId) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_BLOCK_USER);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(FRIEND_ID, blockId);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}
	
	// JSON block user
	public JSONObject unblockUserJsonObject(String userId, String unblockId) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_UNBLOCK_USER);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(FRIEND_ID, unblockId);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG) MyLog.d(TAG, "JSONException");
		}

		return jsonObject;
	}

	// JSON Report picture
	public JSONObject reportPictureJsonObject(String userId, String picHash) {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(FUNCTION_NAME, FUNCTION_REPORT_PICTURE);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(PIC_HASH, picHash);
			jsonObject.put(REPORT_REASON, GENERAL);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}

		return jsonObject;

	}
	
	// JSON Set picture settings
	public JSONObject setPictureSettingsJsonObject(String userId, String picHash, boolean privatePic, boolean deletePic) {
		
		JSONObject jsonObject = new JSONObject();
		JSONObject jsonPictureSettings = new JSONObject();

		try {
			
			jsonPictureSettings.put(PRIVATE, privatePic);
			jsonPictureSettings.put(DELETE, deletePic);
			
			jsonObject.put(FUNCTION_NAME, FUNCTION_SET_PICTURE_SETTINGS);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(PIC_HASH, picHash);
			jsonObject.put(PICTURE_SETTINGS, jsonPictureSettings);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}
		
		// Log.v(TAG, jsonObject.toString());

		return jsonObject;
	}
	
	public JSONObject sharePicInSocJsonObject(String userId, String picHash, String network) {
		
		JSONObject jsonObject = new JSONObject();
		
		try {
			
			jsonObject.put(FUNCTION_NAME, FUNCTION_SHARE_PICTURE_IN_SOC);
			jsonObject.put(USER_ID, userId);
			jsonObject.put(PIC_HASH, picHash);
			jsonObject.put(NETWORK, network);
			
		} catch (JSONException e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "JSONException");
		}
		
		// Log.v(TAG, jsonObject.toString());
		
		return jsonObject;
	}

	/************************
	 * SIGNATURE & KOE_TOKEN
	 ************************/

	// Saves the new koeToken
	public void saveKoeToken(String koeToken) {
		LoggedUser.koeToken = koeToken;
		LoggedUser.save(context);
	}

	// Devuelve reverse de la string
	private String reverseString(String string) {
		String reverseString = "";
		for (int i = string.length() - 1; i >= 0; i--) {
			reverseString = reverseString + string.substring(i, i + 1);
		}
		return reverseString;
	}

	// Crea firma MD5
	public static final String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*****************
	 * HTTP SEND JSON
	 *****************/

	// Envío Json String al server http y obtenemos respuesta
	public String sendJson(JSONObject jsonObject, File photo, String koeToken) throws ClientProtocolException, IOException {

		String respuesta = "";
		String signature = null;

		// Generamos firma
		if (koeToken != null) {

			String actionData = jsonObject.toString().replaceAll("[^\\u0020-\\u007F]", "");
			signature = koeToken.substring(0, 4) + HttpConnections.SIGNATURE + reverseString((reverseString(HttpConnections.SIGNATURE) + actionData + koeToken.substring(4)));
			// if(BaseActivity.DEBUG) MyLog.d(TAG, "Signature generada: " +
			// signature);
			signature = md5(signature);

			/*
			 * if(BaseActivity.DEBUG) MyLog.d(TAG, "Signature generada MD5: " +
			 * signature); if(BaseActivity.DEBUG) MyLog.d(TAG, "--koe " +
			 * koeToken); if(BaseActivity.DEBUG) MyLog.d(TAG, "--1 " +
			 * koeToken.substring(4)); if(BaseActivity.DEBUG) MyLog.d(TAG,
			 * "--2 " + jsonObject.toString()); if(BaseActivity.DEBUG)
			 * MyLog.d(TAG, "--3 " + reverseString(HttpConnections.SIGNATURE));
			 * if(BaseActivity.DEBUG) MyLog.d(TAG, "--4 " +
			 * reverseString((reverseString(HttpConnections.SIGNATURE) +
			 * actionData + koeToken.substring(4))));
			 */
		}

		// Enviamos datos json
		try {
			// 1. construct the multipart entity of the post request
			MultipartEntity multipart = new MultipartEntity();
			multipart.addPart(ACTION_DATA, new StringBody(jsonObject.toString(), "text/plain", Charset.forName("UTF-8")));
			if (signature != null) {
				multipart.addPart(FUNCTION_SIGNATURE, new StringBody(signature, "text/plain", Charset.forName("UTF-8")));
			}
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "jsonObject sent:" + jsonObject.toString());

			if (photo != null) {
				multipart.addPart(HttpConnections.PHOTO, new FileBody(photo, "image/jpeg"));
			}

			// 2. create the post request
			HttpPost post = new HttpPost(URL_SERVER);
			post.setEntity(multipart);
			post.setHeader("User-Agent", "AudioSnaps Android " + BaseActivity.audioSnapVersion + " " + BaseActivity.userAgent);

			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "AudioSnaps Android " + BaseActivity.audioSnapVersion + " " + BaseActivity.userAgent);

			// 3. execute the post method
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(post, httpContext);

			// 4. read the response
			HttpEntity entity = response.getEntity();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, ("Http response: " + response.getStatusLine().toString()));
			BufferedInputStream bin = new BufferedInputStream(entity.getContent());
			byte[] contents = new byte[1024];
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "-- HTTP RESPONSE --");
			int bytesRead = 0;
			StringBuffer strFileContents = new StringBuffer();
			while ((bytesRead = bin.read(contents)) != -1) {
				strFileContents.append(new String(contents, 0, bytesRead));
			}

			// if(BaseActivity.DEBUG) MyLog.d(TAG, "mensaje:\"" +
			// strFileContents.toString() + "\"");
			bin.close();
			// if(BaseActivity.DEBUG) MyLog.d(TAG, "fin mensaje");

			respuesta = strFileContents.toString();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "http response: " + respuesta);

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Exception");
		}

		return respuesta;

	}

}
