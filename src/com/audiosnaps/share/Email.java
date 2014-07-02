package com.audiosnaps.share;

import com.audiosnaps.R;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.widget.Toast;

public class Email {
	
	public static void send(Context context, String userName, String picHash){
		
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.EMAIL_SUBJECT, userName));
		i.putExtra(Intent.EXTRA_TEXT   , Html.fromHtml(context.getResources().getString(R.string.EMAIL_BODY, userName, picHash)));
		try {
		    context.startActivity(Intent.createChooser(i, context.getResources().getString(R.string.SEND_BY_EMAIL)));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
	}
	
}
