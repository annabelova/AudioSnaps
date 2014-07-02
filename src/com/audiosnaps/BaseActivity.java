package com.audiosnaps;

import java.io.File;
import java.util.ArrayList;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.audiosnaps.log.MyLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

public class BaseActivity extends Application {

	private static final String TAG = "BaseActivity";

	public static final boolean DEBUG = BuildConfig.DEBUG;

	// ImageLoader configs
	private final static String CACHE_IMAGES_DIRECTORY = "UniversalImageLoader/AudioSnaps/Cache";
	public static final String CACHE_AUDIOSNAPS_FILES = "/AudioSnaps/";
	public static final String TMP_AUDIOSNAPS_FILES = "/AudioSnaps/Tmp";
	public static ImageLoaderConfiguration config;
	public static DisplayImageOptions options, optionsGridImage, optionsAvatarImage, defaultOptions, optionsAudioSnapImage;

	// App Vars
	public static String audioSnapVersion = "1.0.0";
	public static String userAgent = "";
	public static ArrayList<String> arrayIntervalosNotificaciones;
	public static final String SHARED_PREFERENCES = "AudioSnapsPreferences";
	public static final String SAVE_IN_LIBRARY = "saveInLibrary";
	public static final String ACTIVATED = "1";
	public static final String DEACTIVATED = "0";
	public static final int IS_FRIEND_FOLLOWER_OK = 1;
	public static final String AUDIO_SNAP_OBJECT = "audioSnapObject";
	public static final int MAX_SIZE_COMMENT = 140;
	public static final String NOTIFICATIONS_REQUESTED = "30";
	public final static String FIRST_FROM_TIMESTAMP = "2000000000";
	public final static String MY_FRIENDS_JSONARRAY = "myFriendsJsonArray";
	public final static String GMC_REGISTER_ID = "gmcRegisterId";
	public final static String AUDIOSNAPS_USER_ID = "10";

	// Request codes
	public static final int TAKE_PICTURE_REQUEST_CODE = 3245;
	public static final int USER_CONFIG_REQUEST_CODE = 2956;
	public static final int COMMENTS_REQUEST_CODE = 9999;

	// Tabs ids
	public final static int TAB_NO_POSITION = -1;
	public final static int TAB_FRIENDS = 0;
	public final static int TAB_FEED = 1;
	public final static int TAB_ME_AND_MINE = 2;
	public final static int TAB_DISCOVER = 3;
	public final static int TAB_CONFIG = 4;
	public final static int TAB_FRIEND_FEED = 5;
	public final static int TAB_USER_LISTS = 6;

	// Screen
	public final static String SCREEN_WIDTH = "screenWidth";
	public final static String SCREEN_HEIGHT = "screenHeight";
	public final static String AUDIOSNAP_WIDTH = "audioSnapWidth";
	public static float screenRelation = 0.0f;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	public static int audioSnapWidth = 0;
	public static int tabsHeight = 0;

	// Notification codes
	public final static int I_RECEIVE_FRIEND_REQUEST = 10;
	public final static int MY_FRIEND_REQUEST_ACCEPTED = 20;
	public final static int MY_FRIEND_REQUEST_AUTO_ACCEPTED = 21;
	public final static int I_HAVE_NEW_FOLLOWER = 30;
	public final static int MY_PICTURE_IS_LIKED = 40;
	public final static int MY_PICTURE_IS_COMMENTED = 50;
	public final static int PICTURE_I_COMMENTED_IS_COMMENTED = 90;
	public final static int FACEBOOK_FRIEND_SIGNED_IN = 100;
	public final static int TWITTER_FRIEND_SIGNED_IN = 110;
	public final static int YOU_WHERE_TAGGED = 120;
	public final static int YOU_WHERE_TAGGED_IN_COMMENT = 130;
	public final static int DB_FIRST_FRIEND_HARVEST_DONE = 140;
	public final static int DB_DELETED_PICTURE = 150;

