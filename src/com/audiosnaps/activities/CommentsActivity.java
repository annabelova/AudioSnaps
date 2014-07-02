package com.audiosnaps.activities;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TextView;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.adapters.CommentsAdapter;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.facebook.FacebookManager;
import com.audiosnaps.fragments.AudioSnapFragment;
import com.audiosnaps.http.AddSocNetworkFromMobile;
import com.audiosnaps.http.CommentPicture;
import com.audiosnaps.http.GetCommentsForPicture;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.http.SharePictureInSoc;
import com.audiosnaps.json.model.Comment;
import com.audiosnaps.json.model.FeedObject;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.twitter.TwitterLoginActivity;
import com.audiosnaps.view.CommentsEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sromku.simple.fb.SimpleFacebook;

public class CommentsActivity extends Activity {

	private final String TAG = "CommentsActivity";
	private CommentsEditText txtCommentFeed;
	private TableLayout tableViewCommentsFeed;
	private List<Comment> commentsList;
	private CommentsAdapter commentsAdapter;
	private CheckBox btnFacebookCommentFeed;
	private CheckBox btnTwitterCommentFeed;
	private SimpleFacebook mSimpleFacebook;
	private FeedObject feedObject;
	private static final int INTERVALO_PARA_REFRESCAR_COMENTARIOS = 30000;

