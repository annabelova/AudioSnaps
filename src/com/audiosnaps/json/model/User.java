package com.audiosnaps.json.model;

public class User implements Comparable<User> {

	public int is_friend;
	public int is_follower;
	public Koepics koepics;
	public Facebook facebook;
	public Twitter twitter;

	public User(){
		koepics = new Koepics();
		facebook = new Facebook();
		twitter = new Twitter();
	}
	
	@Override
    public int compareTo(User friend) {
		
		if(koepics.user_name != null){
			if(friend.koepics.user_name != null) return koepics.user_name.compareToIgnoreCase(friend.koepics.user_name);
		}
		
		if(facebook.user_name != null){
			if(friend.facebook.user_name != null) return facebook.user_name.compareToIgnoreCase(friend.facebook.user_name);
		}
		
		if(twitter.user_name != null){
			if(friend.twitter.user_name != null) return twitter.user_name.compareToIgnoreCase(friend.twitter.user_name);
		}
		
        return -1;
    }
	
}
