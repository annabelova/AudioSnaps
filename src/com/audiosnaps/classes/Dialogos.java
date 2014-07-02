package com.audiosnaps.classes;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.audiosnaps.log.MyLog;
import android.view.Window;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;

public class Dialogos {
	
	private final static String TAG = "Dialogos";
	private Context context;
	
	public Dialogos(Context context){
		this.context = context;
	}
	
	// ProgressDialog
	public ProgressDialog loadingProgressDialog() {
		if(BaseActivity.DEBUG) MyLog.d(TAG, "Iniciado ProgressDialog");
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage(context.getResources().getString(R.string.loading));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(true);
		progressDialog.show();
		return progressDialog;
	}
	
	// Alert
	public AlertDialog okAlertDialog(String message){
		if(BaseActivity.DEBUG) MyLog.d(TAG, "Iniciado AlertDialog");
		Builder alertDialogBuilder =  new AlertDialog.Builder(context)
	    .setMessage(message)
	    .setPositiveButton(context.getResources().getString(R.string.OK), 
	    	new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
	    	}
	    );
		return alertDialogBuilder.create();
	}

}