	public final static int kKPNotificationCode_IReceiveFriendRequest = 10;
	public final static int kKPNotificationCode_MyFriendRequestAccepted = 20;
	public final static int kKPNotificationCode_FriendRequestAutoAccepted = 21;
	public final static int kKPNotificationCode_IHaveNewFollower = 30;
	public final static int kKPNotificationCode_MyPictureIsLiked = 40;
	public final static int kKPNotificationCode_MyPictureIsCommented = 50;
	public final static int kKPNotificationCode_PictureICommentedIsCommented = 90;
	public final static int kKPNotificationCode_FacebookFriendSignedIn = 100;
	public final static int kKPNotificationCode_TwitterFriendSignedIn = 110;
	public final static int kKPNotificationCode_YouWhereTagged = 120;
	public final static int kKPNotificationCode_YouWhereTaggedInComment = 130;
	public final static int kKPNotificationCode_DBFirstFriendHarvestDone = 140;
	public final static int kKPNotificationCode_DBDeletedPicture = 150;

	// Logged accounts
	public final static String TWITTER_IS_LOGGED_IN = "twitter_is_logged_in";
	public final static String FACEBOOK_IS_LOGGED_IN = "facebook_is_logged_in";

	// Animation vars
	public static final int SWIPE_TIME_ANIMATION = 200;
	public static final int POSITION_CENTER = 0;
	public static final int POSITION_TOP = 1;
	public static final int POSITION_BOTTOM = 2;

	// Feed vars
	public static final int MAIN_FEED = 0;
	public static final int MY_FEED = 1;
	public static final int FRIEND_FEED = 2;
	public static final int ONE_PICTURE_FEED = 3;

	// Friends list vars
	public static final int MAIN_LISTS = 0;
	public static final int FRIEND_LIST = 1;

	// Login vars
	public static final String IS_LOGGED = "isLogged";
	public static final String LOGGED_MODE = "loggedMode";
	public static final String FACEBOOK_LOGIN = "facebookLogin";
	public static final String FACEBOOK_PUBLISH_ACTIONS = "facebookPublishActions";
	public static final String TWITTER_LOGIN = "twitterLogin";
	public static final String MAIL_LOGIN = "mailLogin";

	// Camera vars
	public static final String PIC_FORMAT = "picFormat";
	public static final String FLASH = "flash";
	public static final String CAMERA = "camera";

	public static final int ORIENTATION_PORTRAIT_NORMAL = 0;
	public static final int ORIENTATION_PORTRAIT_INVERTED = 1;
	public static final int ORIENTATION_LANDSCAPE_NORMAL = 2;
	public static final int ORIENTATION_LANDSCAPE_INVERTED = 3;

	public static final int FORMAT_1_1 = 0;
	public static final int FORMAT_4_3 = 1;

	public static final int BACK_CAMERA = 0;
	public static final int FRONT_CAMERA = 1;

	public static final int FLASH_OFF = 0;
	public static final int FLASH_ON = 1;
	public static final int FLASH_AUTO = 2;

	public static final String SAVE_IN_GALLERY = "saveInGallery";

	public static final int REQUEST_CODE_TWITTER_LOGIN = 10;

	public static final int FACEBOOK_HAS_PUBLISH_ACTIONS = 1;
	public static final int FACEBOOK_NOT_HAS_PUBLISH_ACTIONS = 2;

	public static final String POSITION = "position";

	public static SimpleFacebookConfiguration configuration;

	@Override
	public void onCreate() {
		super.onCreate();

		// AudioSnaps version
		PackageInfo pInfo;
		try {
			pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			audioSnapVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		// User Agent
		userAgent = System.getProperty("http.agent");

		// init araylist
		arrayIntervalosNotificaciones = new ArrayList<String>();
		arrayIntervalosNotificaciones.add(getResources().getString(R.string.AS_IT_HAPPENS));
		arrayIntervalosNotificaciones.add(getResources().getString(R.string.HOURLY));
		arrayIntervalosNotificaciones.add(getResources().getString(R.string.DAILY));
		arrayIntervalosNotificaciones.add(getResources().getString(R.string.MONTHLY));
		arrayIntervalosNotificaciones.add(getResources().getString(R.string.NEVER));

		// Facebook

		Permissions[] permissions = new Permissions[] { Permissions.BASIC_INFO, Permissions.EMAIL };
		
		configuration = new SimpleFacebookConfiguration.Builder()
		    .setAppId(getResources().getString(R.string.app_id))
		    .setNamespace("audiosnaps")
		    .setPermissions(permissions)
		    .build();
		
		SimpleFacebook.setConfiguration(configuration);

		File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), CACHE_IMAGES_DIRECTORY);
		// File cacheDir = getApplicationContext().getCacheDir();

