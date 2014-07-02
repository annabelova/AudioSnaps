package com.audiosnaps.twitter;

import java.util.concurrent.ExecutionException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;

public class TwitterLoginActivity extends Activity {

	public final static String TWITTER_CONSUMER_KEY = "cPWID05OXpbtfYust5lA";
	public final static String TWITTER_CONSUMER_SECRET = "m4Ru2ZBBXwh7LuVkc4rc7THy58YoIaQ9mmOicu2NI";

	public static final String TWITTER_ACCESS_TOKEN_SECRET = "twitter_access_token_secret";
	public static final String TWITTER_ACCESS_TOKEN = "twitter_access_token";

	private static final String OAUTH_VERIFIER = "oauth_verifier";
	private static final String TWITTER_CALLBACK = "twitter-callback:///";
	
	private WebView webView;

	private Twitter mTwitter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.webview);

		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.loader);
		
		webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(TWITTER_CALLBACK)) {
					Uri uri = Uri.parse(url);
					String oauthVerifier = uri.getQueryParameter(OAUTH_VERIFIER);
					// Pair up our request with the response
					if (oauthVerifier != null) {
						try {
							Intent mIntent = new GetTokenAndTokenSecret().execute(oauthVerifier).get();
							if(mIntent != null){
								//Toast.makeText(getApplicationContext(), "RESULT OK", Toast.LENGTH_LONG).show();
								setResult(RESULT_OK, mIntent);
								finish();
								return true;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
					Intent mIntent = new Intent();
					//Toast.makeText(getApplicationContext(), "RESULT CANCELED", Toast.LENGTH_LONG).show();
					setResult(RESULT_CANCELED, mIntent);
					finish();
					return true;
				}
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				progressBar.setVisibility(View.GONE);
			}
		});
		
		new LoadUrlTask().execute();

	}

	class LoadUrlTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				mTwitter = new TwitterFactory().getInstance();
				mTwitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
				return mTwitter.getOAuthRequestToken(TWITTER_CALLBACK).getAuthenticationURL();
			} catch (TwitterException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(String url) {
			webView.loadUrl(url);
		}

	}
	
	class GetTokenAndTokenSecret extends AsyncTask<String, Void, Intent> {

		@Override
		protected Intent doInBackground(String... params) {
			try {
				Intent mIntent = new Intent();
				AccessToken oauthAccessToken = mTwitter.getOAuthAccessToken(params[0]);
				mIntent.putExtra(TWITTER_ACCESS_TOKEN, oauthAccessToken.getToken());
				mIntent.putExtra(TWITTER_ACCESS_TOKEN_SECRET, oauthAccessToken.getTokenSecret());
				return mIntent;
			} catch (TwitterException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
}
