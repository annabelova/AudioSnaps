package com.audiosnaps;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.audiosnaps.classes.AudioPlayer;
import com.audiosnaps.classes.AudioSnapsFileCache;
import com.audiosnaps.classes.FontLoader;
import com.audiosnaps.classes.UserClass;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.data.LoggedUserNotifications;
import com.audiosnaps.http.GetCompleteFriendsList;
import com.audiosnaps.http.GetUserSimpleProfileWithSettings;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.login.LoginRegisterUtil;
import com.audiosnaps.tabs.DiscoverTab;
import com.audiosnaps.tabs.FeedVerticalTab;
import com.audiosnaps.tabs.FriendsTab;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sromku.simple.fb.SimpleFacebook;

@SuppressLint("HandlerLeak")
public class MainActivity extends FragmentActivity {

	private final static String TAG = "MainActivity";

//	public static JSONArray jsonArrayNotifications = null;

	// notificaciones
	private static final long INTERVALO_PARA_REFRESCAR_NOTIFICACIONES = 60000;
	
	private Timer timer;
	private TimerTaskGetUserData timerTaskGetUserData;
	private FragmentManager fragmentManager;
	public static ViewFlipper viewFlipper;
	public static ListView mainFeedViewPager, myFeedViewPager;
	public static boolean fotoUploaded = false;
	public static UserClass userClass = null;
	private ImageView btnFriends, btnFeed, btnTakePic, btnMeAndMine, btnDiscover;
	private TableRow tableFooterTabs;
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	// Tabs classes
	private FriendsTab friendsClass = null;
	public static FeedVerticalTab feedClass = null;

	public static String audionsnapsUserId = "10";

	private static AudioPlayer audioPlayer;
	
	private FeedVerticalTab myFeedClass = null;
	private DiscoverTab discoverClass = null;

	private int selectedTab;

	private boolean mReturningWithResult;

	private LinearLayout feedViewPagerWrapper;

	private static  DisplayMetrics metrics = null;
	
	/// begin - by anna
	public static final String EXIT_CODE					= "EXIT_CODE";
	public static final String TAKEAUDIOSNAP_THUMBNAIL		= "TAKEAUDIOSNAP_THUMBNAIL";
	public static final String GOTO_LOCALLIBRARY		= "GOTO_LOCALLIBRARY";
	/// end - by anna

	public static DisplayMetrics getMetrics() {
		return metrics;
	}

