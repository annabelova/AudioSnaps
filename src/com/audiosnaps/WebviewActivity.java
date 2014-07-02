package com.audiosnaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebviewActivity extends Activity {

	private WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.webview);

		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.loader);
		
		webView = (WebView) findViewById(R.id.webview);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				progressBar.setVisibility(View.GONE);
			}
		});
		
		String url = getIntent().getStringExtra("url");
		
		if(url != null) webView.loadUrl(url);
		else invalidUrl();

	}
	
	private void invalidUrl() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getResources().getString(R.string.notLoadedPage));
		alertDialog.setMessage(getResources().getString(R.string.ERROR_LOADING));
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.CLOSE), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
	}
	
	@Override
	public void onBackPressed() {
		if(getIntent().getBooleanExtra(GCMIntentService.FROM_PUSH_NOTIFICATION, false)){
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}	
		super.onBackPressed();
	}
	
}
