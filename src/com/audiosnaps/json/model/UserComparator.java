package com.audiosnaps.json.model;

import java.util.Comparator;

public class UserComparator implements Comparator<User> {

	@Override
	public int compare(User friend1, User friend2) {
		return friend1.compareTo(friend2);
	}

}
