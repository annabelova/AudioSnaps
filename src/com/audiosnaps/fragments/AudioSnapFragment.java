package com.audiosnaps.fragments;

import java.io.File;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.audiosnap.library.util.BitmapUtil;
import com.audiosnap.library.util.DateUtil;
import com.audiosnaps.BaseActivity;
import com.audiosnaps.FullScreenPictureActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;
import com.audiosnaps.WebviewActivity;
import com.audiosnaps.activities.CommentsActivity;
import com.audiosnaps.activities.UserConfigActivity;
import com.audiosnaps.activities.UserFeedActivity;
import com.audiosnaps.activities.UserFriendsFollowersActivity;
import com.audiosnaps.activities.UserNotificationActivity;
import com.audiosnaps.adapters.CommentsAdapter;
import com.audiosnaps.adapters.MentionedFriendsListAdapter;
import com.audiosnaps.adapters.NotificationListAdapter;
import com.audiosnaps.classes.Animaciones;
import com.audiosnaps.classes.AudioPlayer;
import com.audiosnaps.classes.AudioSnapsFileCache;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.data.LoggedUserNotifications;
import com.audiosnaps.facebook.FacebookManager;
import com.audiosnaps.gesture.SwipeDetector;
import com.audiosnaps.http.AddSocNetworkFromMobile;
import com.audiosnaps.http.BlockUser;
import com.audiosnaps.http.CommentPicture;
import com.audiosnaps.http.GetCommentsForPicture;
import com.audiosnaps.http.GetCompleteFriendsList;
import com.audiosnaps.http.GetUserSimpleProfile;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.LikePicture;
import com.audiosnaps.http.NotificationClicked;
import com.audiosnaps.http.ReportPicture;
import com.audiosnaps.http.SendFriendRequests;
import com.audiosnaps.http.SetPictureSettings;
import com.audiosnaps.http.SetUserPic;
import com.audiosnaps.http.SharePictureInSoc;
import com.audiosnaps.http.UnblockUser;
import com.audiosnaps.http.UnfollowUser;
import com.audiosnaps.json.model.Comment;
import com.audiosnaps.json.model.FeedObject;
import com.audiosnaps.json.model.Notification;
import com.audiosnaps.json.model.SimpleProfile;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.login.LoginRegisterUtil;
import com.audiosnaps.share.Email;
import com.audiosnaps.share.Whatsapp;
import com.audiosnaps.twitter.TwitterLoginActivity;
import com.audiosnaps.view.CommentsEditText;
import com.audiosnaps.view.CommentsFormatter;
import com.audiosnaps.view.TextViewFixTouchConsume;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.sromku.simple.fb.SimpleFacebook;

@SuppressLint("HandlerLeak")
public class AudioSnapFragment extends Fragment implements UpdateableFragment {

	// Intervalos para refrescar (milisegundos)
	// comentarios
	public static final int INTERVALO_PARA_REFRESCAR_COMENTARIOS = 30000;

	private final static String TAG = "AudioSnapFragment";

	private static final int BLOCK_ITEM = 0;
	private static final int CANCEL_ITEM = 1;
	private static final int SET_PUBLIC_OR_PRIVATE_ITEM = 2;
	private static final int DELETE_PICTURE_ITEM = 3;
	private static final int REPORT_PICTURE_ITEM = 4;
	private static final int SET_AS_PROFILE_PICTURE_ITEM = 5;
	private static final int SEND_BY_EMAIL_ITEM = 6;
	private static final int SEND_BY_WHATSAPP_ITEM = 7;
	private static final int SEND_BY_FACEBOOK_ITEM = 8;
	private static final int SEND_BY_TWITTER_ITEM = 9;
	/* añadir 2 opciones más */
	private static final int CONFIG_ITEM = 20;
	private static final int LOGOUT_ITEM = 21;

	public static final int REQUEST_CODE_TWITTER_LOGIN_COMMENTS = 168;
	public static final int REQUEST_CODE_TWITTER_LOGIN_MENU = 175;

	// json
	private JSONArray jsonArrayFriends;

	private SimpleProfile userInfo;

	private List<Comment> commentsList;

	// adapters
	private MentionedFriendsListAdapter mentionedFriendsListAdapter;
	private CommentsAdapter commentsAdapter;

	// views
	private View view;
	private CommentsEditText txtCommentFeed;
	private ImageView imgUserAvatarFeed, imageViewAudioSnap, picCorners,
			btnStamp, imgAvatar, btnOptionsNowFeed;
	private Button btnLikesFeed, btnComentariosFeed, btnMasFeed, btnFollowFeed,
			btnConfigMe, btnLogoutMe;
	private Animaciones animaciones;
	private LinearLayout feedUserInfo, feedComments, linearButtons,
			linearUserFriends, linearUserPictures, linearUserFollowers,
			linearImageContainerFeed, linearLayoutCommentFeed,
			listCommentsFeed;
	private RelativeLayout feedHeader, feedPhoto, captionBox1, imageBox;
	private TextView feedCaptionFooter1, lblUserInfoUserNameFeed,
			lblPrivateFeed, lblUserName, lblPicturesMe, lblFriends,
			lblFollowersMe, lblCaptionMe, lblUserInfoRelationFeed,
			lblUserInfoRelationUserInfoFeed;
	private TextViewFixTouchConsume feedCaptionBox1;
	private TableLayout tableViewCommentsFeed;
	private TableRow tableLikesCommentsFeed;
	private ProgressBar progressBarAudioSnap, progressBarUserInfoAvatarFeed,
			progressBarButtonFollowFeed;
	private CheckBox btnFacebookCommentFeed, btnTwitterCommentFeed;
	private ImageView btnRefresh;

	// primitive
	private String stringComments/* , targetId */;
	private boolean noPhoto = false, isUnfollowed = false, isLiked = false;
	private int comments = -1, likes = -1;

	// other classes
	public AudioPlayer audioPlayer;
	private SwipeDetector swipeDetector;

	private Timer refreshCommentsTimer;

	private FeedObject feedObject;

	private SimpleFacebook mSimpleFacebook;

	private NotificationListAdapter notificationListAdapter;

	private Button btnNotifications;

	private boolean visibleHeader = true;

	private boolean visibleUserInfo = false;

	private boolean contextMenuClick=false;

	private LinearLayout parentView;

	private OnPreDrawListener onPreDrawListener;

	private int position;

	private ImageLoader imageLoader;
	
	// Fragment new instance
	public static final AudioSnapFragment newInstance(FeedObject feedObject) {
		AudioSnapFragment audioSnapFragment = new AudioSnapFragment();
		Bundle bundle = new Bundle(1);
		bundle.putParcelable(BaseActivity.AUDIO_SNAP_OBJECT, feedObject);
		audioSnapFragment.setArguments(bundle);
		return audioSnapFragment;
	}

	
	
	public ImageLoader getImageLoader() {
		return imageLoader;
	}



