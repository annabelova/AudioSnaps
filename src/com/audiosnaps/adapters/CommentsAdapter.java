package com.audiosnaps.adapters;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.audiosnap.library.util.DateUtil;
import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.json.model.Comment;
import com.audiosnaps.view.CommentsFormatter;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CommentsAdapter {

	private static String TAG = "CommentsAdapter";
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private LayoutInflater inflator;
	private TableLayout tableLayout;
	private boolean first;
	
	// Constructor
	public CommentsAdapter(TableLayout table) {
		this.tableLayout = table;
		inflator = (LayoutInflater) table.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		first = true;
	}

	public synchronized void addRow(Comment comment) {
		
		TableRow row = (TableRow) inflator.inflate(R.layout.item_lista_comments, null);
		
		// Views
		TextView userComment = (TextView) row.findViewById(R.id.lblUserComment);
		TextView added = (TextView) row.findViewById(R.id.lblCommentTime);
		ImageView userAvatar = (ImageView) row.findViewById(R.id.imgAvatarComments);

		// Load data
		try {

			// User data
			//userComment.gatherLinksForText(Integer.toString(comment.user_id), comment.user_name, comment.comment);
			userComment.setText(Html.fromHtml(
					CommentsFormatter.formatUser(comment.user_name, comment.user_id) + " " 
							+ CommentsFormatter.replaceMentions(CommentsFormatter.replaceHashTags(comment.comment))));
			MovementMethod m = userComment.getMovementMethod();
			if ((m == null) || !(m instanceof LinkMovementMethod)) if (userComment.getLinksClickable()) userComment.setMovementMethod(LinkMovementMethod.getInstance());
			
			try {
				if (comment.added.length() > 0) {
					if (comment.added.equalsIgnoreCase("-1")) {
						added.setText(tableLayout.getContext().getString(R.string.FEW_SECONDS_AGO));
					} else {
						added.setText(DateUtil.formatTimeAgo(comment.added, tableLayout.getContext()));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// User avatar
			imageLoader.displayImage(comment.profile_pic_url, userAvatar, BaseActivity.optionsAvatarImage, null);
			
			if(first){
				ImageView separator = (ImageView) row.findViewById(R.id.separator);
				separator.setVisibility(View.GONE);
				if(tableLayout.getChildCount() > 0) tableLayout.removeViewAt(0);
				tableLayout.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				first = false;
			}
			
			tableLayout.addView(row);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public synchronized void clear(){
		if(!first){
			tableLayout.removeAllViews();
			TextView row = new TextView(tableLayout.getContext());
			row.setText(tableLayout.getContext().getString(R.string.LOADING_COMMENTS));
			row.setGravity(Gravity.CENTER);
			tableLayout.addView(row);
			tableLayout.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			first = true;
		}
	}
	
	public int size(){
		return tableLayout.getChildCount() - ((first) ? 1 : 0);
	}
	
}