package com.audiosnaps.adapters;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.audiosnaps.fragments.AudioSnapFragment;

public class FeedPageAdapter extends FragmentStatePagerAdapter {
	private List<AudioSnapFragment> fragments;

	public FeedPageAdapter(FragmentManager fm, List<AudioSnapFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
}