	public void setImageLoader(ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		// View fragment
		view = inflater.inflate(R.layout.feed_fragment, container, false);

		// Views contenedores
		feedUserInfo = (LinearLayout) view.findViewById(R.id.feedUserInfo);
		feedHeader = (RelativeLayout) view.findViewById(R.id.feedHeaderContent);
		feedPhoto = (RelativeLayout) view.findViewById(R.id.feedPhoto);
		feedComments = (LinearLayout) view.findViewById(R.id.feedComments);

		// Views
		imageViewAudioSnap = (ImageView) view
				.findViewById(R.id.imageViewAudioSnap);
		picCorners = (ImageView) view.findViewById(R.id.PicCorners);
		btnStamp = (ImageView) view.findViewById(R.id.btnStamp);
		feedCaptionBox1 = (TextViewFixTouchConsume) view
				.findViewById(R.id.feedCaptionBox1);
		feedCaptionFooter1 = (TextView) view
				.findViewById(R.id.feedCaptionFooter1);
		captionBox1 = (RelativeLayout) view.findViewById(R.id.captionBox1);
		imageBox = (RelativeLayout) view.findViewById(R.id.imageBox);
		tableViewCommentsFeed = (TableLayout) view
				.findViewById(R.id.tableViewCommentsFeed);
		btnMasFeed = (Button) view.findViewById(R.id.btnMasFeed);
		btnOptionsNowFeed = (ImageView) view
				.findViewById(R.id.btnOptionsNowFeed);
		imgUserAvatarFeed = (ImageView) view
				.findViewById(R.id.imgUserAvatarFeed);
		btnLikesFeed = (Button) view.findViewById(R.id.btnLikesFeed);
		btnComentariosFeed = (Button) view
				.findViewById(R.id.btnComentariosFeed);
		lblPrivateFeed = (TextView) view.findViewById(R.id.lblPrivateFeed);
		lblUserInfoUserNameFeed = (TextView) view
				.findViewById(R.id.lblUserInfoUserNameFeed);
		progressBarAudioSnap = (ProgressBar) view
				.findViewById(R.id.progressBarAudioSnap);
		progressBarAudioSnap.setVisibility(View.VISIBLE);
		View feedTopMargin = (View) view.findViewById(R.id.feedTopMargin);
		progressBarUserInfoAvatarFeed = (ProgressBar) view
				.findViewById(R.id.progressBarUserInfoAvatarFeed);
		progressBarButtonFollowFeed = (ProgressBar) view
				.findViewById(R.id.progressBarButtonFollowFeed);
		lblUserName = (TextView) view.findViewById(R.id.lblUserNameFeed);
		lblPicturesMe = (TextView) view.findViewById(R.id.lblPicturesFeed);
		lblFriends = (TextView) view.findViewById(R.id.lblFriendsFeed);
		lblFollowersMe = (TextView) view.findViewById(R.id.lblFollowersFeed);
		lblCaptionMe = (TextView) view.findViewById(R.id.lblCaptionFeed);
		imgAvatar = (ImageView) view.findViewById(R.id.imgUserInfoAvatarFeed);
		btnFollowFeed = (Button) view.findViewById(R.id.btnFollowFeed);
		linearButtons = (LinearLayout) view.findViewById(R.id.linearButtons);
		/* creación boton notificaciones */
		btnNotifications = (Button) view.findViewById(R.id.btnNotifications);
		btnConfigMe = (Button) view.findViewById(R.id.btnConfigMe);
		btnLogoutMe = (Button) view.findViewById(R.id.btnLogoutMe);
		lblUserInfoRelationFeed = (TextView) view
				.findViewById(R.id.lblUserInfoRelationFeed);
		lblUserInfoRelationUserInfoFeed = (TextView) view
				.findViewById(R.id.lblUserRelationUserInfoFeed);
		linearUserFriends = (LinearLayout) view
				.findViewById(R.id.linearUserFriends);
		linearUserFollowers = (LinearLayout) view
				.findViewById(R.id.linearUserFollowers);
		linearUserPictures = (LinearLayout) view
				.findViewById(R.id.linearUserPictures);
		tableLikesCommentsFeed = (TableRow) view
				.findViewById(R.id.tableLikesCommentsFeed);
		txtCommentFeed = (CommentsEditText) view
				.findViewById(R.id.txtCommentFeed);
		linearImageContainerFeed = (LinearLayout) view
				.findViewById(R.id.linearImageContainerFeed);
		linearLayoutCommentFeed = (LinearLayout) view
				.findViewById(R.id.linearLayoutCommentFeed);
		listCommentsFeed = (LinearLayout) view
				.findViewById(R.id.listCommentsFeed);

		// Facebook
		btnFacebookCommentFeed = (CheckBox) view
				.findViewById(R.id.btnFacebookCommentFeed);

		// Twitter
		btnTwitterCommentFeed = (CheckBox) view
				.findViewById(R.id.btnTwitterCommentFeed);

		// Scale views
		animaciones = new Animaciones(getActivity());
		animaciones.scaleViewPx(imageViewAudioSnap,
				BaseActivity.audioSnapWidth, BaseActivity.audioSnapWidth);
		animaciones.scaleViewPx(picCorners, BaseActivity.audioSnapWidth,
				BaseActivity.audioSnapWidth);
		animaciones.scaleViewPx(feedTopMargin, BaseActivity.screenWidth,
				(int) (BaseActivity.screenWidth * 0.025));

		if (BaseActivity.DEBUG)
			MyLog.v(TAG, "SCREEN RELATION: " + BaseActivity.screenRelation);

		feedObject = (FeedObject) getArguments().getParcelable(
				BaseActivity.AUDIO_SNAP_OBJECT);

		if (feedObject.no_photos) {
			noPhoto = true;
		}
		// Activamos botón update feed en primer fragment, MAIN_FEED

		btnRefresh = (ImageView) view
				.findViewById(R.id.imgRefreshFeed);
		btnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity.feedClass.getFeed(LoggedUser.id, true);
			}
		});
		

		
		
		
		actualizarBtnContext();
		registerForContextMenu(btnMasFeed);

		// Init fragment
		
		initAudioSnapFragment();
		
		return view;
	}

	
	public void controlBotonRefresco()
	{
		if (feedObject.first_picture
				&& (feedObject.feedMode == BaseActivity.MAIN_FEED || feedObject.feedMode == BaseActivity.FRIEND_FEED )) {			
			btnRefresh.setVisibility(View.VISIBLE);			
		}
		else
		{
			btnRefresh.setVisibility(View.GONE);
		}
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub 
		 
		 
		   ViewTreeObserver vto = getView().getViewTreeObserver();
		   /*
		   onPreDrawListener=new ViewTreeObserver.OnPreDrawListener() {
			   int counter=0;
		       public boolean onPreDraw() { 
		           int finalHeight = getView().getMeasuredHeight();
		           int finalWidth = getView().getMeasuredWidth();
	               MyLog.i(TAG, "!!! addOnPreDrawListener parent "+parentView+"  height "+finalHeight+" "+finalWidth);
	             parentView.setLayoutParams(new GridView.LayoutParams(finalWidth,finalHeight));
	               if(counter>30)
	            	   getView().getViewTreeObserver().removeOnPreDrawListener(this);
	               counter++;
		           return true;
		       }
		   };
		   vto.addOnPreDrawListener(onPreDrawListener);
		   */		
		   /*
			  LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)getView().getLayoutParams();
			   params.setMargins(0, -50, 0, 0); 
			   getView().setLayoutParams(params);		
		   */
		  // imageLoader.init(BaseActivity.config);
		   super.onActivityCreated(savedInstanceState);
			View v=getView();	
			if(parentView!=null && parentView!=v)
			{								
				((ViewGroup)getView().getParent()).removeView(v);
				parentView.addView(v); 			
			}
	}

	private void actualizarBtnContext() {
		
		if (feedObject.user_data != null) {
			if (feedObject.user_data.user_id
					.equals(MainActivity.audionsnapsUserId)) {
				
				btnOptionsNowFeed.setVisibility(View.GONE);
			} else if (feedObject.user_data.user_id.equals(LoggedUser.id)) {
				LoggedUser.initPicturePublicOrPrivate(feedObject.pic_hash,
						feedObject.is_public);
				/* Cambios */
				// btnOptionsNowFeed.setVisibility(View.GONE);
				btnOptionsNowFeed.setTag("lodguedUserContextMenu");
				registerForContextMenu(btnOptionsNowFeed);
				btnOptionsNowFeed.setVisibility(View.VISIBLE);
			} else {
				registerForContextMenu(btnOptionsNowFeed);
				btnOptionsNowFeed.setVisibility(View.VISIBLE);
			}
		} else {
			
			btnOptionsNowFeed.setVisibility(View.GONE);
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		contextMenuClick=true;
		super.onCreateContextMenu(menu, v, menuInfo);
		
		if (v.getClass() == Button.class) {

			// inflater.inflate(R.menu.context_menu, menu);

			if (feedObject.user_data.user_id.equals(LoggedUser.id)) {

				feedObject.is_public = LoggedUser
						.getPublic(feedObject.pic_hash);
				// Add 'set as profile picture' menu item
				menu.add(0, SET_PUBLIC_OR_PRIVATE_ITEM, 0,
						(feedObject.is_public) ? R.string.SET_PRIVATE
								: R.string.SET_PUBLIC);
				// // Add 'delete picture' menu item
				// menu.add(0, DELETE_PICTURE_ITEM, 0, R.string.DELETE_PICTURE);
				// Add 'set as profile picture' menu item
				menu.add(0, SET_AS_PROFILE_PICTURE_ITEM, 0,
						R.string.SET_AS_PROFILE_PICTURE);
			} else {
				// Add 'report picture' menu item
				menu.add(0, REPORT_PICTURE_ITEM, 0, R.string.REPORT_PICTURE);
			}

			if (feedObject.is_public) {
				// Add 'send by email' menu item
				menu.add(0, SEND_BY_EMAIL_ITEM, 0, R.string.SEND_BY_EMAIL);
				// Add 'send by whatsapp' menu item
				menu.add(0, SEND_BY_WHATSAPP_ITEM, 0, R.string.SEND_BY_WHATSAPP);
				// Add 'share in facebook' menu item
				menu.add(0, SEND_BY_FACEBOOK_ITEM, 0,
						R.string.SHARE_IN_FACEBOOK);
				// Add 'share in twitter' menu item
				menu.add(0, SEND_BY_TWITTER_ITEM, 0, R.string.SHARE_IN_TWITTER);
			}

		} else {
			
			
			
			if (v.getTag() != null
					&& v.getTag().equals("lodguedUserContextMenu")
					&& userInfo != null
					&& LoggedUser.id.equals(userInfo.user_id)) {
				menu.add(0, CONFIG_ITEM, 0, R.string.CONFIG);
				menu.add(0, LOGOUT_ITEM, 0, R.string.LOGOUT);
			} else if (userInfo != null) {

				// Add block/unblock menu item
				menu.add(
						0,
						BLOCK_ITEM,
						0,
						(userInfo.is_follower != HttpConnections.RELATION_BLOCKED) ? R.string.BLOCK_USER
								: R.string.UNBLOCK_USER);
				// Add cancel menu items
				menu.add(0, CANCEL_ITEM, 0, R.string.CANCEL);

			}
			
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// Show/Hide 'send by email' menu item
		if (menu.findItem(SEND_BY_EMAIL_ITEM) != null)
			menu.getItem(SEND_BY_EMAIL_ITEM).setEnabled(feedObject.is_public);
		// Show/Hide 'send by whatsapp' menu item
		if (menu.findItem(SEND_BY_WHATSAPP_ITEM) != null)
			menu.getItem(SEND_BY_WHATSAPP_ITEM)
					.setEnabled(feedObject.is_public);
	}

	
	
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
	    contextMenuClick=false;
	    

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {/*
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();		
	    int index = info.position;
	    View view = info.targetView;*/
	    boolean menuSelect=contextMenuClick;
	    contextMenuClick=false;
		
		int tab = MainActivity.viewFlipper.getDisplayedChild();

		// Toast.makeText(getActivity(), "Tab: " + tab + ", Feed mode: " +
		// feedObject.feedMode, Toast.LENGTH_LONG).show();
		// Toast.makeText(getActivity(), "report picture item",
		// Toast.LENGTH_LONG).show();

		if (getUserVisibleHint() && menuSelect) {
			
			// En un ViewPager se propaga por todos los fragments el evento de
			// seleccionar item del menú
			// por eso debemos comprobar a que opción corresponde e ignorar
			// todas las otras (no posibles).

			// feed propio abierto en tab de 'me and mine'
			if ((feedObject.feedMode == BaseActivity.MY_FEED && tab == BaseActivity.TAB_ME_AND_MINE)
					||
					// feed principal abierto en tab de 'main feed'
					(feedObject.feedMode == BaseActivity.MAIN_FEED && tab == BaseActivity.TAB_FEED)
					||
					// one picture feed se abre desde discover 'pictures'
					(feedObject.feedMode == BaseActivity.ONE_PICTURE_FEED && tab == BaseActivity.TAB_DISCOVER)
					||
					// friend feed se puede abrir desde cualquier lado
					(feedObject.feedMode == BaseActivity.FRIEND_FEED)) {

				// Perform action according to selected item from context menu
				switch (item.getItemId()) {
				case SET_PUBLIC_OR_PRIVATE_ITEM:
					final Handler handlerPublicOrPrivate = new Handler() {
						public void handleMessage(Message msg) {
							final String result = (String) msg.obj;
							if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
								feedObject.is_public = !feedObject.is_public;
								LoggedUser.setPicturePublicOrPrivate(
										feedObject.pic_hash,
										feedObject.is_public);
							}
						};
					};
					AlertDialog.Builder publicOrPrivateDialog = new AlertDialog.Builder(
							getActivity())
							.setTitle(
									getResources().getString(
											R.string.CONFIRM_TITLE_GENERIC))
							.setMessage(
									getResources().getString(
											R.string.ARE_YOU_SURE))
							.setPositiveButton(
									getResources().getString(R.string.YES),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											SetPictureSettings setPictureSettings = new SetPictureSettings(
													getActivity(),
													LoggedUser.id,
													feedObject.pic_hash,
													feedObject.is_public,
													false, LoggedUser.koeToken,
													handlerPublicOrPrivate,
													true);
											setPictureSettings.execute();
										}
									})
							.setNegativeButton(
									getResources().getString(R.string.NO),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											// continue with delete
										}
									});
					publicOrPrivateDialog.create().show();
					break;
				case DELETE_PICTURE_ITEM:
					final Handler handlerDelete = new Handler() {
						public void handleMessage(Message msg) {
							final String result = (String) msg.obj;
							if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
								// TODO
							}
						};
					};
					SetPictureSettings setPictureSettings1 = new SetPictureSettings(
							getActivity(), LoggedUser.id, feedObject.pic_hash,
							!feedObject.is_public, true, LoggedUser.koeToken,
							handlerDelete, false);
					setPictureSettings1.execute();
					break;
				case REPORT_PICTURE_ITEM:
					AlertDialog.Builder reportPictureDialog = new AlertDialog.Builder(
							getActivity())
							.setTitle(
									getResources().getString(
											R.string.CONFIRM_TITLE_GENERIC))
							.setMessage(
									getResources().getString(
											R.string.ARE_YOU_SURE))
							.setPositiveButton(
									getResources().getString(R.string.YES),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											ReportPicture reportPicture = new ReportPicture(
													getActivity(),
													LoggedUser.id,
													feedObject.pic_hash,
													LoggedUser.koeToken, true);
											reportPicture.execute();
										}
									})
							.setNegativeButton(
									getResources().getString(R.string.NO),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											// continue with delete
										}
									});
					reportPictureDialog.create().show();
					break;
				case SET_AS_PROFILE_PICTURE_ITEM:
					final Handler handlerUpdateUserPic = new Handler() {
						public void handleMessage(Message msg) {
							final String result = (String) msg.obj;
							if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
								LoggedUser.avatarURL = result;
								feedObject.user_data.picture_url = result;
								imageLoader.displayImage(
										feedObject.user_data.picture_url,
										imgUserAvatarFeed,
										BaseActivity.optionsAvatarImage, null);
							}
						};
					};
					AlertDialog.Builder profilePictureDialog = new AlertDialog.Builder(
							getActivity())
							.setTitle(
									getResources().getString(
											R.string.CONFIRM_TITLE_GENERIC))
							.setMessage(
									getResources().getString(
											R.string.ARE_YOU_SURE))
							.setPositiveButton(
									getResources().getString(R.string.YES),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											SetUserPic setUserPic = new SetUserPic(
													getActivity(),
													LoggedUser.id,
													LoggedUser.koeToken,
													feedObject.pic_hash,
													handlerUpdateUserPic, true);
											setUserPic.execute();
										}
									})
							.setNegativeButton(
									getResources().getString(R.string.NO),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											// continue with delete
										}
									});
					profilePictureDialog.create().show();
					break;
				case SEND_BY_EMAIL_ITEM:
					Email.send(getActivity(), LoggedUser.username,
							feedObject.pic_hash);
					break;
				case SEND_BY_WHATSAPP_ITEM:
					Whatsapp.share(getActivity(), feedObject.pic_hash);
					break;
				case SEND_BY_FACEBOOK_ITEM:
					if (!getActivity().getSharedPreferences(
							BaseActivity.SHARED_PREFERENCES,
							Context.MODE_PRIVATE).getBoolean(
							BaseActivity.FACEBOOK_PUBLISH_ACTIONS, false)) {
						// add_soc_network_from_mobile
						final Handler handler = new Handler() {
							public void handleMessage(Message msg) {
								final String result = (String) msg.obj;
								final int publish_actions = msg.arg1;
								if (!result
										.equalsIgnoreCase(HttpConnections.ERROR)) {
									if (publish_actions == BaseActivity.FACEBOOK_HAS_PUBLISH_ACTIONS) {
										SharedPreferences sharedPreferences = getActivity()
												.getSharedPreferences(
														BaseActivity.SHARED_PREFERENCES,
														Context.MODE_PRIVATE);
										SharedPreferences.Editor editor = sharedPreferences
												.edit();
										editor.putBoolean(
												BaseActivity.FACEBOOK_IS_LOGGED_IN,
												true);
										editor.putBoolean(
												BaseActivity.FACEBOOK_PUBLISH_ACTIONS,
												true);
										editor.commit();
										sharePicInFacebook();
									} else {
										SharedPreferences sharedPreferences = getActivity()
												.getSharedPreferences(
														BaseActivity.SHARED_PREFERENCES,
														Context.MODE_PRIVATE);
										SharedPreferences.Editor editor = sharedPreferences
												.edit();
										editor.putBoolean(
												BaseActivity.FACEBOOK_IS_LOGGED_IN,
												true);
										editor.commit();
									}
								}
							};
						};
						FacebookManager
								.askForReadWritePermissionsAndAddSocNetwork(
										getActivity(), mSimpleFacebook, handler);
					} else {
						sharePicInFacebook();
					}
					break;
				case SEND_BY_TWITTER_ITEM:
					if (!getActivity().getSharedPreferences(
							BaseActivity.SHARED_PREFERENCES,
							Context.MODE_PRIVATE).getBoolean(
							BaseActivity.TWITTER_IS_LOGGED_IN, false)) {
						loginToTwitter(REQUEST_CODE_TWITTER_LOGIN_MENU);
					} else {
						sharePicInTwitter();
					}
					break;
				case BLOCK_ITEM:
					if (userInfo.is_follower != HttpConnections.RELATION_BLOCKED) {
						final Handler handlerBlock = new Handler() {
							public void handleMessage(Message msg) {
								final String result = (String) msg.obj;
								if (!result
										.equalsIgnoreCase(HttpConnections.ERROR)) {
									// Cambiar estado tras block
									userInfo.is_follower = HttpConnections.RELATION_BLOCKED;
								}
							};
						};
						AlertDialog.Builder blockUserDialog = new AlertDialog.Builder(
								getActivity())
								.setTitle(
										getResources().getString(
												R.string.CONFIRM_TITLE_GENERIC))
								.setMessage(
										getResources().getString(
												R.string.YOU_BLOCK_USER))
								.setPositiveButton(
										getResources().getString(R.string.YES),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												BlockUser blockUser = new BlockUser(
														getActivity(),
														LoggedUser.id,
														feedObject.user_data.user_id,
														LoggedUser.koeToken,
														handlerBlock, true);
												blockUser.execute();
											}
										})
								.setNegativeButton(
										getResources().getString(R.string.NO),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												// continue with delete
											}
										});
						blockUserDialog.create().show();
					} else {
						final Handler handlerUnblock = new Handler() {
							public void handleMessage(Message msg) {
								final String result = (String) msg.obj;
								if (!result
										.equalsIgnoreCase(HttpConnections.ERROR)) {
									// Cambiar estado tras unblock
									userInfo.is_follower = HttpConnections.RELATION_IS_FRIEND;
								}
							};
						};
						UnblockUser unblockUser = new UnblockUser(
								getActivity(), LoggedUser.id,
								feedObject.user_data.user_id,
								LoggedUser.koeToken, handlerUnblock);
						unblockUser.execute();
					}
					break;
				case CANCEL_ITEM:
					break;
				case CONFIG_ITEM:
					Intent intent = new Intent(getActivity(),
							UserConfigActivity.class);
					getActivity().startActivityForResult(intent,
							BaseActivity.USER_CONFIG_REQUEST_CODE);
					break;
				case LOGOUT_ITEM:
					new LoginRegisterUtil(getActivity()).logout(getActivity(),
							getActivity(), true, false);
					break;
				}
				return super.onContextItemSelected(item);
			}
			return false;
		} else {

			return false;

		}
	}

	private void sharePicInTwitter() {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

					// TODO Toast.makeText(getActivity(),
					// "Your picture has been shared in Twitter",
					// Toast.LENGTH_LONG).show();

				}
			};
		};
		SharePictureInSoc sharePictureInSoc = new SharePictureInSoc(
				getActivity(), LoggedUser.id, LoggedUser.koeToken,
				feedObject.pic_hash, "tw", handler);
		sharePictureInSoc.execute();
	}

	private void sharePicInFacebook() {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

					// TODO Toast.makeText(getActivity(),
					// "Your picture has been shared in Twitter",
					// Toast.LENGTH_LONG).show();

				}
			};
		};
		SharePictureInSoc sharePictureInSoc = new SharePictureInSoc(
				getActivity(), LoggedUser.id, LoggedUser.koeToken,
				feedObject.pic_hash, "fb", handler);
		sharePictureInSoc.execute();
	}

	private void gestionarMultiplesPantallas() {

		if (tableLikesCommentsFeed != null && captionBox1 != null
				&& linearImageContainerFeed != null
				&& linearLayoutCommentFeed != null && listCommentsFeed != null) {

			int tableLikesLocation = getY(tableLikesCommentsFeed);
			int captionBoxLocation = getY(captionBox1);

			// int spaceForTabs = BaseActivity.screenHeight - tableLikesLocation
			// - tableLikesCommentsFeed.getHeight() + 20;
			int spaceForTabs = BaseActivity.screenHeight
					- BaseActivity.tabsHeight - feedHeader.getHeight()
					- imageBox.getHeight() - tableLikesCommentsFeed.getHeight()
					+ 20;

		
			int translation = 0;

			if (BaseActivity.DEBUG)
				MyLog.v(TAG, "Tabs Height: " + BaseActivity.tabsHeight
						+ ", Space For Tabs: " + spaceForTabs);

			int transTableLikesCommentsFeed = 0;

			if (captionBox1.getHeight() > spaceForTabs) {
				captionBox1.setVisibility(View.INVISIBLE);
				captionBox1.setPadding(0, 0, 0, 0);
				ViewHelper.setTranslationY(
						captionBox1,
						linearImageContainerFeed.getHeight()
								- captionBoxLocation
								- BitmapUtil.getPixels(35, getActivity()));

				int top = captionBox1.getHeight()
						- BitmapUtil.getPixels(20, getActivity());
				linearLayoutCommentFeed.setPadding(
						linearLayoutCommentFeed.getPaddingLeft(), top,
						linearLayoutCommentFeed.getPaddingRight(), 0);

				transTableLikesCommentsFeed = captionBox1.getHeight()
						- BitmapUtil.getPixels(20, getActivity());

			} else {
				int top = BitmapUtil.getPixels(40, getActivity());
				linearLayoutCommentFeed.setPadding(
						linearLayoutCommentFeed.getPaddingLeft(), top,
						linearLayoutCommentFeed.getPaddingRight(), 0);
			}

			tableLikesCommentsFeed.setPadding(0, 0, 0, 0);
			translation += (BaseActivity.tabsHeight - spaceForTabs)
					- BitmapUtil.getPixels(20, getActivity());
			MyLog.i(TAG,
					"tanslation: " + translation + " "
							+ ViewHelper.getTranslationY(tableViewCommentsFeed));
			MyLog.i(TAG, "tanslation: " + translation + " +=  ("
					+ BaseActivity.tabsHeight + " - " + spaceForTabs + ") - "
					+ BitmapUtil.getPixels(20, getActivity()));
			// ViewHelper.setTranslationY(tableLikesCommentsFeed, -translation);
			// sube un poco mas del actual: BaseActivity.tabsHeight
			// ViewHelper.setTranslationY(tableLikesCommentsFeed,
			// BitmapUtil.getPixels(20, getActivity()) );
			ViewHelper.setTranslationY(tableLikesCommentsFeed,
					-transTableLikesCommentsFeed);

			listCommentsFeed.setPadding(0, 0, 0, BaseActivity.tabsHeight);

			if (feedObject.feedMode == BaseActivity.MY_FEED
					&& feedUserInfo != null)
				feedUserInfo
						.setPadding(
								feedUserInfo.getPaddingLeft(),
								feedUserInfo.getPaddingTop(),
								feedUserInfo.getPaddingRight(),
								BaseActivity.tabsHeight
										+ (int) ((BaseActivity.screenHeight - BaseActivity.tabsHeight) * 0.2));

		}
	}

	private int getY(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location[1];
	}

	// Se encarga de cargar todo el contenido
	public void initAudioSnapFragment() {
		btnStamp.setVisibility(View.GONE);
		if (swipeDetector != null)
			swipeDetector.setClickedEnabled(false);
		if (noPhoto) {
			try {
				feedObject.user_data = feedObject.owner_data;
				progressBarAudioSnap.setVisibility(View.GONE);
				lblPrivateFeed.setVisibility(View.VISIBLE);
				tableLikesCommentsFeed.setVisibility(View.GONE);
				ScrollView scrollViewFeedComments = (ScrollView) view
						.findViewById(R.id.scrollViewFeedComments);
				scrollViewFeedComments.setVisibility(View.GONE);
				loadUserName();
				loadUserPicture();
				initButtons();
				initNotificationsAndSwipe();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			try {
				loadUserName();
				loadAudioSnap();
				loadUserPicture();
				loadLikesInfo();
				loadCommentsInfo();
				loadCaption();
				loadDate();
				initButtons();
				initCommentsAndMention();
				initNotificationsAndSwipe();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (feedObject.feedMode !=BaseActivity.ONE_PICTURE_FEED && 
				feedObject.feedMode !=BaseActivity.MAIN_FEED && position==0) {
			feedUserInfo.setVisibility(View.VISIBLE);
			queryUserInfo(feedObject.feedMode);
		} else
			feedUserInfo.setVisibility(View.GONE);

	}
	
	public void onContextMenuClosed(Menu menu) {
		
		contextMenuClick=false;
	}
	
	
	public boolean onBackPressed() {
		
		contextMenuClick=false;
		if (swipeDetector.getPosition() == BaseActivity.POSITION_TOP) {
			swipeDetector.onTopToBottomSwipe();
			return true;
		} else if (swipeDetector.getPosition() == BaseActivity.POSITION_BOTTOM) {
			swipeDetector.onBottomToTopSwipe();
			return true;
		}

		return false;
	}

	/****************************/

	// Load user picture
	private void loadUserPicture() {
		
		if (imgUserAvatarFeed != null)
			if (feedObject.user_data.picture_url != null)
			{
				
				
				imageLoader.displayImage(feedObject.user_data.picture_url,
						imgUserAvatarFeed, BaseActivity.optionsAvatarImage,
						null);
			}
		
	}

	// Load user info
	private void loadUserName() {
		
		if (lblUserInfoUserNameFeed != null)
			lblUserInfoUserNameFeed.setText(feedObject.user_data.user_name);
		if (userInfo != null)
			fillLayoutUserInfo();
		
	}

	// Load likes info
	private void loadLikesInfo() {

		if (!noPhoto) {
			if (likes < 0) {
				isLiked = feedObject.like_data.has_liked;
				likes = Integer.valueOf(feedObject.like_data.total_likes);
			}
			if (isLiked) {
				btnLikesFeed.setSelected(true);
			} else {
				btnLikesFeed.setSelected(false);
			}

			btnLikesFeed.setText(likes
					+ " "
					+ getActivity().getResources()
							.getString(R.string.LIKE_THIS));
		}

	}

	// Load comments info
	private void loadCommentsInfo() {
		
		TextView row = new TextView(tableViewCommentsFeed.getContext());
		row.setText(tableViewCommentsFeed.getContext().getString(
				R.string.LOADING_COMMENTS));
		row.setGravity(Gravity.CENTER);
		tableViewCommentsFeed.addView(row);

		commentsAdapter = new CommentsAdapter(tableViewCommentsFeed);

		if (!noPhoto) {
			if (comments < 0) {
				comments = Integer
						.valueOf(feedObject.comment_data.total_comments);
			}
			btnComentariosFeed
					.setText(comments
							+ " "
							+ getActivity().getResources().getString(
									R.string.COMMENTS));
			if (comments < 1) {
				tableViewCommentsFeed.removeAllViews();
				row.setText(tableViewCommentsFeed.getContext().getString(
						R.string.NO_COMMENTS));
				row.setGravity(Gravity.CENTER);
				tableViewCommentsFeed.addView(row);
			}
			if (commentsList != null) {
				// btnComentariosFeed.setText(comments + " " +
				// getActivity().getResources().getString(R.string.COMMENTS));
				// fillLayoutComments(null);
			}
		}
		
	}

	// Load caption
	private void loadCaption() {
		
		if (feedObject.user_data.user_name != null) {

			feedCaptionBox1.setTextViewHTML(CommentsFormatter
					.replaceMentions(CommentsFormatter
							.replaceHashTags(feedObject.caption.str)));
			feedCaptionBox1
					.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod
							.getInstance());

			// feedCaptionBox1.gatherLinksForText(feedObject.caption.str);
			// MovementMethod m = feedCaptionBox1.getMovementMethod();
			// if ((m == null) || !(m instanceof LinkMovementMethod))
			// if (feedCaptionBox1.getLinksClickable())
			// feedCaptionBox1.setMovementMethod(LinkMovementMethod.getInstance());
			
		}
		
	}

	// Load date
	private void loadDate() {

		if (!noPhoto) {
			if (feedObject.pic_date != null) {
				try {
					feedCaptionFooter1.setText(DateUtil.formatTimeAgo(
							feedObject.pic_date, getActivity()));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	
	private void loadAudioSnapImage()
	{
		
	}
	// Load AudioSnap picture
	private void loadAudioSnap() {
		
		// Load AudioSnap Picture
		if (!noPhoto) {
			if (new AudioSnapsFileCache().isMediaMounted()) {
				
				
				final File fileImage = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ BaseActivity.CACHE_AUDIOSNAPS_FILES
						+ feedObject.pic_hash);
				final File fileAudio = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ BaseActivity.CACHE_AUDIOSNAPS_FILES
						+ "audio"
						+ feedObject.pic_hash);
				if (fileImage.exists() && imageViewAudioSnap != null) {
					
					imageLoadingPrintconf(imageLoader);
					imageLoader.displayImage(
							Uri.fromFile(fileImage).toString(),
							imageViewAudioSnap,
							BaseActivity.optionsAudioSnapImage,
							new ImageLoadingListener() {

								@Override
								public void onLoadingCancelled(String arg0,
										View arg1) {
									// TODO Auto-generated method stub
									
								}

								@Override
								public void onLoadingComplete(String arg0,
										View arg1, Bitmap arg2) {
									
									if (progressBarAudioSnap != null)
										progressBarAudioSnap
												.setVisibility(View.GONE);
									imageViewAudioSnap.setVisibility(View.VISIBLE);
									btnStamp.setVisibility(View.VISIBLE);
									//swipeDetector.setClickedEnabled(true);
								}

								@Override
								public void onLoadingFailed(String arg0,
										View arg1, FailReason arg2) {
									// TODO Auto-generated method stub
									
								}

								@Override
								public void onLoadingStarted(String arg0,
										View arg1) {
							
								}
							});

					// imageLoader.displayImage(Uri.fromFile(fileImage).toString(),
					// imageViewAudioSnap);

					// El nativo provoca muchos OutOfMemory
					// imageViewAudioSnap.setImageURI(Uri.fromFile(fileImage));

				}

				// Create click listener
				btnStamp.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						MainActivity.stopAudioPlayer();
						if (fileImage.exists()) {

							// fade out stamp
							animaciones.fadeOutStamp(btnStamp);

							if (BaseActivity.DEBUG)
								MyLog.d(TAG, "---- Play AudioSnap ----");
							audioPlayer = new AudioPlayer(new Runnable() {
								@Override
								public void run() {
									// fade in stamp
									animaciones.fadeInStamp(btnStamp);
									btnStamp.setClickable(true);
								}
							});
							try {
								if (fileAudio.exists()) {
									audioPlayer.setFileSource(fileAudio);
								} else {
									try {
										audioPlayer
												.setFileSource(new AudioSnapsFileCache()
														.extraeAudio(
																fileImage,
																feedObject.pic_hash));
									} catch (Exception e) {
										e.printStackTrace();
									}

								}
								btnStamp.setClickable(false);
								audioPlayer.play();
								MainActivity.setAudioPlayer(audioPlayer);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				
				imageViewAudioSnap.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						if (fileImage.exists()) {
						launchFullScreenPictureActivity();
						}
					}
				});
				
			} else {
				// No SD...
			}

		}
		
	}

	// Init comments and mention list
	private void initCommentsAndMention() {
		if (!noPhoto) {
			initComments(view, stringComments, feedObject.pic_hash);
			initMentionedFriendsList();
		}
	}

	// Load user notifications and swipe detector
	private void initNotificationsAndSwipe() {
		try {
			String picHash = null, caption = null;
			if (!noPhoto) {
				picHash = feedObject.pic_hash;
				caption = feedObject.caption.str;
			}

			int feedMode = feedObject.feedMode;
			if (feedMode == BaseActivity.MY_FEED) {
				FrameLayout frameUserNotifications = (FrameLayout) view
						.findViewById(R.id.frameUserNotifications);
				frameUserNotifications.setVisibility(View.GONE);
				// initUserNotifications();

				// Swipe detectors para animar views
				swipeDetector = new SwipeDetector(animaciones, feedPhoto,
						feedHeader, feedUserInfo, null, frameUserNotifications,
						picHash, caption, getActivity(), this, captionBox1,
						tableLikesCommentsFeed, feedMode);
				feedPhoto.setOnTouchListener(swipeDetector);
				
			
				

				// Forzamos posición foto en primer fragment, MY_FEED
				// No consigo que se vea abajo, el view se pinta por defecto
				// aunque realmente esté abajo...
				// if (jsonAudioSnap.has(HttpConnections.FIRST_PICTURE)) {
				// Toast.makeText(getActivity(), "Es el primero",
				// Toast.LENGTH_LONG).show();
				// }

			} else {
				swipeDetector = new SwipeDetector(animaciones, feedPhoto,
						feedHeader, feedUserInfo, null, null, picHash, caption,
						getActivity(), this, captionBox1,
						tableLikesCommentsFeed, feedMode);
				feedPhoto.setOnTouchListener(swipeDetector);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Init buttons listeners
	private void initButtons() {
		try {

			initSocialNeworkButtons();

			// Ampliar el área clicable
			view.post(new Runnable() {
				public void run() {
					// Post in the parent's message queue to make sure the
					// parent
					// lays out its children before we call getHitRect()
					Rect delegateArea = new Rect();
					ImageView delegate = btnStamp;
					delegate.getHitRect(delegateArea);
					delegateArea.top -= 50;
					delegateArea.left -= 50;
					delegateArea.right += 50;
					TouchDelegate expandedArea = new TouchDelegate(
							delegateArea, delegate);

					// give the delegate to an ancestor of the view we're
					// delegating the area to
					if (View.class.isInstance(delegate.getParent())) {
						((View) delegate.getParent())
								.setTouchDelegate(expandedArea);
					}
				};
			});

			// Realiza swipe

			feedHeader.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Pulsamos avatar user feed");

					if (feedObject.feedMode == BaseActivity.MAIN_FEED || feedObject.feedMode == BaseActivity.ONE_PICTURE_FEED) {
						if (BaseActivity.DEBUG)
							MyLog.d(TAG, "Consultando user: "
									+ feedObject.user_data.user_id);
						Intent intent = new Intent(getActivity(),
								UserFeedActivity.class);
						intent.putExtra(HttpConnections.USER_TARGET_ID,
								feedObject.user_data.user_id);
						if (LoggedUser.id
								.equalsIgnoreCase(feedObject.user_data.user_id)) {
							intent.putExtra(HttpConnections.FEED_MODE,
									BaseActivity.MY_FEED);
						} else {
							intent.putExtra(HttpConnections.FEED_MODE,
									BaseActivity.FRIEND_FEED);
							intent.putExtra(BaseActivity.POSITION,
									BaseActivity.POSITION_BOTTOM);
						}
						getActivity().startActivity(intent);
					}
					// swipeDetector.onTopToBottomSwipe();
				}
			});

			// Like listener
			btnLikesFeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Pulsamos botón like feed");
					try {
						if (btnLikesFeed.isSelected()) {
							btnLikesFeed.setSelected(false);
							likes--;
							isLiked = false;
							btnLikesFeed.setText(likes
									+ " "
									+ getActivity().getResources().getString(
											R.string.LIKE_THIS));
							queryPhotoLike(HttpConnections.DISLIKE_PICTURE);
						} else {
							btnLikesFeed.setSelected(true);
							likes++;
							isLiked = true;
							btnLikesFeed.setText(likes
									+ " "
									+ getActivity().getResources().getString(
											R.string.LIKE_THIS));
							queryPhotoLike(HttpConnections.LIKE_PICTURE);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// Swipe para mostrar comentarios
			btnComentariosFeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					MyLog.d(TAG, "Pulsamos botón comments feed");
					// swipeDetector.disable();
					// swipeDetector.blockViewPagers();
					// queryComments();
					/*
					 * swipeDetector.onBottomToTopSwipe();
					 */
					// showCommentsPopup();
					Intent intent = new Intent(getActivity(),
							CommentsActivity.class);
					intent.putExtra("feedObject", feedObject);
					startActivityForResult(intent,
							BaseActivity.COMMENTS_REQUEST_CODE);
					/*
					 * feedUserInfo.setVisibility(View.INVISIBLE);
					 * feedComments.setVisibility(View.VISIBLE);
					 * feedComments.setY(10);
					 * feedPhoto.setVisibility(View.INVISIBLE);
					 * feedHeader.setVisibility(View.INVISIBLE);
					 */
				}
			});

			// Desactivado por el momento
			btnMasFeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Pulsamos mas feed");
					getActivity().openContextMenu(v);
				}
			});

			// Desactivado por el momento
			btnOptionsNowFeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Pulsamos mas feed");
					if (v != null && getActivity() != null)
						getActivity().openContextMenu(v);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initSocialNeworkButtons() {

		Activity activity = getActivity();

		if (activity != null && btnTwitterCommentFeed != null
				&& btnFacebookCommentFeed != null) {

			final SharedPreferences prefs = activity.getSharedPreferences(
					BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);

			// Twitter
			btnTwitterCommentFeed = (CheckBox) view
					.findViewById(R.id.btnTwitterCommentFeed);

			btnTwitterCommentFeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Pulsamos botón fb like");

					if (!prefs.getBoolean(BaseActivity.TWITTER_IS_LOGGED_IN,
							false)) {
						btnTwitterCommentFeed.setSelected(false);
						loginToTwitter(REQUEST_CODE_TWITTER_LOGIN_COMMENTS);
					} else if (btnTwitterCommentFeed.isSelected()) {
						btnTwitterCommentFeed.setSelected(false);
					} else {
						btnTwitterCommentFeed.setSelected(true);
					}

				}
			});

			if (!prefs.getBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, false)) {
				btnTwitterCommentFeed.setSelected(false);
			} else {
				btnTwitterCommentFeed.setSelected(true);
			}

			// Facebook
			btnFacebookCommentFeed = (CheckBox) view
					.findViewById(R.id.btnFacebookCommentFeed);

			if (!prefs.getBoolean(BaseActivity.FACEBOOK_PUBLISH_ACTIONS, false)) {
				btnFacebookCommentFeed.setSelected(false);

				btnFacebookCommentFeed
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {

								// add_soc_network_from_mobile
								final Handler handler = new Handler() {
									public void handleMessage(Message msg) {
										final String result = (String) msg.obj;
										final int publish_actions = msg.arg1;

										if (!result
												.equalsIgnoreCase(HttpConnections.ERROR)) {

											if (publish_actions == BaseActivity.FACEBOOK_HAS_PUBLISH_ACTIONS) {

												SharedPreferences sharedPreferences = getActivity()
														.getSharedPreferences(
																BaseActivity.SHARED_PREFERENCES,
																Context.MODE_PRIVATE);
												SharedPreferences.Editor editor = sharedPreferences
														.edit();
												editor.putBoolean(
														BaseActivity.FACEBOOK_IS_LOGGED_IN,
														true);
												editor.putBoolean(
														BaseActivity.FACEBOOK_PUBLISH_ACTIONS,
														true);
												editor.commit();

												btnFacebookCommentFeed
														.setSelected(true);

												btnFacebookCommentFeed
														.setOnClickListener(new OnClickListener() {
															@Override
															public void onClick(
																	View v) {

																if (BaseActivity.DEBUG)
																	MyLog.d(TAG,
																			"Pulsamos botón fb like");

																if (btnFacebookCommentFeed
																		.isSelected()) {
																	btnFacebookCommentFeed
																			.setSelected(false);
																} else {
																	btnFacebookCommentFeed
																			.setSelected(true);
																}
															}
														});

											} else {

												SharedPreferences sharedPreferences = getActivity()
														.getSharedPreferences(
																BaseActivity.SHARED_PREFERENCES,
																Context.MODE_PRIVATE);
												SharedPreferences.Editor editor = sharedPreferences
														.edit();
												editor.putBoolean(
														BaseActivity.FACEBOOK_IS_LOGGED_IN,
														true);
												editor.commit();

												btnFacebookCommentFeed
														.setSelected(false);

											}

										} else {

											btnFacebookCommentFeed
													.setSelected(false);

										}
									};
								};

								FacebookManager
										.askForReadWritePermissionsAndAddSocNetwork(
												getActivity(), mSimpleFacebook,
												handler);

							}
						}

						);

			} else {
				btnFacebookCommentFeed.setSelected(true);

				btnFacebookCommentFeed
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {

								if (BaseActivity.DEBUG)
									MyLog.d(TAG, "Pulsamos botón fb like");

								if (btnFacebookCommentFeed.isSelected()) {
									btnFacebookCommentFeed.setSelected(false);
								} else {
									btnFacebookCommentFeed.setSelected(true);
								}
							}
						});
			}

		}
	}

	/**********************************/

	// Photo like
	public void queryPhotoLike(int direction) {
		final Handler handlerPhotoLike = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				try {
					if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {
						try {
							// Algo a hacer tras like?
						} catch (Exception e) {
							e.printStackTrace();
							if (BaseActivity.DEBUG)
								MyLog.d(TAG, "Exception");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};

		try {
			LikePicture likePicture = new LikePicture(getActivity(),
					handlerPhotoLike, LoggedUser.id, LoggedUser.koeToken,
					feedObject.pic_hash, direction);
			likePicture.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Query user info
	public void queryUserInfo(int mode) {

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "---- Update user data and comments ----");
		try {
			// Si es mi ficha, ya tenemos los datos
			// if (mode == BaseActivity.MY_FEED) {
			//
			// userInfo = new SimpleProfile();
			// userInfo.user_name = LoggedUser.username;
			// userInfo.picture_url = LoggedUser.avatarURL;
			// userInfo.user_id = LoggedUser.id;
			// userInfo.num_of_pics = LoggedUser.numOfPics;
			// userInfo.num_of_friends = LoggedUser.numOfFriends;
			// userInfo.num_of_followers = LoggedUser.numOfFollowers;
			// userInfo.user_caption = LoggedUser.caption;
			// userInfo.privacy_mode = LoggedUser.privacyMode;
			//
			// fillLayoutUserInfo();
			// // viewPager.getAdapter().notifyDataSetChanged();
			// }
			// En el feed principal los solicitamos para cada audiosnap
			// else {
			final Handler handlerUserInfo = new Handler() {
				public void handleMessage(Message msg) {
					final String result = (String) msg.obj;
					try {
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							try {

								Log.v(TAG, result);

								Gson gson = new Gson();
								userInfo = gson.fromJson(result,
										SimpleProfile.class);
								fillLayoutUserInfo();

								// viewPager.getAdapter().notifyDataSetChanged();
							} catch (Exception e) {
								e.printStackTrace();
								if (BaseActivity.DEBUG)
									MyLog.d(TAG, "!!! Exception");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			};

			try {
				new GetUserSimpleProfile(getActivity(), handlerUserInfo,
						LoggedUser.id, feedObject.user_data.user_id,
						LoggedUser.koeToken).execute();
			} catch (Exception e) {
				e.printStackTrace();
				new GetUserSimpleProfile(getActivity(), handlerUserInfo,
						LoggedUser.id, feedObject.user_data.user_id,
						LoggedUser.koeToken).execute();
			}

			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void queryComments() {

		commentsAdapter.clear();

		if (!noPhoto) {

			try {
				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "---- Picture with comments ----");
				final Handler handlerComments = new Handler() {
					public void handleMessage(Message msg) {
						stringComments = (String) msg.obj;
						try {
							if (!stringComments
									.equalsIgnoreCase(HttpConnections.ERROR)) {
								try {
									fillLayoutComments(stringComments);
								} catch (Exception e) {
									e.printStackTrace();
									if (BaseActivity.DEBUG)
										MyLog.d(TAG, "Exception");
								}
							}

							// crear timer que vaya refrescando los comentarios
							RefreshCommentsTimerTask refreshCommentsTask = new RefreshCommentsTimerTask();
							refreshCommentsTimer = new Timer();
							refreshCommentsTimer.schedule(refreshCommentsTask,
									INTERVALO_PARA_REFRESCAR_COMENTARIOS,
									INTERVALO_PARA_REFRESCAR_COMENTARIOS);

						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				};

				new GetCommentsForPicture(getActivity(), handlerComments,
						LoggedUser.id, feedObject.pic_hash, "0", null, null,
						LoggedUser.koeToken).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void releaseComments() {
		// Kill timer that updates comments
		if (refreshCommentsTimer != null) {
			refreshCommentsTimer.cancel();
			refreshCommentsTimer = null;
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "timer canceled");
		}
	}

	private class RefreshCommentsTimerTask extends TimerTask {
		@Override
		public void run() {
			timerQueryComments();
			Log.v(TAG, "timer called");
		}
	}

	public void timerQueryComments() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!noPhoto) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "---- Picture with comments ----");
					refreshComments();
				}
			}
		});
	}

	private void refreshComments() {

		final Handler handlerComments = new Handler() {

			public void handleMessage(Message msg) {
				stringComments = (String) msg.obj;
				try {
					if (!stringComments.equalsIgnoreCase(HttpConnections.ERROR)) {
						try {

							// rellenar tabla de comentarios
							if (stringComments != null) {
								Gson gson = new Gson();
								Type commentsListType = new TypeToken<List<Comment>>() {
								}.getType();
								commentsList = gson.fromJson(stringComments,
										commentsListType);
							}

							// update listView aún no está inicializado adapter,
							// no hay comments
							if (commentsAdapter.size() == 0
									&& commentsList.size() != 0) {

								// actualizar el número de comentarios
								comments = commentsList.size();
								btnComentariosFeed.setText(comments
										+ " "
										+ getActivity().getResources()
												.getString(R.string.COMMENTS));

								try {
									// rellenar tabla de comentarios
									for (int i = 0; i < commentsList.size(); i++) {
										commentsAdapter.addRow(commentsList
												.get(i));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							// adapter inicializado, ya hay comments
							else if (commentsAdapter.size() > 0) {

								// actualizar el número de comentarios
								comments = commentsList.size();
								btnComentariosFeed.setText(comments
										+ " "
										+ getActivity().getResources()
												.getString(R.string.COMMENTS));

								// añadir comentarios nuevos
								if (commentsList.size() > commentsAdapter
										.size()) {
									for (int i = commentsAdapter.size(); i < commentsList
											.size(); i++)
										commentsAdapter.addRow(commentsList
												.get(i));
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
							if (BaseActivity.DEBUG)
								MyLog.d(TAG, "Exception");
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};

		new GetCommentsForPicture(getActivity(), handlerComments,
				LoggedUser.id, feedObject.pic_hash, "0", null, null,
				LoggedUser.koeToken).execute();
	}

	// Inicializa notificaciones usuario
	private void initUserNotifications() {

		ListView listViewNotifications = (ListView) view
				.findViewById(R.id.listViewNotifications);
		notificationListAdapter = new NotificationListAdapter(getActivity());
		listViewNotifications.setAdapter(notificationListAdapter);

		// List items listeners
		listViewNotifications.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				if (BaseActivity.DEBUG)
					MyLog.d(TAG,
							"Pulsado item listViewNotifications position: "
									+ position);

				Notification notification = LoggedUserNotifications
						.getNotifications().get(position);
				notificationClicked(notification.notification_id);
				notificationAction(notification.type, notification);
			}
		});

	}

	public void upadteNotifications() {
		if (notificationListAdapter != null) {
			notificationListAdapter.notifyDataSetChanged();
		}
	}

	// Notification cases
	private void notificationAction(int code, Notification notification) {
		try {

			Intent intent = null;

			switch (code) {
			case BaseActivity.kKPNotificationCode_IReceiveFriendRequest:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_MyFriendRequestAccepted:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_FriendRequestAutoAccepted:
				// Nada
				break;
			case BaseActivity.kKPNotificationCode_IHaveNewFollower:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_MyPictureIsLiked:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.receiver.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_MyPictureIsCommented:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.receiver.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_TOP);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_PictureICommentedIsCommented:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_TOP);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_FacebookFriendSignedIn:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_TwitterFriendSignedIn:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.FRIEND_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_BOTTOM);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_YouWhereTagged:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_YouWhereTaggedInComment:
				/*** OK ***/
				intent = new Intent(getActivity(), UserFeedActivity.class);
				intent.putExtra(HttpConnections.USER_TARGET_ID,
						notification.maker.user_id);
				intent.putExtra(HttpConnections.PIC_HASH, notification.pic_hash);
				intent.putExtra(HttpConnections.FEED_MODE,
						BaseActivity.ONE_PICTURE_FEED);
				intent.putExtra(BaseActivity.POSITION,
						BaseActivity.POSITION_TOP);
				getActivity().startActivity(intent);
				break;
			case BaseActivity.kKPNotificationCode_DBFirstFriendHarvestDone:
				break;
			case BaseActivity.kKPNotificationCode_DBDeletedPicture:
				break;
			}

			if (code >= 500 || intent == null) {
				if (notification.open_url) {
					intent = new Intent(getActivity(), WebviewActivity.class);
					intent.putExtra("url",
							NotificationListAdapter.getCustomUrl(notification));
					getActivity().startActivity(intent);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "Error en notification action");
		}
	}

	// Marca notification as clicked
	private void notificationClicked(String notificationId) {
		NotificationClicked notificationClicked = new NotificationClicked(
				getActivity(), null, LoggedUser.id, notificationId,
				String.valueOf(System.currentTimeMillis() / 1000L),
				LoggedUser.koeToken);
		notificationClicked.execute();
	}

	// Fill the layout with the user info
	private void fillLayoutUserInfo() {

		try {
			// Load avatar
			try {
				if (feedObject.user_data.picture_url != null) {
					imageLoader.displayImage(feedObject.user_data.picture_url,
							imgAvatar, BaseActivity.optionsAvatarImage,
							new ImageLoadingListener() {
								@Override
								public void onLoadingStarted(String imageUri,
										View view) {
								}

								@Override
								public void onLoadingFailed(String imageUri,
										View view, FailReason failReason) {
									progressBarUserInfoAvatarFeed
											.setVisibility(View.GONE);
								}

								@Override
								public void onLoadingComplete(String imageUri,
										View view, Bitmap loadedImage) {
									progressBarUserInfoAvatarFeed
											.setVisibility(View.GONE);
								}

								@Override
								public void onLoadingCancelled(String imageUri,
										View view) {
									progressBarUserInfoAvatarFeed
											.setVisibility(View.GONE);
								}
							});
				}
			} catch (Exception e) {
				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "Exception");
				e.printStackTrace();
			}

			// Check user grandma
			try {
				if (userInfo.privacy_mode
						.equalsIgnoreCase(HttpConnections.GRANDMA)) {
					ImageView imgLockUserInfoAvatar = (ImageView) view
							.findViewById(R.id.imgLockUserInfoAvatar);
					imgLockUserInfoAvatar.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// User relationship
			try {
				if (feedObject.feedMode == BaseActivity.MY_FEED) {
					linearButtons.setVisibility(View.VISIBLE);
					// lblUserInfoRelationFeed.setVisibility(View.INVISIBLE);
					btnFollowFeed.setVisibility(View.INVISIBLE);
					lblUserInfoRelationUserInfoFeed.setText(getResources()
							.getString(R.string.THIS_IS_YOU));
				} else if (feedObject.user_data.user_id
						.equalsIgnoreCase(LoggedUser.id)) {
					linearButtons.setVisibility(View.VISIBLE);
					// lblUserInfoRelationFeed.setVisibility(View.INVISIBLE);
					btnFollowFeed.setVisibility(View.INVISIBLE);
					lblUserInfoRelationUserInfoFeed.setText(getResources()
							.getString(R.string.THIS_IS_YOU));
				} else if (feedObject.user_data.user_id
						.equalsIgnoreCase(MainActivity.audionsnapsUserId)) {
					linearButtons.setVisibility(View.VISIBLE);
					// lblUserInfoRelationFeed.setVisibility(View.INVISIBLE);
					lblUserInfoRelationUserInfoFeed.setText(getResources()
							.getString(R.string.IS_AUDIOSNAPS_USER));
					btnFollowFeed.setVisibility(View.INVISIBLE);
					btnConfigMe.setVisibility(View.INVISIBLE);
					btnLogoutMe.setVisibility(View.INVISIBLE);
				} else {

					btnFollowFeed.setVisibility(View.VISIBLE);
					String relation = "";
					boolean isFollowing = false;

					switch (userInfo.is_follower) {
					case HttpConnections.RELATION_IS_FOLLOWING:
						relation = getResources().getString(R.string.FOLLOWER);
						isFollowing = true;
						btnFollowFeed.setText(getResources().getString(
								R.string.FOLLOW));
						break;
					}

					switch (userInfo.is_friend) {
					case HttpConnections.RELATION_IS_FRIEND:
						if (isFollowing) {
							relation = getResources()
									.getString(R.string.FRIEND)
									+ " & "
									+ getResources().getString(
											R.string.FOLLOWER);
							btnFollowFeed.setText(getResources().getString(
									R.string.UNFOLLOW));
							if (isUnfollowed) {
								btnFollowFeed.setText(getResources().getString(
										R.string.FOLLOW));
							}
						} else {
							relation = getResources()
									.getString(R.string.FRIEND);
							btnFollowFeed.setText(getResources().getString(
									R.string.UNFOLLOW));
							if (isUnfollowed) {
								btnFollowFeed.setText(getResources().getString(
										R.string.FOLLOW));
							}
						}
						break;
					case HttpConnections.RELATION_IS_PENDING:
						btnFollowFeed.setText(getResources().getString(
								R.string.PENDING));
						btnFollowFeed.setClickable(false);
						break;
					}

					Log.v(TAG, "entra" + userInfo.is_follower);
					lblUserInfoRelationFeed.setText(relation);
					lblUserInfoRelationUserInfoFeed.setText(relation);
				}
				progressBarButtonFollowFeed.setVisibility(View.GONE);
			} catch (Exception e) {
				lblUserInfoRelationFeed.setText("");
				e.printStackTrace();
			}

			// Fill layout data
			lblUserName.setText(userInfo.user_name);

			// Si es el usuario AudioSnaps no mostramos ni dejamos clicar en
			// Friends/Followers
			if (userInfo.user_id.equals(BaseActivity.AUDIOSNAPS_USER_ID)) {
				lblPicturesMe.setText("-");
				lblFriends.setText("-");
				lblFollowersMe.setText("-");
				linearUserFriends.setEnabled(false);
				linearUserFollowers.setEnabled(false);
			} else {
				lblPicturesMe.setText(userInfo.num_of_pics);
				lblFriends.setText(userInfo.num_of_friends);
				lblFollowersMe.setText(userInfo.num_of_followers);
				linearUserFriends.setEnabled(true);
				linearUserFollowers.setEnabled(true);
			}

			if (userInfo.user_caption != null) {
				if (userInfo.user_caption.equalsIgnoreCase("null")) {
					lblCaptionMe.setText("");
				} else {
					lblCaptionMe.setText(userInfo.user_caption);
				}
			}

			// Listeners
			btnFollowFeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Pulsamos botón follow/unfollow");

					// No le estamos siguiendo
					if (btnFollowFeed
							.getText()
							.toString()
							.equalsIgnoreCase(
									getResources().getString(R.string.FOLLOW))) {

						final Handler handlerComment = new Handler() {
							public void handleMessage(Message msg) {
								final boolean result = (Boolean) msg.obj;
								try {
									if (result) {
										try {
											btnFollowFeed
													.setText(getResources()
															.getString(
																	R.string.UNFOLLOW));
											isUnfollowed = false;
										} catch (Exception e) {
											e.printStackTrace();
											if (BaseActivity.DEBUG)
												MyLog.d(TAG, "Exception");
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							};
						};

						new SendFriendRequests(getActivity(), handlerComment,
								LoggedUser.id, feedObject.user_data.user_id,
								LoggedUser.koeToken).execute();

						// Le estamos siguiendo
					} else {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage(
								getResources().getString(
										R.string.CONFIRM_UNFOLLOW))
								.setTitle(
										getResources().getString(
												R.string.CONFIRM_TITLE_GENERIC))
								.setPositiveButton(
										getResources().getString(R.string.YES),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {

												dialog.cancel();

												final Handler handlerComment = new Handler() {
													public void handleMessage(
															Message msg) {
														final boolean result = (Boolean) msg.obj;
														try {
															if (result) {
																try {
																	if (feedObject.feedMode == BaseActivity.FRIEND_FEED) {
																		if (BaseActivity.DEBUG)
																			MyLog.d(TAG,
																					"---- Unfollowed confirmed ----");
																		getActivity()
																				.finish();
																	} else {
																		btnFollowFeed
																				.setText(getResources()
																						.getString(
																								R.string.FOLLOW));
																		isUnfollowed = true;
																	}
																} catch (Exception e) {
																	e.printStackTrace();
																	if (BaseActivity.DEBUG)
																		MyLog.d(TAG,
																				"Exception");
																}
															}
														} catch (Exception e) {
															e.printStackTrace();
														}
													};
												};

												new UnfollowUser(
														getActivity(),
														handlerComment,
														LoggedUser.id,
														feedObject.user_data.user_id,
														LoggedUser.koeToken)
														.execute();

											}
										})
								.setNegativeButton(
										getResources().getString(R.string.NO),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						builder.show();
					}

				}
			});

			linearUserPictures.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {

						if (feedObject.feedMode == BaseActivity.FRIEND_FEED
								|| feedObject.feedMode == BaseActivity.MY_FEED) {
							swipeDetector.onBottomToTopSwipe();
						} else {
							try {
								Intent intent = new Intent(getActivity(),
										UserFeedActivity.class);
								intent.putExtra(HttpConnections.USER_TARGET_ID,
										feedObject.user_data.user_id);
								if (LoggedUser.id
										.equalsIgnoreCase(feedObject.user_data.user_id)) {
									intent.putExtra(HttpConnections.FEED_MODE,
											BaseActivity.MY_FEED);
								} else {
									intent.putExtra(HttpConnections.FEED_MODE,
											BaseActivity.FRIEND_FEED);
								}
								getActivity().startActivity(intent);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			linearUserFriends.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(getActivity(),
								UserFriendsFollowersActivity.class);
						intent.putExtra(HttpConnections.USER_TARGET_ID,
								feedObject.user_data.user_id);
						intent.putExtra(HttpConnections.FRIENDS, true);
						getActivity().startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();

					}
				}
			});

			linearUserFollowers.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(getActivity(),
								UserFriendsFollowersActivity.class);
						intent.putExtra(HttpConnections.USER_TARGET_ID,
								feedObject.user_data.user_id);
						intent.putExtra(HttpConnections.FRIENDS, false);
						getActivity().startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();

					}
				}
			});
			/* listener notification */
			btnNotifications.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MyLog.i("btnNotifications", "UserNotificationActivity");
					Intent intent = new Intent(getActivity(),
							UserNotificationActivity.class);
					getActivity().startActivityForResult(intent,
							BaseActivity.USER_CONFIG_REQUEST_CODE);
				}
			});

			// Listeners Config y Logout
			btnConfigMe.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(),
							UserConfigActivity.class);
					getActivity().startActivityForResult(intent,
							BaseActivity.USER_CONFIG_REQUEST_CODE);
				}
			});

			btnLogoutMe.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new LoginRegisterUtil(getActivity()).logout(getActivity(),
							getActivity(), true, false);
				}
			});

			actualizarBtnContext();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Inicializa layout comentarios
	private void initComments(View view, final String string,
			final String picHash) {

		// Views feedComments
		final Button btnCommentFeed = (Button) view
				.findViewById(R.id.btnCommentFeed);
		btnCommentFeed.setEnabled(false);
		btnCommentFeed.getBackground().setAlpha(127);

		final TextView lblContadorCaracteresCommentsFeed = (TextView) view
				.findViewById(R.id.lblContadorCaracteresCommentsFeed);

		// EditText listener
		txtCommentFeed.setFocusable(true);

		txtCommentFeed.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {

				if (txtCommentFeed.getText().length() > 0) {
					if (!btnCommentFeed.isEnabled()) {
						btnCommentFeed.setEnabled(true);
						btnCommentFeed.getBackground().setAlpha(255);
					}
				} else {
					if (btnCommentFeed.isEnabled()) {
						btnCommentFeed.setEnabled(false);
						btnCommentFeed.getBackground().setAlpha(127);
					}
				}

				int cuentaCaracteresCaption = BaseActivity.MAX_SIZE_COMMENT
						- txtCommentFeed.getText().length();

				if (cuentaCaracteresCaption >= 0) {
					lblContadorCaracteresCommentsFeed.setText(String
							.valueOf(cuentaCaracteresCaption));
				} else {
					lblContadorCaracteresCommentsFeed.setText(String.valueOf(0));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

		// Button listener
		btnCommentFeed.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "Pulsamos botón comment");

				// Cerrar teclado
				InputMethodManager inputManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				/* comentar esta linea porque error null */
				// inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
				// InputMethodManager.HIDE_NOT_ALWAYS);

				final Handler handlerComment = new Handler() {
					public void handleMessage(Message msg) {
						final boolean result = (Boolean) msg.obj;
						try {
							if (result) {

								txtCommentFeed.setText("");
								refreshComments();

							} else {
								// Error comments text
								tableViewCommentsFeed.removeAllViews();
								TextView row = new TextView(
										tableViewCommentsFeed.getContext());
								row.setText(tableViewCommentsFeed
										.getContext()
										.getString(
												R.string.ERROR_LOADING_COMMENTS));
								row.setGravity(Gravity.CENTER);
								tableViewCommentsFeed.addView(row);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				};
				if (txtCommentFeed.length() > 0)
					new CommentPicture(getActivity(), handlerComment,
							LoggedUser.id, picHash, txtCommentFeed
									.getFormattedComment(),
							btnFacebookCommentFeed.isSelected(),
							btnTwitterCommentFeed.isSelected(),
							LoggedUser.koeToken).execute();
			}
		});

	}

	// Fill comments
	private void fillLayoutComments(String string) {
		try {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- Loading comments into Fragment ----");

			// rellenar tabla de comentarios
			if (string != null) {
				Gson gson = new Gson();
				Type commentsListType = new TypeToken<List<Comment>>() {
				}.getType();
				commentsList = gson.fromJson(string, commentsListType);
			}
			// commentsAdapter = new CommentsAdapter(tableViewCommentsFeed);
			for (int i = 0; i < commentsList.size(); i++)
				commentsAdapter.addRow(commentsList.get(i));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Inicializa mentions
	private void initMentionedFriendsList() {
		
		
		// Check if list of friends is saved
		final SharedPreferences prefs = getActivity().getSharedPreferences(
				BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		if (prefs.contains(BaseActivity.MY_FRIENDS_JSONARRAY)) {
			try {
				jsonArrayFriends = new JSONArray(prefs.getString(
						BaseActivity.MY_FRIENDS_JSONARRAY, ""));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mentionedFriendsListAdapter = new MentionedFriendsListAdapter(
					view.getContext(), jsonArrayFriends);
			txtCommentFeed.setAdapter(mentionedFriendsListAdapter);
		} else {
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "---- Request friends list for mentions ----");
			final Handler handler = new Handler() {

				public void handleMessage(Message msg) {
					final String result = (String) msg.obj;
					try {
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							// Save list of friends
							Editor editor = prefs.edit();
							editor.putString(BaseActivity.MY_FRIENDS_JSONARRAY,
									result);
							editor.commit();

							jsonArrayFriends = new JSONArray(result);
							
							mentionedFriendsListAdapter = new MentionedFriendsListAdapter(
									view.getContext(), jsonArrayFriends);
							txtCommentFeed
									.setAdapter(mentionedFriendsListAdapter);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				};
			};

			new GetCompleteFriendsList(view.getContext(), handler,
					LoggedUser.id, LoggedUser.id, true, false, false,
					LoggedUser.koeToken, false).execute();
		}
		
	}

	// Return if fragment has photos
	public boolean noPhotosFragment() {
		return noPhoto;
	}

	@Override
	public void update(){
		
	}
	
	
	public void updateX() {
		View v=getView();	
		if(parentView!=null && parentView!=v)
		{		
		
							
			//int h=v.getMeasuredHeight();

			((ViewGroup)getView().getParent()).removeView(v);
			parentView.addView(v); 
/*
			 ViewTreeObserver vto = getView().getViewTreeObserver();
			   vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			       public boolean onPreDraw() {
			           int finalHeight = getView().getMeasuredHeight();
			           int finalWidth = getView().getMeasuredWidth();
		               MyLog.i(TAG, "!!! addOnPreDrawListener parent "+parentView+"  height "+finalHeight+" "+finalWidth);
		               parentView.setLayoutParams(new GridView.LayoutParams(finalWidth,finalHeight));
		               getView().getViewTreeObserver().removeOnPreDrawListener(this);
			           return true;
			       }
			   });
			*/
			
		}
		// do whatever you want to update your data
		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "---- Updating AudioSnapFragment Info ----");

		if (feedObject.user_data.user_id.equals(LoggedUser.id)) {
			feedObject.user_data.user_name = LoggedUser.username;
			loadUserName();
			feedObject.user_data.picture_url = LoggedUser.avatarURL;
			loadUserPicture();
		}

		// MyLog.i(TAG,
		// "!!! n comments: "+feedObject.comment_data.total_comments);
		// this.loadAudioSnap();
		initSocialNeworkButtons();

	}

	@Override
	public void load() {
		// do whatever you want to update your data
		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "---- Updating AudioSnap displayed ----");
		// loadAudioSnap();
	}

	@Override
	public void onDestroy() {
		try {

			// Listeners
			btnStamp.setOnClickListener(null);
			btnLikesFeed.setOnClickListener(null);
			btnMasFeed.setOnClickListener(null);
			btnFollowFeed.setOnClickListener(null);
			btnConfigMe.setOnClickListener(null);
			btnLogoutMe.setOnClickListener(null);
			feedPhoto.setOnTouchListener(null);
			btnTwitterCommentFeed.setOnClickListener(null);
			btnFacebookCommentFeed.setOnClickListener(null);
			txtCommentFeed.setOnItemClickListener(null);

			// Adapters
			txtCommentFeed.setAdapter(null);
			mentionedFriendsListAdapter = null;
			notificationListAdapter = null;

			// View fragment
			view = null;

			// Views contenedores
			feedUserInfo = null;
			feedHeader = null;
			feedPhoto = null;
			feedComments = null;

			// Views
			imageViewAudioSnap = null;
			picCorners = null;
			// btnStamp = null;
			feedCaptionBox1 = null;
			feedCaptionFooter1 = null;
			captionBox1 = null;
			tableViewCommentsFeed = null;
			btnMasFeed = null;
			imgUserAvatarFeed = null;
			btnLikesFeed = null;
			btnComentariosFeed = null;
			lblPrivateFeed = null;
			lblUserInfoUserNameFeed = null;
			progressBarAudioSnap = null;
			progressBarUserInfoAvatarFeed = null;
			progressBarButtonFollowFeed = null;
			lblUserName = null;
			lblPicturesMe = null;
			lblFriends = null;
			lblFollowersMe = null;
			lblCaptionMe = null;
			imgAvatar = null;
			btnFollowFeed = null;
			linearButtons = null;
			btnConfigMe = null;
			btnLogoutMe = null;
			lblUserInfoRelationFeed = null;
			lblUserInfoRelationUserInfoFeed = null;
			linearUserFriends = null;
			linearUserFollowers = null;
			linearUserPictures = null;
			tableLikesCommentsFeed = null;
			txtCommentFeed = null;
			linearImageContainerFeed = null;
			linearLayoutCommentFeed = null;
			listCommentsFeed = null;

			if (refreshCommentsTimer != null) {
				refreshCommentsTimer.cancel();
				refreshCommentsTimer = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	@Override
	public void finalize() {
		recycleBitmapFromImageView(imageViewAudioSnap);
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void recycleBitmapFromImageView(ImageView imageViewAudioSnap) {
		if (imageViewAudioSnap != null) {
			Drawable drawable = imageViewAudioSnap.getDrawable();
			if (drawable instanceof BitmapDrawable) {
				((BitmapDrawable) drawable).getBitmap().recycle();
			}
		}
	}

	public String getPicHash() {
		return feedObject.pic_hash;
	}

	@Override
	public void onResume() {
		super.onResume();
		update();
		mSimpleFacebook = SimpleFacebook.getInstance(getActivity());

		if (swipeDetector.getPosition() == BaseActivity.POSITION_TOP) {

			if (refreshCommentsTimer == null) {
				RefreshCommentsTimerTask refreshCommentsTask = new RefreshCommentsTimerTask();
				refreshCommentsTimer = new Timer();
				refreshCommentsTimer.schedule(refreshCommentsTask,
						INTERVALO_PARA_REFRESCAR_COMENTARIOS,
						INTERVALO_PARA_REFRESCAR_COMENTARIOS);
			}

		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (refreshCommentsTimer != null) {
			refreshCommentsTimer.cancel();
			refreshCommentsTimer = null;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable(BaseActivity.AUDIO_SNAP_OBJECT,
				feedObject);
		super.onSaveInstanceState(savedInstanceState);
	}

	// Twitter and Facebook Login
	@SuppressLint("HandlerLeak")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_CODE_TWITTER_LOGIN_COMMENTS) {

			if (resultCode == Activity.RESULT_OK) {

				String token = data
						.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN);
				String tokenSecret = data
						.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN_SECRET);

				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						final String result = (String) msg.obj;
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							SharedPreferences sharedPreferences = getActivity()
									.getSharedPreferences(
											BaseActivity.SHARED_PREFERENCES,
											Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPreferences
									.edit();
							editor.putBoolean(
									BaseActivity.TWITTER_IS_LOGGED_IN, true);
							editor.commit();

							btnTwitterCommentFeed.setSelected(true);

						}
					};
				};

				AddSocNetworkFromMobile addSocNetworkFromMobile = new AddSocNetworkFromMobile(
						getActivity(), handler, LoggedUser.id, "twitter", null,
						token, tokenSecret, LoggedUser.koeToken);
				addSocNetworkFromMobile.execute();

			}
		} else if (requestCode == REQUEST_CODE_TWITTER_LOGIN_MENU) {

			if (resultCode == Activity.RESULT_OK) {

				String token = data
						.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN);
				String tokenSecret = data
						.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN_SECRET);

				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						final String result = (String) msg.obj;
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							SharedPreferences sharedPreferences = getActivity()
									.getSharedPreferences(
											BaseActivity.SHARED_PREFERENCES,
											Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPreferences
									.edit();
							editor.putBoolean(
									BaseActivity.TWITTER_IS_LOGGED_IN, true);
							editor.commit();

							sharePicInTwitter();

						}
					};
				};

				AddSocNetworkFromMobile addSocNetworkFromMobile = new AddSocNetworkFromMobile(
						getActivity(), handler, LoggedUser.id, "twitter", null,
						token, tokenSecret, LoggedUser.koeToken);
				addSocNetworkFromMobile.execute();

			}

		} else if (requestCode == BaseActivity.COMMENTS_REQUEST_CODE) {
			
			FeedObject fo = data.getExtras().getParcelable("feedObject");
			feedObject.comment_data.total_comments = fo.comment_data.total_comments;
			comments = feedObject.comment_data.total_comments;
			// btnComentariosFeed.setText(comments + " " +
			// getActivity().getResources().getString(R.string.COMMENTS));
			loadCommentsInfo();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}

	}

	// Twitter Login
	private void loginToTwitter(int requestCode) {
		// Launch twitter activity
		Intent intent = new Intent(getActivity(), TwitterLoginActivity.class);
		startActivityForResult(intent, requestCode);
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		contextMenuClick=false;
		
		if (audioPlayer != null) {
			audioPlayer.stop();
		}
		super.onStop();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		
		super.setUserVisibleHint(isVisibleToUser);
		// Make sure that we are currently visible
		if (this.isVisible()) {
			// If we are becoming invisible, then...
			if (!isVisibleToUser) {
				if (audioPlayer != null) {
					audioPlayer.stop();
				}
				Log.d("MyFragment", "Not visible anymore.  Stopping audio.");
				// TODO stop audio playback
			}
		}
	}

	public void invalidateSwipe() {
		if (swipeDetector.getPosition() == BaseActivity.POSITION_BOTTOM)
			swipeDetector.onBottomToTopSwipe(true);
		else if (swipeDetector.getPosition() == BaseActivity.POSITION_TOP)
			swipeDetector.onTopToBottomSwipe(true);
	}

	public FeedObject getFeedObject() {
		return feedObject;
	}

	public void setFeedObject(FeedObject feedObject) {
		this.feedObject = feedObject;
	}

	public void isVisibleHeader(boolean b) {
		// TODO Auto-generated method stub
		visibleHeader = b;
		/*
		 * if(b) this.feedHeader.setVisibility(View.VISIBLE); else
		 * this.feedHeader.setVisibility(View.GONE);
		 */
	}

	public void isDetailedUserInfo(boolean b) {
		// TODO Auto-generated method stub
		visibleUserInfo = b;
	}

	public void refreshAudioSnapPicture() {
		// TODO Auto-generated method stub
		
		this.loadAudioSnap();
	}

	/*
	 * final Handler handlerChangeComments = new Handler() { public void
	 * handleMessage(Message msg) { try{ Integer nComments= (Integer) msg.obj;
	 * feedObject.comment_data.total_comments=nComments; } catch(Exception e) {
	 * e.printStackTrace(); } } };
	 */

	private void launchFullScreenPictureActivity() {
		
		String picHash = feedObject.pic_hash;
		String caption = feedObject.caption.str;
		if (picHash != null && caption != null) {
			Intent intent = new Intent(getActivity(),
					FullScreenPictureActivity.class);
			intent.putExtra("picHash", picHash);
			intent.putExtra("captionApp", Html.fromHtml(caption).toString());
			if (audioPlayer != null)
				audioPlayer.stop();
			getActivity().startActivity(intent);

		}

	}

	public void setParentView(LinearLayout rowView) {
		this.parentView=rowView;
	}

	ReuseFragmentTask reuseFragmentTask = new ReuseFragmentTask();;
	public void reuseFragment(int position,FeedObject feedObject2) {
		// TODO Auto-generated method stub
   	 if(feedObject.pic_hash.equals(feedObject2.pic_hash))
   	 {
   		
		 return;
   	 }
		
		
	if(reuseFragmentTask!=null)
		reuseFragmentTask.cancel(true);
		reuseFragmentTask = new ReuseFragmentTask();
		reuseFragmentTask.init(position,feedObject2);
		reuseFragmentTask.execute();
		
	}

	public void setPosition(int position) {
		// TODO Auto-generated method stub
		this.position=position;
	}
	
	public int getPosition()
	{
		return this.position;
	}

	//private final ReuseFragmentTask reuseFragmentTask=new ReuseFragmentTask();
	
	private class ReuseFragmentTask extends AsyncTask<Void, Void, Void> {
		
		final static private String TAG="ReuseFragmentTask"; 
		boolean showUserInfo=false;
		
	    public void init(int pos, FeedObject fo) {
			// TODO Auto-generated constructor stub
	    	position=pos;
			feedObject=fo;
			comments = feedObject.comment_data.total_comments;
			likes=-1;
		}

		@Override
	    protected Void doInBackground(Void... params) {
			
			return null;
	    }
	 
	    @Override
	    protected void onProgressUpdate(Void... values) {
	    
	    }
	 
	    @Override
	    protected void onPreExecute() {
	    	
	    	progressBarAudioSnap.setVisibility(View.VISIBLE);
			imageViewAudioSnap.setImageResource(android.R.color.transparent);
			btnStamp.setVisibility(View.GONE);
			loadUserName();
			if (feedObject.feedMode !=BaseActivity.ONE_PICTURE_FEED && 
					feedObject.feedMode !=BaseActivity.MAIN_FEED && position==0) {
				feedUserInfo.setVisibility(View.VISIBLE);
				showUserInfo=true;
				
			} else
			{
				feedUserInfo.setVisibility(View.GONE);
				showUserInfo=false;
			}
			
			if (feedObject.no_photos) {
				noPhoto = true;
			}
			
	    }
	 
	    @Override
	    protected void onPostExecute(Void result) {
	    	
	    	
	    	
	    	initAudioSnapFragment();		
			
			controlBotonRefresco();
			
			if(showUserInfo)
			{
				if(userInfo==null)
					queryUserInfo(feedObject.feedMode);						
			}									
			
	    }
	 
	    @Override
	    protected void onCancelled() {
	    	
	    	//initAudioSnapFragment();
	    }
	}
	
	public void imageLoadingPrintconf(ImageLoader imgL)
	{
		MyLog.d(TAG, "ImageLoader config:\nsize:"+imgL.getMemoryCache().keys().size()+"\nkeys:" +imgL.getMemoryCache().keys().toString());
	}
}
