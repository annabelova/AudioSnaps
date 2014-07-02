package com.audiosnaps.adapters;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;

import com.audiosnaps.fragments.AudioSnapFragment;
import com.audiosnaps.fragments.UpdateableFragment;
import com.audiosnaps.json.model.FeedObject;
import com.audiosnaps.log.MyLog;

public class NewFeedPageAdapter extends FragmentStatePagerAdapter {

	private static final String TAG = "NewFeedPageAdapter";

	private List<FeedObject> feedObjectList;
	
	private List<AudioSnapFragment> fragmentAudioSnaps;
	
	// Constructor of adapter
	public NewFeedPageAdapter(FragmentManager fm, List<FeedObject> feedObjectList) {
		super(fm);
		this.feedObjectList = feedObjectList;
		fragmentAudioSnaps = new ArrayList<AudioSnapFragment>();
//		 mSelectedPage = 0;
	}
	
	// Return number of pages
	public int getCount() {
		return feedObjectList.size();
	}

	// Return page of view pager
	@Override
	public Fragment getItem(int position) {
		/*AudioSnapFragment audioSnapFragment = getFragmentAt(position);

		if (audioSnapFragment == null) {
			audioSnapFragment = fragments.get(position);
			if(BaseActivity.DEBUG) MyLog.v("WizardActivity", "---- getitem() == NULL ----");
		} else {
			if(BaseActivity.DEBUG) MyLog.v("WizardActivity", "---- getitem() == OK ----");
		}*/

		//mSelectedPage++;
		
		AudioSnapFragment audioSnapsFragment = AudioSnapFragment.newInstance(feedObjectList.get(position));
		fragmentAudioSnaps.add(position, audioSnapsFragment);
		return audioSnapsFragment;
	}

	@Override
	public int getItemPosition(Object item) {
		
		AudioSnapFragment fragment = (AudioSnapFragment)item;
        String picHash = fragment.getPicHash();
        int position = 0;
        if(picHash != null)
			position = indexOf(picHash, feedObjectList);

        // comprobar que position estra dentro del rango feed - margen <= position <= feed + margen (con margen = 12) 
        // y en caso afirmativo, updatear
        
        if (position >= 0) {
        	
        	// refresh audiosnap displayed
        	if (item instanceof UpdateableFragment) {
    			((UpdateableFragment) item).load();
    		}
        	
        	Log.v(TAG, "postion >= 0");
        	
            return position;
            
        }/* else {
        	
        	Log.v(TAG, "POSITION NONE");
        	
        	mSelectedPage--;*/
        	
            return POSITION_NONE;
        //}
		
	}

	private int indexOf(String picHash, List<FeedObject> feedObjects) {
		int i = 0;
		if(feedObjects.size() == 1) return 0;
		for(FeedObject jsonAudioSnap : feedObjects){
			if(jsonAudioSnap.pic_hash != null) 
				if(jsonAudioSnap.pic_hash.equals(picHash)) 
					return i;
			i++;
		}
		return -1;
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
	    super.destroyItem(container, position, object);
	    if(fragmentAudioSnaps.size() > 0) fragmentAudioSnaps.remove(position);
	    System.gc();
	}
	
//	public AudioSnapFragment getFragment(int key) {
//		int i = indexOf(feedObjectList.get(key).pic_hash, feedObjectList);
//		if(i >= 0) return fragmentAudioSnaps.get(i);
//		else return null;
//	}
	
	public AudioSnapFragment get(int pos){
		return fragmentAudioSnaps.get(pos);
	}
	
	public int size(){
		return fragmentAudioSnaps.size();
	}
	
	public void invalidateSwipe(){
		for(AudioSnapFragment fragment : fragmentAudioSnaps){
			fragment.invalidateSwipe();
		}
	}
	
	public void clearAll()
	{
		int max=fragmentAudioSnaps.size();
		for(int i=max-1;i>=0;i--)
			fragmentAudioSnaps.remove(i);
	}
	
}