		// ImageLoader configurations
		optionsAvatarImage = new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.ARGB_4444).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheInMemory(true).cacheOnDisc(true).build();

		optionsGridImage = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.ic_launcher).bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).cacheInMemory(true)
				.cacheOnDisc(true).build();

		optionsAudioSnapImage = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565).
		imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
		.cacheInMemory(true)
		.cacheOnDisc(false)
		.build();

		// config options imageLoader
		// defaultOptions = new
		// DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		// .cacheInMemory().cacheOnDisc().build();
		//
		// options = new
		// DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.loading_blipoint_app).cacheInMemory().cacheOnDisc().build();
		//
		// optionsAvatarImage = new
		// DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.ARGB_4444)
		// .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheInMemory().cacheOnDisc().build();
		//
		// optionsListOptimizedImage = new
		// DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.RGB_565)
		// .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).cacheInMemory().cacheOnDisc().build();
		// .displayer(new FadeInBitmapDisplayer(500))

		// UTILIZAR COMPRESSFORMAT DA PROBLEMAS CON UNIVERSAL IMAGE LOADER 1.8
		config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
		// .discCacheExtraOptions(600, 600, CompressFormat.WEBP,
		// 75).memoryCacheExtraOptions(400, 400)
		// .threadPoolSize(5).threadPriority(Thread.NORM_PRIORITY).denyCacheImageMultipleSizesInMemory().memoryCache(new
		// UsingFreqLimitedMemoryCache(4 * 1024 * 1024))
		// .discCache(new
		// UnlimitedDiscCache(cacheDir)).defaultDisplayImageOptions(defaultOptions).build();

		/*
		 * HttpParams params = new BasicHttpParams(); // Turn off stale
		 * checking. Our connections break all the time anyway, // and it's not
		 * worth it to pay the penalty of checking every time.
		 * HttpConnectionParams.setStaleCheckingEnabled(params, false); //
		 * Default connection and socket timeout of 10 seconds. Tweak to taste.
		 * HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
		 * HttpConnectionParams.setSoTimeout(params, 10 * 1000);
		 * HttpConnectionParams.setSocketBufferSize(params, 8192);
		 * 
		 * // Don't handle redirects -- return them to the caller. Our code //
		 * often wants to re-POST after a redirect, which we must do ourselves.
		 * HttpClientParams.setRedirecting(params, false); // Set the specified
		 * user agent and register standard protocols.
		 * HttpProtocolParams.setUserAgent(params, "some_randome_user_agent");
		 * SchemeRegistry schemeRegistry = new SchemeRegistry();
		 * schemeRegistry.register(new Scheme("http",
		 * PlainSocketFactory.getSocketFactory(), 80));
		 * schemeRegistry.register(new Scheme("https",
		 * SSLSocketFactory.getSocketFactory(), 443));
		 * 
		 * ClientConnectionManager manager = new
		 * ThreadSafeClientConnManager(params, schemeRegistry);
		 * 
		 * 
		 * config = new ImageLoaderConfiguration
		 * .Builder(getApplicationContext())
		 * .defaultDisplayImageOptions(defaultOptions) .discCache(new
		 * UnlimitedDiscCache(cacheDir)) .threadPoolSize(1) .memoryCache(new
		 * WeakMemoryCache()) .imageDownloader(new
		 * HttpClientImageDownloader(getApplicationContext(), new
		 * DefaultHttpClient(manager, params))) .build();
		 */

		// Screen width & AudioSnap width
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		BaseActivity.screenWidth = displayMetrics.widthPixels;
		BaseActivity.screenHeight = displayMetrics.heightPixels;
		BaseActivity.screenRelation = Float.valueOf(BaseActivity.screenHeight) / Float.valueOf(BaseActivity.screenWidth);
		BaseActivity.audioSnapWidth = (int) (displayMetrics.widthPixels * 0.96875);

		// init imageloaders
		ImageLoader.getInstance().init(config);
		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "ImageLoader.getInstance().init(config)");
	}

}
