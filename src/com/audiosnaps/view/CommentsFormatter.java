package com.audiosnaps.view;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class CommentsFormatter {

	private static final String TAG = "CommentsTextView";

	private static final String kpIdEnd = "\"";
	private static final String kpIdStart = "k_kp_id=\"";

	// "<root xmlns:koepics="koepics">@conceptinbox at <koepics:hashtag hashtag="#wayraesbc">#wayraesbc</koepics:hashtag> <koepics:hashtag hashtag="&#x1F60A;">&#x1F60A;</koepics:hashtag></root>"

	// Pattern for gathering @usernames from the Text
	// "(<koepics:user user=\"&?@[^@]+\">&?@[^@]+</koepics:user>)"
	private static Pattern mentionTagsPattern = Pattern.compile("(<koepics:user k_kp_id=\"[0-9]+\" k_fb_id=\"([0-9]+|null)\" k_tw_id=\"([0-9]+|null)\">@[^@]+</koepics:user>)");

	// Pattern for gathering #hasttags from the Text
	private static Pattern hashtagsTagsPattern = Pattern.compile("(<koepics:hashtag hashtag=\"&?#[^\"]+\">&?#[^<]+</koepics:hashtag>)");

	// Pattern for gathering @usernames from the Text
	private static Pattern mentionsPattern = Pattern.compile("(@[^(@|\")]+)");

	// Pattern for finding user ids

	// Pattern for gathering #hasttags from the Text
	private static Pattern hashTagsPattern = Pattern.compile("(&?#[^\")]+)");

	private static String DISCOVER_URL = "com.audiosnaps.discover://";
	private static String FEED_URL = "com.audiosnaps.feed://";

	private String koepicsStartTag = "<root xmlns:koepics=\"koepics\">";

	private String koepicsEndTag = "</root>";

	/**
	 * 
	 * @param text
	 * @return
	 */
	public static String replaceHashTags(String text) {

		Log.v(TAG, text);
		
		StringBuilder result = new StringBuilder(text);
		
		// Matcher matching the pattern
		Matcher m = hashtagsTagsPattern.matcher(text);
		int translation = 0;
		
		while (m.find()) {
			String hashtag = (new Scanner(m.group())).findWithinHorizon(hashTagsPattern, 0);
			String replacement = "<b><font color=\"#4d4134\"><a style=\"text-decoration: none\" href=\"" + DISCOVER_URL  + hashtag + "\">" + hashtag + "</a></font></b>";
			result.replace(m.start() + translation, m.end() + translation, replacement);
			translation += replacement.length() - m.group().length();  
		}

		Log.v(TAG, result.toString());
		
		return result.toString();
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static String replaceMentions(String text) {

		StringBuilder result = new StringBuilder(text);
		
		// Matcher matching the pattern
		Matcher m = mentionTagsPattern.matcher(text);
		int translation = 0;
		
		while (m.find()) {

			String mention = (new Scanner(m.group())).findWithinHorizon(mentionsPattern, 0).split("</koepics:user>")[0];

			Log.v(TAG, mention);
			
			// Obtener el id del usuario
			String id = null;
			try {
				id = extractKoepicsId(m.group());
				String replacement = "<b><font color=\"#000000\"><a style=\"text-decoration: none\" href=\"" + FEED_URL  + id + "\">" + mention + "</a></font></b>";
				result.replace(m.start() + translation, m.end() + translation, replacement);
				translation += replacement.length() - m.group().length();
			} catch (Exception e) {
				result.replace(m.start(), m.end(), mention);
				translation += mention.length() - m.group().length();
				e.printStackTrace();
				break;
			}

		}

		Log.v(TAG, result.toString());
		
		return result.toString();
	}
	
	public static String formatUser(String username, int id){
		return "<b><font color=\"#808080\"><a style=\"text-decoration: none\" href=\"" + FEED_URL  + id + "\">" + username + "</a></font></b>";
	}
	
	/*
	 * Get koepics_id from a user tag
	 */
	private static String extractKoepicsId(String text) throws NumberFormatException {
		int start = text.indexOf(kpIdStart);
		int end = text.indexOf(kpIdEnd, start + kpIdStart.length());
		return text.substring(start + kpIdStart.length(), end);
	}
}