	private String stringComments;
	private Timer refreshCommentsTimer;
	private RefreshCommentsTimerTask refreshCommentsTask;
	private int comments = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		feedObject = (FeedObject) getIntent().getExtras().get("feedObject");
		setContentView(R.layout.activity_comments);
		MyLog.d(TAG, "onCreate");
		txtCommentFeed = (CommentsEditText) findViewById(R.id.txtCommentFeed);
		tableViewCommentsFeed = (TableLayout) findViewById(R.id.tableViewCommentsFeed);
		commentsAdapter = new CommentsAdapter(tableViewCommentsFeed);
		btnFacebookCommentFeed = (CheckBox) findViewById(R.id.btnFacebookCommentFeed);
		btnTwitterCommentFeed = (CheckBox) findViewById(R.id.btnTwitterCommentFeed);
		mSimpleFacebook = SimpleFacebook.getInstance(getActivity());
		initSocialNeworkButtons();
		initCommentsAndMention();
		loadCommentsInfo();
		queryComments();
	}

	private Activity getActivity() {
		return this;
	}

	public void onBackPressed() {
	
		Intent returnIntent = new Intent();
		 returnIntent.putExtra("feedObject",feedObject);
		 setResult(RESULT_OK,returnIntent);     
		 finish();
	}
	
	// Init comments and mention list
	private void initCommentsAndMention() {
		final String stringComments = null;
		final String pic_hash = feedObject.pic_hash;
		initComments(this, stringComments, pic_hash);
		// initMentionedFriendsList();

	}

	// Inicializa layout comentarios
	private void initComments(Activity view, final String string,
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
				inputManager.hideSoftInputFromWindow(getActivity()
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

				final Handler handlerComment = new Handler() {
					public void handleMessage(Message msg) {
						final boolean result = (Boolean) msg.obj;
						try {
							if (result) {

								txtCommentFeed.setText("");
								refreshComments();
								feedObject.comment_data.total_comments++;
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

	private void initSocialNeworkButtons() {

		Activity activity = getActivity();

		if (activity != null && btnTwitterCommentFeed != null
				&& btnFacebookCommentFeed != null) {

			final SharedPreferences prefs = activity.getSharedPreferences(
					BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);

			// Twitter
			btnTwitterCommentFeed = (CheckBox) findViewById(R.id.btnTwitterCommentFeed);

			btnTwitterCommentFeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (BaseActivity.DEBUG)
						MyLog.d(TAG, "Pulsamos botón fb like");

					if (!prefs.getBoolean(BaseActivity.TWITTER_IS_LOGGED_IN,
							false)) {
						btnTwitterCommentFeed.setSelected(false);
						loginToTwitter(AudioSnapFragment.REQUEST_CODE_TWITTER_LOGIN_COMMENTS);
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
			btnFacebookCommentFeed = (CheckBox) findViewById(R.id.btnFacebookCommentFeed);

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

	// Twitter Login
	private void loginToTwitter(int requestCode) {
		// Launch twitter activity
		Intent intent = new Intent(getActivity(), TwitterLoginActivity.class);
		startActivityForResult(intent, requestCode);
	}

	private void loadCommentsInfoNEW() {

		TextView row = new TextView(tableViewCommentsFeed.getContext());
		row.setText(tableViewCommentsFeed.getContext().getString(
				R.string.LOADING_COMMENTS));
		row.setGravity(Gravity.CENTER);
		tableViewCommentsFeed.addView(row);

		commentsAdapter = new CommentsAdapter(tableViewCommentsFeed);

		if (!feedObject.no_photos) {
			fillLayoutComments(null);
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

			if (!feedObject.no_photos) {
				if (comments < 0) {
					comments = Integer
							.valueOf(feedObject.comment_data.total_comments);
				}
			
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
					fillLayoutComments(null);
				}
			}
			
		}
	
	public void queryComments() {

		commentsAdapter.clear();

		if (!feedObject.no_photos) {

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
							refreshCommentsTask = new RefreshCommentsTimerTask();
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
				if (!feedObject.no_photos) {
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
								/* Habría que avisar que hay más comentarios */
								// btnComentariosFeed.setText(comments + " " +
								// getActivity().getResources().getString(R.string.COMMENTS));

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
								/* Habría que avisar que hay más comentarios */
								// btnComentariosFeed.setText(comments + " " +
								// getActivity().getResources().getString(R.string.COMMENTS));

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

	@Override
		protected void onPause() {
		
			if (refreshCommentsTask != null && refreshCommentsTimer != null) {
				refreshCommentsTask.cancel();
				refreshCommentsTimer.cancel();
				refreshCommentsTimer.purge();
				refreshCommentsTimer=null;
				refreshCommentsTask=null;
				
			}
			super.onPause();
		}

	@Override
	protected void onResume() {
		
		if (refreshCommentsTask == null && refreshCommentsTimer == null) {
			refreshCommentsTask = new RefreshCommentsTimerTask();
			refreshCommentsTimer = new Timer();
			refreshCommentsTimer.schedule(refreshCommentsTask,
					INTERVALO_PARA_REFRESCAR_COMENTARIOS,
					INTERVALO_PARA_REFRESCAR_COMENTARIOS);
		}
		super.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	
		if (requestCode == AudioSnapFragment.REQUEST_CODE_TWITTER_LOGIN_COMMENTS) {

			if (resultCode == Activity.RESULT_OK) {

				String token = data.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN);
				String tokenSecret = data.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN_SECRET);

				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						final String result = (String) msg.obj;
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							SharedPreferences sharedPreferences = getActivity().getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, true);
							editor.commit();

							btnTwitterCommentFeed.setSelected(true);

						}
					};
				};

				AddSocNetworkFromMobile addSocNetworkFromMobile = new AddSocNetworkFromMobile(getActivity(), handler, LoggedUser.id, "twitter", null, token, tokenSecret, LoggedUser.koeToken);
				addSocNetworkFromMobile.execute();

			}
		} else if (requestCode == AudioSnapFragment.REQUEST_CODE_TWITTER_LOGIN_MENU) {

			if (resultCode == Activity.RESULT_OK) {

				String token = data.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN);
				String tokenSecret = data.getStringExtra(TwitterLoginActivity.TWITTER_ACCESS_TOKEN_SECRET);

				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						final String result = (String) msg.obj;
						if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

							SharedPreferences sharedPreferences = getActivity().getSharedPreferences(BaseActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putBoolean(BaseActivity.TWITTER_IS_LOGGED_IN, true);
							editor.commit();

							sharePicInTwitter();

						}
					};
				};

				AddSocNetworkFromMobile addSocNetworkFromMobile = new AddSocNetworkFromMobile(getActivity(), handler, LoggedUser.id, "twitter", null, token, tokenSecret, LoggedUser.koeToken);
				addSocNetworkFromMobile.execute();

			}

		}
		else if(requestCode==BaseActivity.COMMENTS_REQUEST_CODE)
			{
		
			FeedObject fo=data.getExtras().getParcelable("feedObject");
			feedObject.comment_data.total_comments=fo.comment_data.total_comments;
			comments = feedObject.comment_data.total_comments;
			//btnComentariosFeed.setText(comments + " " + getActivity().getResources().getString(R.string.COMMENTS));
			loadCommentsInfo();			
			}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}

	}
	
	private void sharePicInTwitter() {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

					// TODO Toast.makeText(getActivity(), "Your picture has been shared in Twitter", Toast.LENGTH_LONG).show();

				}
			};
		};
		SharePictureInSoc sharePictureInSoc = new SharePictureInSoc(getActivity(), LoggedUser.id, LoggedUser.koeToken, feedObject.pic_hash, "tw", handler);
		sharePictureInSoc.execute();
	}
	
	private void sharePicInFacebook() {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String result = (String) msg.obj;
				if (!result.equalsIgnoreCase(HttpConnections.ERROR)) {

					// TODO Toast.makeText(getActivity(), "Your picture has been shared in Twitter", Toast.LENGTH_LONG).show();

				}
			};
		};
		SharePictureInSoc sharePictureInSoc = new SharePictureInSoc(getActivity(), LoggedUser.id, LoggedUser.koeToken, feedObject.pic_hash, "fb", handler);
		sharePictureInSoc.execute();
	}
}
