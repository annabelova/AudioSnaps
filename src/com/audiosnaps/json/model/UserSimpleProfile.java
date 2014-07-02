package com.audiosnaps.json.model;

public class UserSimpleProfile implements Comparable<UserSimpleProfile> {

	public int is_friend;
	public int is_follower;

	public String user_id;		
	public String user_name;		
	public String privacy_mode;
	public String picture_url;
	public String user_caption;

	public int num_of_followers;
	public int num_of_friends;
	public int num_of_pics;
	
	@Override
    public int compareTo(UserSimpleProfile user) {
		
		if(user_name != null){
			if(user.user_name != null) return user_name.compareToIgnoreCase(user.user_name);
		}
		
        return 0;
    }
}
