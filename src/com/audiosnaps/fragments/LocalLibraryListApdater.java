package com.audiosnaps.fragments;

import java.util.ArrayList;

import com.audiosnap.library.util.LocalLibraryUtil;
import com.audiosnaps.R;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;

public class LocalLibraryListApdater extends ArrayAdapter<LocalLibraryUtil.LocalItem> implements OnClickListener, OnTouchListener
{
	private final Context context;
	private final ArrayList<LocalLibraryUtil.LocalItem> arrItems;
	private boolean enabledEditing = false;
	
	private Button deleteBtn;
	
	public LocalLibraryListApdater(Context context, ArrayList<LocalLibraryUtil.LocalItem> values)
	{
		super(context, R.layout.cell_local_library, values);
		this.context = context;
		this.arrItems = values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LocalLibraryUtil.LocalItem item = (LocalLibraryUtil.LocalItem) arrItems.get(position);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		 
		View rowView = inflater.inflate(R.layout.cell_local_library, parent, false);
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewPhoto);
		imageView.setImageBitmap(item.getImage());
		
		return rowView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	public void setEnableEditing() {
		this.enabledEditing = true;
	}
}
