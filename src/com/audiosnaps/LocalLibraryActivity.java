package com.audiosnaps;

import com.audiosnap.library.util.LocalLibraryUtil;
import com.audiosnaps.fragments.LocalLibraryListApdater;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class LocalLibraryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_library);
		
		LocalLibraryUtil util = new LocalLibraryUtil();
		util.load(this);
		
		LocalLibraryListApdater adapter = new LocalLibraryListApdater(LocalLibraryActivity.this, util.arrItems);
		
		ListView listView = (ListView) findViewById(R.id.listLocalLibrary);
		listView.setAdapter(adapter);
	}
}
