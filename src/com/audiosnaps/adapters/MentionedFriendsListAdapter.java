package com.audiosnaps.adapters;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import com.audiosnaps.log.MyLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.audiosnap.library.util.MentionUtil;
import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.http.HttpConnections;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MentionedFriendsListAdapter extends BaseAdapter implements Filterable {

	private static String TAG = "AutocompletionFriendsListAdapter";
	private JSONArray jsonArrayFriendsOriginal;
	private ArrayList<JSONObject> jsonArrayMentionedFriends;
	private JSONArray jsonArrayFriends;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private LayoutInflater inflator;

	// Constructor
	public MentionedFriendsListAdapter(Context context, JSONArray jsonArrayFriends) {
		this.jsonArrayFriendsOriginal = jsonArrayFriends;
		this.jsonArrayFriends = this.jsonArrayFriendsOriginal;
		this.jsonArrayMentionedFriends = new ArrayList<JSONObject>();
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if(BaseActivity.DEBUG) MyLog.d(TAG, "constructor");
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = inflator.inflate(R.layout.item_lista_mentions, null);
		
		// Views
		TextView koepicsUserName = (TextView) row.findViewById(R.id.autocompleteKoepicsUserName);
		TextView fbUserName = (TextView) row.findViewById(R.id.autocompleteFbUserName);
		TextView twUserName = (TextView) row.findViewById(R.id.autocompleteTwUserName);
		ImageView userAvatar = (ImageView) row.findViewById(R.id.autocompleteUserAvatar);

		ImageView koepicsLogo = (ImageView) row.findViewById(R.id.autocompleteKoepicsLogo);
		ImageView fbLogo = (ImageView) row.findViewById(R.id.autocompleteFbLogo);
		ImageView twLogo = (ImageView) row.findViewById(R.id.autocompleteTwLogo);
		
		JSONObject jsonObject;
		try {
			jsonObject = jsonArrayFriends.getJSONObject(position);

			String userNameKoepics = jsonObject.getJSONObject(HttpConnections.KOEPICS).getString(HttpConnections.USER_NAME);
			String userNameFacebook = jsonObject.getJSONObject(HttpConnections.FACEBOOK).getString(HttpConnections.USER_NAME);
			String userNameTwitter = jsonObject.getJSONObject(HttpConnections.TWITTER).getString(HttpConnections.USER_NAME);
			
			String userIdKoepics = jsonObject.getJSONObject(HttpConnections.KOEPICS).getString(HttpConnections.ID);
			String userIdFacebook = jsonObject.getJSONObject(HttpConnections.FACEBOOK).getString(HttpConnections.ID);
			String userIdTwitter = jsonObject.getJSONObject(HttpConnections.TWITTER).getString(HttpConnections.ID);
			
			if(!userIdKoepics.equals("null")){
				koepicsUserName.setText(userNameKoepics);
				if(!userIdFacebook.equals("null")){
					fbUserName.setText(userNameFacebook);
					if(!userIdTwitter.equals("null")) twUserName.setText(userNameTwitter);
					else twLogo.setVisibility(View.GONE);
				}else{
					twLogo.setVisibility(View.GONE);
					if(!userIdTwitter.equals("null")){
						fbLogo.setImageResource(R.drawable.tagging_soc_tw_2x);
						fbUserName.setText(userNameTwitter);
					}
					else {
						fbLogo.setVisibility(View.GONE);
					}
				}
			}else if(!userIdFacebook.equals("null")){
				koepicsLogo.setImageResource(R.drawable.tagging_soc_fb_2x);
				koepicsUserName.setText(userNameFacebook);
				twLogo.setVisibility(View.GONE);
				if(!userIdTwitter.equals("null")) fbUserName.setText(userNameTwitter); 
				else{
					fbLogo.setVisibility(View.GONE);
				}
			}else if(!userIdTwitter.equals("null")){
				koepicsLogo.setImageResource(R.drawable.tagging_soc_tw_2x);
				koepicsUserName.setText(userNameTwitter);
				fbLogo.setVisibility(View.GONE);
				twLogo.setVisibility(View.GONE);
			}
			
			// User avatar
			imageLoader.displayImage(jsonObject.getJSONObject(HttpConnections.KOEPICS).getString(HttpConnections.PICTURE_URL), userAvatar, BaseActivity.optionsAvatarImage, null);

			if(position % 2 == 1) row.setBackgroundColor(row.getResources().getColor(R.color.gris_claro_mentions));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return row;
	}

	@Override
	public int getCount() {
		System.out.println(jsonArrayFriends.length());
		return jsonArrayFriends.length();
	}

	@Override
	public Object getItem(int position) {
		JSONObject jsonObject = null;
		try {
			jsonObject = jsonArrayFriends.getJSONObject(position);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Filter getFilter() {
		return new AutocompletionCommentFilter();
	}

	public void notifyFriendMentioned(long id) {

		JSONObject jsonObject;
		try {
			jsonObject = jsonArrayFriends.getJSONObject((int) id);
			jsonArrayMentionedFriends.add(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void notifyFriendMentionedDeleted(int i) {
		jsonArrayMentionedFriends.remove(i);
	}

	public JSONObject getFriendMentioned(int i) throws JSONException {
		return jsonArrayMentionedFriends.get(i);
	}

	private class AutocompletionCommentFilter extends Filter {

		private String constraint = "";

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			FilterResults result = new FilterResults();

			if (constraint != null && constraint.toString().length() > 0) {

				JSONArray filteredItems = new JSONArray();

				try {
					for (int i = 0, l = jsonArrayFriendsOriginal.length(); i < l; i++) {
						JSONObject jsonObjectFriend = jsonArrayFriendsOriginal.getJSONObject(i);
						if (startsWith(jsonObjectFriend, constraint.toString().toLowerCase(Locale.getDefault()))
								&& !contains(jsonArrayMentionedFriends, jsonObjectFriend))
							filteredItems.put(jsonObjectFriend);

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				result.count = filteredItems.length();
				result.values = filteredItems;

			} else {

				synchronized (this) {
					result.values = jsonArrayFriendsOriginal;
					result.count = jsonArrayFriendsOriginal.length();
				}

			}

			return result;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			jsonArrayFriends = null;
			JSONArray allMatching = (JSONArray) results.values;
			if (allMatching != null)
				jsonArrayFriends = allMatching;
			if (constraint != null)
				this.constraint = constraint.toString().toLowerCase(Locale.getDefault());
			notifyDataSetChanged();
		}

		private boolean startsWith(JSONObject jsonObjectFriend, String constraint) throws JSONException {
			String koepicsUserName = MentionUtil.getKoepicsUserName(jsonObjectFriend).toLowerCase(Locale.getDefault());
			String facebookUserName = MentionUtil.getFacebookUserName(jsonObjectFriend).toLowerCase(Locale.getDefault());
			String twitterUserName = MentionUtil.getTwitterUserName(jsonObjectFriend).toLowerCase(Locale.getDefault());
			return koepicsUserName.startsWith(constraint) && !koepicsUserName.equals("null")
					|| facebookUserName.startsWith(constraint) && !facebookUserName.equals("null")
					|| twitterUserName.startsWith(constraint) && !twitterUserName.equals("null");
		}

		@Override
		public CharSequence convertResultToString(Object jsonObjectFriend) {

			String result = new String();

			if (jsonObjectFriend instanceof JSONObject) {
				try {
					// if(BaseActivity.DEBUG) MyLog.e(TAG, "constraint: " + constraint);
					result = MentionUtil.getKoepicsUserName((JSONObject) jsonObjectFriend);
					if (result.toLowerCase(Locale.getDefault()).startsWith(constraint))
						return result;
					result = MentionUtil.getFacebookUserName((JSONObject) jsonObjectFriend);
					if (result.toLowerCase(Locale.getDefault()).startsWith(constraint))
						return result;
					result = MentionUtil.getTwitterUserName((JSONObject) jsonObjectFriend);
					if (result.toLowerCase(Locale.getDefault()).startsWith(constraint))
						return result;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return result;
		}

		private boolean contains(ArrayList<JSONObject> jsonArrayMentionedFriends, JSONObject jsonObject)
				throws JSONException {
			for (int i = 0; i < jsonArrayMentionedFriends.size(); i++) {
				if (equals(jsonArrayMentionedFriends.get(i), jsonObject))
					return true;
			}
			return false;
		}

		private boolean equals(JSONObject jsonObject1, JSONObject jsonObject2) throws JSONException {
			return MentionUtil.getKoepicsUserName(jsonObject1).toLowerCase(Locale.getDefault())
							.equals(MentionUtil.getKoepicsUserName(jsonObject2).toLowerCase(Locale.getDefault()))
					&& MentionUtil.getFacebookUserName(jsonObject1).toLowerCase(Locale.getDefault())
							.equals(MentionUtil.getFacebookUserName(jsonObject2).toLowerCase(Locale.getDefault()))
					&& MentionUtil.getTwitterUserName(jsonObject1).toLowerCase(Locale.getDefault())
							.equals(MentionUtil.getTwitterUserName(jsonObject2).toLowerCase(Locale.getDefault()));
		}
	}

}