	public static void setMetrics(DisplayMetrics metrics) {
		MainActivity.metrics = metrics;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		if (!MyLog.deactivated) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	                 .detectDiskReads()
	                 .detectDiskWrites()
	                 .detectNetwork()   // or .detectAll() for all detectable problems
	                 .penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 .detectLeakedSqlLiteObjects()
	                 .detectLeakedClosableObjects()
	                 .penaltyLog()
	                 .penaltyDeath()
	                 .build());
	     }
		*/
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(BaseActivity.DEBUG) MyLog.d(TAG, "onCreate iniciamos app");

		// Restore saved user data
		LoggedUser.load(this);

		// Si no existe AudioSnaps en SD se crea
		if (new AudioSnapsFileCache().isMediaMounted()) {
			createSdDirectory();
		} else {
			try {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "---- NO SD CARD FOUND, FORCE LOGOUT ----");
				Toast.makeText(this, getResources().getString(R.string.noSD), Toast.LENGTH_LONG).show();
				new LoginRegisterUtil(this).logout(this, this, false, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Views
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		btnFriends = (ImageView) findViewById(R.id.btnFriends);
		btnFeed = (ImageView) findViewById(R.id.btnFeed);
		btnTakePic = (ImageView) findViewById(R.id.btnTakePic);
		btnMeAndMine = (ImageView) findViewById(R.id.btnMeAndMine);
		btnDiscover = (ImageView) findViewById(R.id.btnDiscover);
		tableFooterTabs = (TableRow) findViewById(R.id.tableFooterTabs);
		
		tableFooterTabs.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				BaseActivity.tabsHeight = tableFooterTabs.getHeight();
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					tableFooterTabs.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		        } else {
		        	tableFooterTabs.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		        }
			}
		});
		
		// Set fonts
		FontLoader.setRobotoFont(findViewById(R.id.lblFriends), this, FontLoader.BOLD);
		FontLoader.setRobotoFont(findViewById(R.id.lblFeed), this, FontLoader.BOLD);
		FontLoader.setRobotoFont(findViewById(R.id.lblTakePic), this, FontLoader.BOLD);
		FontLoader.setRobotoFont(findViewById(R.id.lblMeAndMine), this, FontLoader.BOLD);
		FontLoader.setRobotoFont(findViewById(R.id.lblDiscover), this, FontLoader.BOLD);

		// Initial Tab Feed
		updateFooterTabImages(btnFriends, btnFeed, btnTakePic, btnMeAndMine, btnDiscover, BaseActivity.TAB_FEED);
		viewFlipper.setDisplayedChild(BaseActivity.TAB_FEED);
		fragmentManager = getSupportFragmentManager();
		ListView v2=(ListView)findViewById(R.id.feedViewPager2);
		mainFeedViewPager = v2;
		feedClass = new FeedVerticalTab(this, fragmentManager, mainFeedViewPager);
		feedClass.initFeed(BaseActivity.MAIN_FEED, null, null, null);
		// Activa desactiva el viewPager
		// viewPager.setPagingEnabled(false);
		// Request user data
		getUserData();
		getUserFriends();

		// Programamos el el refresco de user data
		timer = new Timer();
		timerTaskGetUserData = new TimerTaskGetUserData();
		timer.schedule(timerTaskGetUserData, INTERVALO_PARA_REFRESCAR_NOTIFICACIONES, INTERVALO_PARA_REFRESCAR_NOTIFICACIONES);

		// TAB Friends
		btnFriends.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopAudioPlayer();
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos tab friends: " + viewFlipper.getDisplayedChild());
				/*** Analytics ***/
				EasyTracker.getTracker().sendView("/u/me/friends");
				initTabFriends();
			}
		});

		// TAB Feed
		btnFeed.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos tab feed: " + viewFlipper.getDisplayedChild());
				/*** Analytics ***/
				stopAudioPlayer();
				EasyTracker.getTracker().sendView("/feed");
				initTabFeed();
			}
		});

		// TAB Me and Mine
		btnMeAndMine.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos tab me and mine: " + viewFlipper.getDisplayedChild());
				/*** Analytics ***/
				stopAudioPlayer();
				EasyTracker.getTracker().sendView("/u/me/feed");				
				initTabMeAndMine();
			}
		});

		// TAB Discover
		btnDiscover.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos tab discover: " + viewFlipper.getDisplayedChild());
				/*** Analytics ***/
				stopAudioPlayer();
				EasyTracker.getTracker().sendView("/search");
				initTabDiscover();
			}
		});

		// TAB Take a Pic
		btnTakePic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos tab take a pic");
				/*** Analytics ***/
				EasyTracker.getTracker().sendView("/pictures/take_pic");
				initTabTakeAPicture();
			}
		});
		
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
	}
	
	// Init Tabs Friends
	private void initTabFriends() {
		
		updateFooterTabImages(btnFriends, btnFeed, btnTakePic, btnMeAndMine, btnDiscover, BaseActivity.TAB_FRIENDS);
		viewFlipper.setDisplayedChild(BaseActivity.TAB_FRIENDS);
		// Iniciamos TAB friends
		if (friendsClass == null) {
			friendsClass = new FriendsTab(this, this);
			friendsClass.initFriends(BaseActivity.MAIN_LISTS, LoggedUser.id, true);
			
		}else{
			
			if(LoggedUser.hasToUpdate()) friendsClass.updateLists(LoggedUser.id, false);
			
		}
	}

	// Init Tabs Feed
	private void initTabFeed() {
		updateFooterTabImages(btnFriends, btnFeed, btnTakePic, btnMeAndMine, btnDiscover, BaseActivity.TAB_FEED);
		
		if(viewFlipper.getDisplayedChild() == BaseActivity.TAB_FEED)
			if (feedClass != null) feedClass.first();	
		
		viewFlipper.setDisplayedChild(BaseActivity.TAB_FEED);
		if (feedClass == null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			ListView viewPager = (ListView) findViewById(R.id.feedViewPager2);
			feedClass = new FeedVerticalTab(this, fragmentManager, viewPager);
			feedClass.initFeed(BaseActivity.FRIEND_FEED, null, null, null);
		}

	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
	   
	}
	
	// Init Tabs Me and Mine
	private void initTabMeAndMine() {
		updateFooterTabImages(btnFriends, btnFeed, btnTakePic, btnMeAndMine, btnDiscover, BaseActivity.TAB_ME_AND_MINE);
		//if(viewFlipper.getDisplayedChild() == BaseActivity.TAB_ME_AND_MINE)
			if (myFeedClass != null)
				{
				//myFeedClass.first();
				/* regeneración feed*/	
					
					

				}
		
		viewFlipper.setDisplayedChild(BaseActivity.TAB_ME_AND_MINE);
		// init myFeed
		if (myFeedClass == null) {
			myFeedViewPager = (ListView) findViewById(R.id.feedFriendViewPager2);
			myFeedClass = new FeedVerticalTab(this, fragmentManager, myFeedViewPager);
			myFeedClass.initFeed(BaseActivity.MY_FEED, LoggedUser.id, null, null);
		}
	}

	// Init Tabs Discover
	private void initTabDiscover() {
		updateFooterTabImages(btnFriends, btnFeed, btnTakePic, btnMeAndMine, btnDiscover, BaseActivity.TAB_DISCOVER);
		
		if(viewFlipper.getDisplayedChild() != BaseActivity.TAB_DISCOVER) 
			if(discoverClass != null) 
				discoverClass.updateListPicturesPeople(false);
		
		viewFlipper.setDisplayedChild(BaseActivity.TAB_DISCOVER);
		if (discoverClass == null) {
			discoverClass = new DiscoverTab(this, this, null);
			discoverClass.initDiscover();
		}else{
			
		}
	}

	// Launch TakePictureActivity
	private void initTabTakeAPicture() {
		Intent intent = new Intent(this, TakeAudioSnapActivity.class);
		startActivityForResult(intent, BaseActivity.TAKE_PICTURE_REQUEST_CODE);
	}

	private void getUserFriends() {

		final Handler handler = new Handler() {

			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				try {
					if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

						// Save list of friends
						SharedPreferences prefs = getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
						Editor editor = prefs.edit();
						editor.putString(BaseActivity.MY_FRIENDS_JSONARRAY, result);
						editor.commit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};

		GetCompleteFriendsList getFriendsList = new GetCompleteFriendsList(this, handler, LoggedUser.id, LoggedUser.id, true, false, false, LoggedUser.koeToken, false);
		getFriendsList.execute();
	}

	// Cambia resources al clicar en tabs
	private void updateFooterTabImages(ImageView btnFriends, ImageView btnFeed, ImageView btnTakePic, ImageView btnMeAndMine, ImageView btnDiscover, int tab) {
		switch (tab) {
		case BaseActivity.TAB_FRIENDS:
			btnFriends.setBackgroundResource(R.drawable.bt_friends_tap);
			btnFeed.setBackgroundResource(R.drawable.bt_feed);
			btnMeAndMine.setBackgroundResource(R.drawable.bt_profile);
			btnDiscover.setBackgroundResource(R.drawable.bt_search);
			break;
		case BaseActivity.TAB_FEED:
			btnFriends.setBackgroundResource(R.drawable.bt_friends);
			btnFeed.setBackgroundResource(R.drawable.bt_feed_tap);
			btnMeAndMine.setBackgroundResource(R.drawable.bt_profile);
			btnDiscover.setBackgroundResource(R.drawable.bt_search);
			break;
		case BaseActivity.TAB_ME_AND_MINE:
			btnFriends.setBackgroundResource(R.drawable.bt_friends);
			btnFeed.setBackgroundResource(R.drawable.bt_feed);
			btnMeAndMine.setBackgroundResource(R.drawable.bt_profile_tap);
			btnDiscover.setBackgroundResource(R.drawable.bt_search);
			break;
		case BaseActivity.TAB_DISCOVER:
			btnFriends.setBackgroundResource(R.drawable.bt_friends);
			btnFeed.setBackgroundResource(R.drawable.bt_feed);
			btnMeAndMine.setBackgroundResource(R.drawable.bt_profile);
			btnDiscover.setBackgroundResource(R.drawable.bt_search_tap);
			break;
		default:
			break;
		}
		selectedTab = tab;
	}

	// onNewInten acciones para las notificaciones push
	@Override
	public void onNewIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		if (extras != null) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "---- onNewIntent Notification id: " + extras.getInt(HttpConnections.NOTIFICATION_TYPE) + " ----");

			switch (extras.getInt(HttpConnections.NOTIFICATION_TYPE)) {
			case BaseActivity.I_RECEIVE_FRIEND_REQUEST:
				initTabMeAndMine();
				break;
			case BaseActivity.MY_FRIEND_REQUEST_ACCEPTED:
				initTabMeAndMine();
				break;
			case BaseActivity.MY_FRIEND_REQUEST_AUTO_ACCEPTED:
				// Nada
				break;
			case BaseActivity.I_HAVE_NEW_FOLLOWER:
				initTabMeAndMine();
				break;
			case BaseActivity.MY_PICTURE_IS_LIKED:

				break;
			case BaseActivity.MY_PICTURE_IS_COMMENTED:

				break;
			case BaseActivity.PICTURE_I_COMMENTED_IS_COMMENTED:

				break;
			case BaseActivity.FACEBOOK_FRIEND_SIGNED_IN:

				break;
			case BaseActivity.TWITTER_FRIEND_SIGNED_IN:

				break;
			case BaseActivity.YOU_WHERE_TAGGED:

				break;
			case BaseActivity.YOU_WHERE_TAGGED_IN_COMMENT:

				break;
			case BaseActivity.DB_FIRST_FRIEND_HARVEST_DONE:
				// Nada
				break;
			case BaseActivity.DB_DELETED_PICTURE:
				// Nada
				break;

			default:
				// Enviar al feed principal
				break;
			}
		}
		
		String exitCode = intent.getStringExtra(MainActivity.EXIT_CODE);
		if ( exitCode == null )
			return;
		
		if ( exitCode.equals(MainActivity.TAKEAUDIOSNAP_THUMBNAIL) ) {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Pulsamos tab me and mine: " + viewFlipper.getDisplayedChild());
			/*** Analytics ***/
			stopAudioPlayer();
			EasyTracker.getTracker().sendView("/u/me/feed");				
			initTabMeAndMine();
		} else if ( exitCode.equals(MainActivity.GOTO_LOCALLIBRARY) ) {
			
			Intent localLibraryIntent = new Intent(MainActivity.this, LocalLibraryActivity.class);
			startActivityForResult(localLibraryIntent, BaseActivity.LOCAL_LIBRARY_REQUEST_CODE);			
			
		}
	}

	// Crea directorio para almacenar AudioSnaps cacheados
	public void createSdDirectory() {

		File direct = new File(Environment.getExternalStorageDirectory() + BaseActivity.CACHE_AUDIOSNAPS_FILES);

		if (!direct.exists()) {
			direct.mkdir();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Creado directorio en la SD!");
		}
	}

	// Regresamos de otra Activity
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(BaseActivity.DEBUG) MyLog.d(TAG, "---- onActivityResult requestCode: " + requestCode + ", resultCode: " + resultCode + " ----");

		// Cuando hemos subido una foto
		if (requestCode == BaseActivity.TAKE_PICTURE_REQUEST_CODE) {
			// Update myFeed
			mReturningWithResult = true;
			
		} else if (requestCode == BaseActivity.LOCAL_LIBRARY_REQUEST_CODE) {
			
		} else {
			
			 SimpleFacebook.getInstance(this).onActivityResult(this, requestCode, resultCode, data);
			 super.onActivityResult(requestCode, resultCode, data);
			
		}
	}

	protected void onPostResume() {
	    super.onPostResume();
	    if (mReturningWithResult) {
	    	if (fotoUploaded) {
				fotoUploaded = false;
				if(BaseActivity.DEBUG) MyLog.d(TAG, "---- Redirigiendo y actualizando nuestro feed ----");
				if(myFeedClass!=null)
				{		
					
					myFeedClass.destroy();
					myFeedClass = null;
				}
				initTabMeAndMine();
				
			}
	    }
	    // Reset the boolean flag back to false for next time.
	    mReturningWithResult = false;
	}
	
	// Timer para refrescar notificaciones y user info
	private class TimerTaskGetUserData extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(BaseActivity.DEBUG) MyLog.d(TAG, "---- TimerTask getUserData() ----");
					getUserData();
				}
			});
		}
	}
	
	// Get and store user data
    private void getUserData() {

            final Context context = getApplicationContext();

            final Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                            final String userInfo = (String) msg.obj;
                            try {
                                    if (!userInfo.equalsIgnoreCase(HttpConnections.ERROR)) {

                                            JSONObject jsonObject = new JSONObject(userInfo);

                                            if(BaseActivity.DEBUG) MyLog.d(TAG, "--- picture url: " + jsonObject.getString(HttpConnections.PICTURE_URL));
                                            
                                            LoggedUser.avatarURL = jsonObject.getString(HttpConnections.PICTURE_URL);
                                            
                                            SharedPreferences prefs = context.getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
                                            Editor editor = prefs.edit();
                                            editor.putString(HttpConnections.USER_NAME, jsonObject.getString(HttpConnections.USER_NAME));
                                            editor.putString(HttpConnections.PICTURE_URL, jsonObject.getString(HttpConnections.PICTURE_URL));
                                            editor.putString(HttpConnections.USER_ID, jsonObject.getString(HttpConnections.USER_ID));
                                            editor.putString(HttpConnections.NUM_OF_PICS, jsonObject.getString(HttpConnections.NUM_OF_PICS));
                                            editor.putString(HttpConnections.NUM_OF_FRIENDS, jsonObject.getString(HttpConnections.NUM_OF_FRIENDS));
                                            editor.putString(HttpConnections.NUM_OF_FOLLOWERS, jsonObject.getString(HttpConnections.NUM_OF_FOLLOWERS));
                                            editor.putString(HttpConnections.USER_CAPTION, jsonObject.getString(HttpConnections.USER_CAPTION));
                                            editor.putString(HttpConnections.PRIVACY_MODE, jsonObject.getString(HttpConnections.PRIVACY_MODE));
                                            editor.putString(HttpConnections.HAD_PASSWORD, jsonObject.getString(HttpConnections.HAD_PASSWORD));
                                            editor.putString(HttpConnections.HAD_EMAIL, jsonObject.getString(HttpConnections.HAD_EMAIL));
                                            editor.putString(HttpConnections.EMAIL, jsonObject.getString(HttpConnections.EMAIL));
                                            editor.putString(HttpConnections.NOTIFICATION_FB_INTERVAL, jsonObject.getString(HttpConnections.NOTIFICATION_FB_INTERVAL));
                                            editor.putString(HttpConnections.NOTIFICATION_APNS_INTERVAL, jsonObject.getString(HttpConnections.NOTIFICATION_APNS_INTERVAL));
                                            editor.putString(HttpConnections.NOTIFICATION_EMAIL_INTERVAL, jsonObject.getString(HttpConnections.NOTIFICATION_EMAIL_INTERVAL));
                                            editor.putString(HttpConnections.SHARE_LIKES_FB, jsonObject.getString(HttpConnections.SHARE_LIKES_FB));
                                            editor.putString(HttpConnections.SHARE_LIKES_TW, jsonObject.getString(HttpConnections.SHARE_LIKES_TW));
                                            editor.putString(HttpConnections.PICTURE_HASH, jsonObject.getString(HttpConnections.PICTURE_HASH));
                                            editor.commit();
                                            
                                            if(BaseActivity.DEBUG) MyLog.d(TAG, "---- USER DATA SAVED ----");
                                            
                                            LoggedUserNotifications.update(getActivity());
                                            
                                    }
                            } catch (Exception e) {
                                    e.printStackTrace();
                            }
                    };
            };

            GetUserSimpleProfileWithSettings getUserSimpleProfileWithSettings = new GetUserSimpleProfileWithSettings(context, handler, LoggedUser.id, LoggedUser.id, LoggedUser.koeToken);
            getUserSimpleProfileWithSettings.execute();
    }
	
	@Override
	protected void onDestroy() {
		// Guarda koeToken en prefs
		LoggedUser.saveKoeToken(this);
		
		try {
			timer.cancel();
			timer.purge();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "---- TimerTask cancelado ----");
		} catch (Exception e) {
			e.printStackTrace();
		}
		friendsClass = null;
		feedClass = null;
		discoverClass = null;
		System.gc();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onBackPressed(){
		
		if(selectedTab == BaseActivity.TAB_FEED) if(feedClass.onBackPressed()) return;
		if(selectedTab == BaseActivity.TAB_ME_AND_MINE) if(myFeedClass.onBackPressed()) return;
		super.onBackPressed();
	}
	
	@Override
	public void onContextMenuClosed(Menu menu) {
	    super.onContextMenuClosed(menu);
	    
		if(selectedTab == BaseActivity.TAB_FEED) feedClass.onContextMenuClosed(menu);
		if(selectedTab == BaseActivity.TAB_ME_AND_MINE) myFeedClass.onContextMenuClosed(menu);
	}
	
	
	@Override
	protected void onStart() {
	    super.onStart();
	    EasyTracker.getInstance().setContext(this.getBaseContext());
	    EasyTracker.getInstance().activityStart(this); // Add this method
	}

	@Override
	protected void onStop() {
	    super.onStop();
	    EasyTracker.getInstance().activityStop(this); // Add this method
	    stopAudioPlayer();
	}
	
	private Activity getActivity(){
		return this;
	}

	public static void setAudioPlayer(AudioPlayer ap) {
		// TODO Auto-generated method stub
		audioPlayer = ap;
	}

	public static AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
	
	public static void stopAudioPlayer()
	{
		if(audioPlayer!=null)
			audioPlayer.stop();
		audioPlayer=null;
	}
	
	

}
