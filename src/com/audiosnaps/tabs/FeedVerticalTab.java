package com.audiosnaps.tabs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Toast;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;
import com.audiosnaps.adapters.NewFeedVerticalAdapter;
import com.audiosnaps.classes.AudioSnapsFileCache;
import com.audiosnaps.classes.Dialogos;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.fragments.AudioSnapFragment;
import com.audiosnaps.http.Feed;
import com.audiosnaps.http.GetPicture;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.NotificationSeen;
import com.audiosnaps.http.UserFeed;
import com.audiosnaps.json.model.FeedObject;
import com.audiosnaps.log.MyLog;
import com.google.gson.Gson;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

@SuppressLint("HandlerLeak")
public class FeedVerticalTab{

	private final static String TAG = "FeedVerticalTab";

	private static final int VIEW_PAGER_WIDTH = BaseActivity.screenWidth;
	private static final int VIEW_PAGER_HEIGHT = (int) (VIEW_PAGER_WIDTH * 1.684375);

	private Context context;

	private final static int MAX_VIEWPAGER_FRAGMENTS = 100;

	// ImageLoader y Cache
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private AudioSnapsFileCache audioSnapsFileCache;

	// Feed vars
	private int feedMode;
	private int feedPosition = 0;
	private int pageSelected = 0;
	private int pagesToUpdate = 7;
	private JSONArray feedJsonArray, oldFeedJsonArray;
	private boolean primerFeed = true;
	private boolean primerFeedLoad = true;
	private boolean primerUserLoad = true;
	private String fromTimeStamp = null;
	private String feedTargetId;
	private String picHash;
	private int FIRST_LOAD_FEED_COUNT = 3;
	private int POST_DELAYED_TIME = 200;
	private ProgressDialog progressDialog;
	private boolean noPhotos = false, refreshingFeed = false;

	private Gson gson;

	// Fragments y viewPager
	private ListView viewPager;
	private NewFeedVerticalAdapter newFeedVerticalAdapter;
	private FragmentManager fragmentManager;
	private List<FeedObject> feedObjectList;

	private int feedResource = 0;

	// Constructor
	public FeedVerticalTab(Context context, FragmentManager fragmentManager,
			ListView viewPager) {
		this.context = context;
		this.fragmentManager = fragmentManager;
		this.viewPager = viewPager;
		this.gson = new Gson();
		progressDialog = new Dialogos(context).loadingProgressDialog();

	}

