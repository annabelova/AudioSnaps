package com.audiosnaps.adapters;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;
import com.audiosnaps.fragments.AudioSnapFragment;
import com.audiosnaps.json.model.FeedObject;
import com.audiosnaps.log.MyLog;
import com.nostra13.universalimageloader.core.ImageLoader;

public class NewFeedVerticalAdapter extends ArrayAdapter<FeedObject> {
	private static final String TAG = "NewFeedVerticalAdapter";
	private final Context context;
	private final List<FeedObject> feedObjectList;
	private final HashMap<String, AudioSnapFragment> fragmentKeys = new HashMap<String, AudioSnapFragment>();
	private static final int resource = R.layout.feed_vertical_listview_row;
	private FragmentManager fragmentManager;
	private Handler handler;
	private ImageLoader imageLoader;

	
	
	public NewFeedVerticalAdapter(Context context,
			FragmentManager fragmentManager, List<FeedObject> objects,Handler handler, ImageLoader imageLoader) {
		super(context, resource, objects);
		this.context = context;
		this.feedObjectList = objects;
		this.fragmentManager = fragmentManager;
		this.handler=handler;
		this.imageLoader=imageLoader;

	}

	private String getKey(int position) {
		FeedObject feedObject = feedObjectList.get(position);
		return getKey(position, feedObject);
	}

	private String getKey(int position, FeedObject feedObject) {
		String k = "feed-" + feedObject.feedMode + "-" + position;
		return k;
	}

	public void listFragments() {
		List<Fragment> fragments = fragmentManager.getFragments();
		if (fragments == null)
			return;
		int i = 0;
		for (Fragment fragment : fragments) {
			
			i++;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		FeedObject feedObject = feedObjectList.get(position);
		MyLog.i(TAG, "getView(" + position + ")" + convertView + " - " + parent);
		AudioSnapFragment audioSnapsFragment = null;
		LinearLayout rowview=null;
		String txt="Position: "+position+" \nconvertView: "+convertView+"\nparent: "+parent;
		if (convertView != null && convertView.getTag() != null) {
			rowview=(LinearLayout)convertView;
			audioSnapsFragment = (AudioSnapFragment) rowview.getTag();
			if(audioSnapsFragment.getPosition()!=position)
			{
				/*
			CargaNuevoThread ct = new CargaNuevoThread(feedObject,rowview, position);
			ct.setReuse();
			ct.start();*/
				audioSnapsFragment.reuseFragment(position,feedObject);
			}
			//audioSnapsFragment.reuseFragment(position,feedObject);
		}
		else
		{
			// nuevo elemento
			
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowview = (LinearLayout)inflater.inflate(
					R.layout.feed_vertical_listview_row, parent, false);
			CargaNuevoThread ct = new CargaNuevoThread(feedObject, rowview,position);
			ct.start();
			txt=txt+"\nNuevo elemento";				
		}
		

		DisplayMetrics metrics=MainActivity.getMetrics();
		metrics=null;
		if(metrics!=null)
			rowview.setLayoutParams(new GridView.LayoutParams(metrics.widthPixels, metrics.heightPixels));
		/*
		txt=txt+"\nHeight: "+rowview.getLayoutParams().height;
		txt+="pic_date: "+feedObject.pic_date+" pic_hash: "+feedObject.pic_hash+" "+feedObject.caption.str;		
		TextView text = (TextView)rowview.findViewById(R.id.textView1);
		text.setText(txt);
		*/
		if(position==feedObjectList.size()-1 && feedObject.feedMode!=BaseActivity.ONE_PICTURE_FEED)
			addItem(position);
		//listFragments();
		return rowview;
	}

	private void addItem(int postActual) {
		// TODO Auto-generated method stub
		MyLog.i(TAG, "addItem size="+feedObjectList.size()+" actial pos = "+postActual);
		if(postActual==feedObjectList.size()-1)
		{
			Message msg=new Message(); 
			msg.obj=new Integer(feedObjectList.size());
			this.handler.sendMessage(msg);
		}
	}


	class CargaNuevoThread extends Thread {
		public static final int INSERT = 1;
		public static final int REUSE = 2;
		private LinearLayout rowView = null;
		private FeedObject feedObject = null;
		private int position;
		private int action;
		private String reusedKey;

		CargaNuevoThread(FeedObject feedObject, View v, int position) {
			this.feedObject = feedObject;
			this.rowView = (LinearLayout)v;
			this.position = position;
			this.action = INSERT;
		}

		public void setReuse() {
			this.action = REUSE;
		}
		
		public void setReuse(String k) {
			this.action = REUSE;
			this.reusedKey = k;
		}

		public void run() {
						
			if (action == INSERT) {
				AudioSnapFragment audioSnapsFragment = AudioSnapFragment
						.newInstance(feedObject);
				audioSnapsFragment.setImageLoader(imageLoader);
				audioSnapsFragment.setPosition(position);
				String k = getKey(position, feedObject);
				fragmentKeys.put(k,audioSnapsFragment);
				fragmentManager.beginTransaction()
						.add(R.id.feedViewPagerWrapper, audioSnapsFragment, k)
						.commit();
				audioSnapsFragment.setParentView(rowView);
				rowView.setTag(audioSnapsFragment);
			} else if (action == REUSE) {
				AudioSnapFragment audioSnapsFragment= (AudioSnapFragment)rowView.getTag();
				audioSnapsFragment.reuseFragment(position,feedObject);
			}
			feedObject = null;
			rowView = null;			
			

		}
	}


	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
		List<Fragment> fragments = fragmentManager.getFragments();
		if (fragments == null)
			return;
		int i = 0;
		for (Fragment fragment : fragments) {
			((AudioSnapFragment)fragment).refreshAudioSnapPicture();
		}
	}

	
	
}
