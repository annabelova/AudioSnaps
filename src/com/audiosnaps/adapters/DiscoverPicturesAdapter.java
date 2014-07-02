package com.audiosnaps.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.json.model.Pic;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class DiscoverPicturesAdapter extends BaseAdapter {

	private static String TAG = "DiscoverPicturesAdapter";
	private List<Pic> picturesList;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private LayoutInflater inflator;

	// Constructores
	public DiscoverPicturesAdapter(Context context, List<Pic> picturesList) {
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// Get only pictures array
		this.picturesList = picturesList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View view = inflator.inflate(R.layout.item_pictures_discover, null);
		
		// Views
		ImageView pictureDiscover = (ImageView) view.findViewById(R.id.pictureDiscover);
		
		// Spinner shown while image loading
		final ProgressBar spinnerDiscover = (ProgressBar) view.findViewById(R.id.progressBarDiscover);
		
		Pic pic  = picturesList.get(position);
		imageLoader.displayImage(pic.micro_url, pictureDiscover, BaseActivity.optionsGridImage, new SimpleImageLoadingListener() {
		    @Override
		    public void onLoadingStarted(String imageUri, View view) {
		        spinnerDiscover.setVisibility(View.VISIBLE);
		    }

		    @Override
		    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		        spinnerDiscover.setVisibility(View.GONE);
		    }

		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		        spinnerDiscover.setVisibility(View.GONE);
		    }
		});

		return view;
	}

	@Override
	public int getCount() {
		return picturesList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}