	// Init Feed Fragment
	public void initFeed(final int mode, final String targetId,
			final String timeStamp, final String pHash) {
		
		// Animaciones animaciones = new Animaciones();
		// animaciones.scaleViewPx(viewPager, VIEW_PAGER_WIDTH,
		// VIEW_PAGER_HEIGHT);
		audioSnapsFileCache = new AudioSnapsFileCache();
		feedObjectList = new ArrayList<FeedObject>();
		feedTargetId = targetId;
		picHash = pHash;

		// Check timestamp & feedmode
		feedMode = mode;
		if (timeStamp != null) {
			fromTimeStamp = timeStamp;
		} else {
			fromTimeStamp = BaseActivity.FIRST_FROM_TIMESTAMP;
		}
		
		getPhotoForFeed(feedMode, targetId);

		boolean pauseOnScroll = true; // or true
		boolean pauseOnFling = true; // or false
		PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader,pauseOnScroll, pauseOnFling);
		viewPager.setOnScrollListener(listener);
		
	}

	// Request photos for feed
	private void getPhotoForFeed(int mode, String targetId) {
		// Feed mode
		
		switch (mode) {
		case BaseActivity.MAIN_FEED:
			feedMode = BaseActivity.MAIN_FEED;
			getFeed(null, false);
			break;
		case BaseActivity.FRIEND_FEED:
			feedMode = BaseActivity.FRIEND_FEED;
			getFeed(targetId, false);
			break;
		case BaseActivity.MY_FEED:
			feedMode = BaseActivity.MY_FEED;
			getFeed(LoggedUser.id, false);
			break;
		case BaseActivity.ONE_PICTURE_FEED:
			feedMode = BaseActivity.ONE_PICTURE_FEED;
			getOnePictureFeed();
			break;
		}
	}

	// Mark notifications as seen
	private void notificationSeen() {
		try {
			if (MainActivity.userClass.getNotificationsJsonArray().length() > 0) {
				JSONArray jsonArrayNotifications = new JSONArray(
						MainActivity.userClass.getNotificationsJsonArray());
				String[] notificationIds = new String[jsonArrayNotifications
						.length()];
				int length = jsonArrayNotifications.length();
				for (int i = 0; i < length; i++) {
					notificationIds[i] = jsonArrayNotifications
							.getJSONObject(i).getString(
									HttpConnections.NOTIFICATION_ID);
				}
				new NotificationSeen(context, LoggedUser.id, new JSONArray(
						Arrays.asList(notificationIds)), String.valueOf(System
						.currentTimeMillis() / 1000L), LoggedUser.koeToken)
						.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Refresh from interface
	public void RefreshFeed() {
		getFeed(LoggedUser.id, true);
	}

	// Obtenemos jsonArray feed
	public void getFeed(String targetId, boolean refreshFeed) {
		if (!refreshFeed) {
			// Obtengo el fromStamp de la última foto del jsonArray
			if (!primerFeed) {
				try {
					JSONObject jsonObject = feedJsonArray
							.getJSONObject(feedJsonArray.length() - 1);
					fromTimeStamp = jsonObject
							.getString(HttpConnections.TIMESTAMP);
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Nuevo fromTimeStamp: " + fromTimeStamp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			final Handler handler = new Handler() {

				public void handleMessage(Message msg) {
					
					estaIncrementandoFeeds = false;
					final String result = (String) msg.obj;
					try {
						// No hay result, no hay fotos
						if (primerFeedLoad
								&& new JSONObject(result)
										.has(HttpConnections.NO_PHOTOS)) {
							if (BaseActivity.DEBUG)
								MyLog.d(TAG, "---- User without photos ----");
							noPhotos = true;
							// Implementar aquí user sin fotos
							feedJsonArray = new JSONArray();
							feedJsonArray.put(new JSONObject(result));
							if (BaseActivity.DEBUG)
								MyLog.d(TAG, "---- Result feedJsonArray: "
										+ feedJsonArray.toString());
							addFragment(feedPosition);
							estaIncrementandoFeeds = false;
							feedPosition++;
						} else {
							try {
								// Load primeros AudioSnaps al iniciar feed
								if (primerFeedLoad) {
									if (BaseActivity.DEBUG)
										MyLog.d(TAG,
												"---- Adding 3 first fragments ----");
									primerFeedLoad = false;
									feedJsonArray = new JSONArray(result);
									if (!(feedJsonArray.length() >= FIRST_LOAD_FEED_COUNT)) {
										FIRST_LOAD_FEED_COUNT = feedJsonArray
												.length();
									}
									if (feedJsonArray.length() < 1) {
										Toast.makeText(context,
												"Usuario sin fotos",
												Toast.LENGTH_SHORT).show();

									} else {
										for (int i = 0; i < FIRST_LOAD_FEED_COUNT; i++) {
											addFragment(feedPosition);
											estaIncrementandoFeeds = false;
											feedPosition++;
										}
									}
								}
								// Nuevos elementos para añadir al feed
								else {
									if (BaseActivity.DEBUG)
										MyLog.d(TAG,
												"---- Update FeedJSONArray with more data ----");
									JSONArray jsonArrayAux = new JSONArray(
											result);
									for (int i = 0; i < jsonArrayAux.length(); i++) {
										feedJsonArray.put(jsonArrayAux
												.getJSONObject(i));
										
									}
									estaIncrementandoFeeds = false;
									incrementarFeeds();

								}
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						try {
							// Load primeros AudioSnaps al iniciar feed
							if (primerFeedLoad) {
								if (BaseActivity.DEBUG)
									MyLog.d(TAG,
											"---- Adding 3 first fragments ----");
								primerFeedLoad = false;
								feedJsonArray = new JSONArray(result);
								if (!(feedJsonArray.length() >= FIRST_LOAD_FEED_COUNT)) {
									FIRST_LOAD_FEED_COUNT = feedJsonArray
											.length();
								}
								if (feedJsonArray.length() < 1) {
									// Toast.makeText(context,
									// "Usuario sin fotos",
									// Toast.LENGTH_SHORT).show();

								} else {
									for (int i = 0; i < FIRST_LOAD_FEED_COUNT; i++) {
										addFragment(feedPosition);
										feedPosition++;
									}
								}
							}
							// Nuevos elementos para añadir al feed
							else {
								if (BaseActivity.DEBUG)
									MyLog.d(TAG,
											"---- Update FeedJSONArray with more data ---- feedJsonArray.size() = "
													+ feedJsonArray.length());
								JSONArray jsonArrayAux = new JSONArray(result);
								for (int i = 0; i < jsonArrayAux.length(); i++) {
									feedJsonArray.put(jsonArrayAux
											.getJSONObject(i));
									
								}
								estaIncrementandoFeeds = false;
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				};
			};

			// Request en función del tipo de feed
			if (feedMode == BaseActivity.MAIN_FEED) {
				new Feed(context, handler, LoggedUser.id, LoggedUser.koeToken,
						-1, fromTimeStamp, 1, 10, primerFeed).execute();
			} else {
				new UserFeed(context, handler, LoggedUser.id, targetId,
						LoggedUser.koeToken, null, fromTimeStamp,
						HttpConnections.FEED_OLD_ORDER, 10, primerFeed)
						.execute();
			}
		} else {
			// Refresh Feed
			
			if (BaseActivity.DEBUG)
				MyLog.d(TAG, "--- Refreshing feed ----");
			progressDialog = new Dialogos(context).loadingProgressDialog();
			refreshingFeed = true;
			fromTimeStamp = BaseActivity.FIRST_FROM_TIMESTAMP;
			oldFeedJsonArray = feedJsonArray;
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					final String result = (String) msg.obj;
					try {

						// Buscamos nuevos audiosnaps
						JSONArray jsonArrayAux = new JSONArray(result);

						if (feedJsonArray
								.getJSONObject(0)
								.getString(HttpConnections.PIC_HASH)
								.equalsIgnoreCase(
										jsonArrayAux
												.getJSONObject(0)
												.getString(
														HttpConnections.PIC_HASH))) {
							if (BaseActivity.DEBUG)
								MyLog.d(TAG,
										"---- No hay nuevos audiosnaps ----");
							progressDialog.dismiss();
							Toast.makeText(
									context,
									context.getString(R.string.noNewAudioSnaps),
									Toast.LENGTH_SHORT).show();
						} else {
							if (BaseActivity.DEBUG)
								MyLog.d(TAG, "---- Hay nuevos audiosnaps ----");
							oldFeedJsonArray = feedJsonArray;

							feedJsonArray = new JSONArray();
							// Add new audiosnaps
							boolean endUpdate = false;
							for (int i = 0; i < jsonArrayAux.length(); i++) {
								if (!(oldFeedJsonArray.getJSONObject(0)
										.getString(HttpConnections.PIC_HASH)
										.equalsIgnoreCase(jsonArrayAux
												.getJSONObject(i)
												.getString(
														HttpConnections.PIC_HASH)))
										&& !endUpdate) {
									feedJsonArray.put(jsonArrayAux
											.getJSONObject(i));
								} else {
									endUpdate = true;
								}
							}
							// Add old audiosnaps
							for (int i = 0; i < oldFeedJsonArray.length(); i++) {
								feedJsonArray.put(oldFeedJsonArray
										.getJSONObject(i));
							}

							feedJsonArray = jsonArrayAux;
							oldFeedJsonArray = null;
							jsonArrayAux = null;
							primerFeedLoad = true;
							primerFeed = true;
							feedPosition = 0;
							pageSelected = 0;
							pagesToUpdate = 7;
							feedObjectList = new ArrayList<FeedObject>();
							if (primerFeedLoad) {
								if (BaseActivity.DEBUG)
									MyLog.d(TAG,
											"---- Adding 3 first fragments ----");
								primerFeedLoad = false;
								feedJsonArray = new JSONArray(result);
								if (!(feedJsonArray.length() >= FIRST_LOAD_FEED_COUNT)) {
									FIRST_LOAD_FEED_COUNT = feedJsonArray
											.length();
								}
								if (feedJsonArray.length() < 1) {
									Toast.makeText(context,
											"Usuario sin fotos",
											Toast.LENGTH_SHORT).show();

								} else {
									for (int i = 0; i < FIRST_LOAD_FEED_COUNT; i++) {
										addFragment(feedPosition);
										feedPosition++;
									}
								}
							}
							progressDialog.dismiss();
						}
					} catch (Exception e) {
						e.printStackTrace();
						progressDialog.dismiss();
					}
				};
			};

			// Request refresh feed
			new Feed(context, handler, LoggedUser.id, LoggedUser.koeToken, -1,
					fromTimeStamp, 1, 10, primerFeed).execute();
		}
	}

	// Obtenemos jsonArray feed
	private void getOnePictureFeed() {

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				try {
					// Load primeros AudioSnaps al iniciar feed
					if (primerFeedLoad) {
						feedJsonArray = new JSONArray();
						feedJsonArray.put(new JSONObject(result));
						addFragment(feedPosition);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};

		new GetPicture(context, handler, LoggedUser.id, picHash,
				LoggedUser.koeToken, true).execute();
	}

	// Add fragment to feed viewPager
	private void addFragment(int position) {
		
		try {
			FeedObject feedObject = null;
			String fotoUrl = null;
			String picHash = null;

			// Load data into audioSnap jsonObject
			if (!noPhotos) {
				JSONObject jsonAudioSnap = feedJsonArray
						.getJSONObject(position);
				fotoUrl = jsonAudioSnap.getString(HttpConnections.URL);
				picHash = jsonAudioSnap.getString(HttpConnections.PIC_HASH);
				feedObject = gson.fromJson(jsonAudioSnap.toString(),
						FeedObject.class);
			} else {
				JSONObject jsonAudioSnap = feedJsonArray
						.getJSONObject(position);
				feedObject = gson.fromJson(jsonAudioSnap.toString(),
						FeedObject.class);
				// jsonAudioSnap.put(HttpConnections.USER_ID, feedTargetId);
			}

			if (feedObject != null)

				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "---- FEED TYPE ADDED: " + feedMode);
			if (feedMode == BaseActivity.MY_FEED) {
				feedObject.feedMode = BaseActivity.MY_FEED;
			} else if (feedMode == BaseActivity.FRIEND_FEED) {
				feedObject.feedMode = BaseActivity.FRIEND_FEED;
			} else if (feedMode == BaseActivity.ONE_PICTURE_FEED) {
				feedObject.feedMode = BaseActivity.ONE_PICTURE_FEED;
			} else {
				feedObject.feedMode = BaseActivity.MAIN_FEED;
			}

			// Add fragment
			if (noPhotos) {
				progressDialog.dismiss();
				feedObjectList.add(feedObject);
				newFeedVerticalAdapter = new NewFeedVerticalAdapter(context,fragmentManager,feedObjectList,handlerAddFragment,imageLoader);				
				viewPager.setAdapter(newFeedVerticalAdapter);
			} else {
				if (primerFeed) {
					feedObject.first_picture = true;
					progressDialog.dismiss();
					primerFeed = false;
					feedObjectList.add(feedObject);
					newFeedVerticalAdapter = new NewFeedVerticalAdapter(context,fragmentManager, feedObjectList,handlerAddFragment,imageLoader);
					viewPager.setAdapter(newFeedVerticalAdapter);
					setImageAudioSnap(fotoUrl, position, picHash);
				} else {
					// Ya hay fragments en el List, no inicializamos adapter
					if (!refreshingFeed) {
						feedObjectList.add(feedObject);
					} else {
						feedObjectList.add(feedObject);
						refreshingFeed = false;
					}										
					setImageAudioSnap(fotoUrl, position, picHash);
					//newFeedVerticalAdapter.notifyDataSetChanged();
				}
			}
			if (primerUserLoad) {
				// forzamos update user info y comments del primer fragment
				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "---- FEED MODE: " + feedMode);
				// newFeedPageAdapter.getFragment(0).queryUserInfoAndComments(feedMode);

				;
				;
				;
				;
				primerUserLoad = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setImageAudioSnap(String fotoUrl, int position,
			String picHash) {
		// No encontrado en SD
		if (android.os.Build.VERSION.SDK_INT <= 8) {
			fotoUrl = fotoUrl.replaceFirst("https", "http");
		}

		
		if (!audioSnapsFileCache.compruebaAudioSnapCacheado(picHash)) {
			try {
				downloadAndGetAudioSnap(fotoUrl, position, picHash);
			} catch (Exception e) {
				e.printStackTrace();
				if (BaseActivity.DEBUG)
					MyLog.d(TAG,
							"---- Exception de la libreria de descarga asíncrona, volvemos a descargar ----");
			}
		}
	}

	// Descarga AudioSnap
	private void downloadAndGetAudioSnap(String url, final int pos,
			final String id) {

		if (BaseActivity.DEBUG)
			MyLog.d(TAG, "---- Iniciada descarga AudioSnap feed: " + pos
					+ " ----");
		String filename = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + BaseActivity.CACHE_AUDIOSNAPS_FILES + id;

		

		AsyncHttpClient client = AsyncHttpClient.getDefaultInstance();

		client.getFile(url, filename, new AsyncHttpClient.FileCallback() {
			@Override
			public void onCompleted(Exception e, AsyncHttpResponse response,
					File result) {
				
				
				newFeedVerticalAdapter.notifyDataSetChanged();
				if (e != null) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "---- Error en la descarga ----");
					e.printStackTrace();
				}
			}
		});
	}


	public void onContextMenuClosed(Menu menu) {
		
		List<Fragment> fragments = fragmentManager.getFragments();
		if (fragments == null)
			return;
		int i = 0;
		for (Fragment fragment : fragments) {
			((AudioSnapFragment)fragment).onContextMenuClosed(menu);
			i++;
		}		
	}
	
	public boolean onBackPressed() {
		
		List<Fragment> fragments = fragmentManager.getFragments();
		if (fragments == null)
			return false;
		int i = 0;
		for (Fragment fragment : fragments) {
			((AudioSnapFragment)fragment).onBackPressed();
			i++;
		}		

		return false;
	}

	public void first() {
		// viewPager.setCurrentItem(0);
		// newFeedPageAdapter.invalidateSwipe();
	}

	public ListView getViewPager() {
		return viewPager;
	}

	public void setViewPager(ListView viewPager) {
		this.viewPager = viewPager;
	}

	boolean estaIncrementandoFeeds = false;

	

	private void incrementarFeeds() {
		
		estaIncrementandoFeeds = true;
		if (feedObjectList.size() < MAX_VIEWPAGER_FRAGMENTS) {
			pageSelected++;
			pagesToUpdate--;

			try {
				/*
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						// Add new fragment to List
						if (feedJsonArray.length() > feedPosition) {
							addFragment(feedPosition);
							feedPosition++;
						}
						estaIncrementandoFeeds = false;
					}
				}, POST_DELAYED_TIME);
		*/		
				if (feedJsonArray.length() > feedPosition) {
					addFragment(feedPosition);
					feedPosition++;
				}
				estaIncrementandoFeeds = false;
		
			} catch (Exception e) {
				e.printStackTrace();
				if (BaseActivity.DEBUG)
					MyLog.d(TAG, "Exception");
			}

			// Solicitamos más feeds para el jsonArray
			if (pagesToUpdate == 0) {
				pagesToUpdate = 10;
				
				getPhotoForFeed(feedMode, feedTargetId);
			}

		}

	}

	public void destroy() {
		MyLog.i(TAG, "!!! destroy()");
		this.context = null;
		this.fragmentManager = null;
		this.viewPager = null;
		this.gson = null;
		this.progressDialog = null;
		this.newFeedVerticalAdapter = null;
		this.fragmentManager = null;
		this.feedObjectList = null;
	}

	final Handler handlerAddFragment = new Handler() {
		public void handleMessage(Message msg) {
			
			final Integer result = (Integer) msg.obj;
			if(result!=null && result>0 && result==newFeedVerticalAdapter.getCount())
			{				
				int max=newFeedVerticalAdapter.getCount();
				
				 incrementarFeeds();
			}
		}
	};
	
}
