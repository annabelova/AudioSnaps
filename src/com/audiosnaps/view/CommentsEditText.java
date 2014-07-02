package com.audiosnaps.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.audiosnap.library.Range;
import com.audiosnap.library.util.MentionUtil;
import com.audiosnaps.adapters.MentionedFriendsListAdapter;
import com.audiosnaps.span.NoUnderlineSpan;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.Toast;
import android.widget.TextView.BufferType;

public class CommentsEditText extends AutoCompleteTextView {

	// TAGS

	private static final String TAG_ROOT_START = "<root xmlns:koepics=\"koepics\">";

	private static final String TAG_ROOT_END = "</root>";

	private static final String TAG_MENTION_END = "</koepics:user>";

	private static final String TAG_MENTION_START = "<koepics:user k_kp_id=\"%d\" k_fb_id=\"%d\" k_tw_id=\"%d\">";

	// SEPARATORS

	private static final String MENTION_INITIAL_SEPARATOR = "@";

	private static final char MENTION_FINAL_SEPARATOR = ' ';

	// REGULAR EXPRESSIONS

	private static final String HASHTAG_INITIAL_SEPARATOR = "#";

	private static final String HASHTAG_INITIAL_SEPARATOR_REGEX = "(#)";

	private static final String HASHTAG_REGEX = "(#\\S+)";

	public ArrayList<Range> rangeArrayMentionedFriends;

	private int LIMIT_CHARACTERS = 140;

	private int lastMentionInputtedStart = -1;

	private CommentsEditText multiwordAutocompleteTextView = this;

	public CommentsEditText(Context context) {
		super(context);
		init();
	}

	public CommentsEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CommentsEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void init() {

		rangeArrayMentionedFriends = new ArrayList<Range>();

		setMaxLenght(LIMIT_CHARACTERS);

		this.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((MentionedFriendsListAdapter) multiwordAutocompleteTextView.getAdapter()).notifyFriendMentioned(id);
			}

		});

	}

	private void setMaxLenght(int max) {
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter.LengthFilter(max);
		this.setFilters(filters);
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		String newText = text.toString();
		int cursor = getSelectionStart();
		if (cursor == -1)
			cursor = 0;
		if (newText.lastIndexOf(MENTION_INITIAL_SEPARATOR, cursor) != -1
				&& (cursor == newText.length() || newText.charAt(cursor) == MENTION_FINAL_SEPARATOR)) {
			int lastIndex = newText.lastIndexOf(MENTION_INITIAL_SEPARATOR, cursor);
			if (lastIndex >= 0) {
				if (lastIndex + 1 < cursor) {
					newText = newText.substring(lastIndex + 1, cursor);
					if (newText.length() >= getThreshold()) {
						text = newText;
						super.performFiltering(text, keyCode);
					}
				}
			}
		}
	}

	@Override
	protected void replaceText(CharSequence text) {
		Editable newText = getText();
		int cursor = getSelectionStart();
		if (newText.toString().lastIndexOf(MENTION_INITIAL_SEPARATOR, cursor) != -1) {
			int lastIndex = newText.toString().lastIndexOf(MENTION_INITIAL_SEPARATOR, cursor);
			SpannedString spannedText = (SpannedString) TextUtils.concat(newText.subSequence(0, lastIndex + 1),
					text.toString(), newText.subSequence(cursor, newText.length()));
			Spannable spannableText = new SpannableString(spannedText);
			spannableText.setSpan(new UnderlineSpan(), lastIndex, lastIndex + 1 + text.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannableText.setSpan(new ForegroundColorSpan(Color.RED), lastIndex, lastIndex + 1 + text.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			lastMentionInputtedStart = lastIndex + 1 + text.length();
			if (spannableText.length() > LIMIT_CHARACTERS)
				this.setMaxLenght(spannableText.length());
			super.replaceText(spannableText);
			int count = text.length() + 1 - (cursor - lastIndex);
			updateMentionPositions(lastIndex + 1, count);
			rangeArrayMentionedFriends.add(new Range(lastIndex, lastIndex + text.length()));
		} else {
			super.replaceText(text);
		}
	}

	@Override
	public void dismissDropDown() {
		super.dismissDropDown();
		if (lastMentionInputtedStart > 0) {
			Selection.setSelection(getText(), lastMentionInputtedStart);
			lastMentionInputtedStart = -1;
		}
	}

	public String getFormattedComment() {

		StringBuilder comment = new StringBuilder(getText().toString());

		ArrayList<Range> rangeArrayMentionedFriendsCopy = new ArrayList<Range>();

		for (Range range : rangeArrayMentionedFriends)
			rangeArrayMentionedFriendsCopy.add(new Range(range.getStart(), range.getEnd()));

		for (int i = 0; i < rangeArrayMentionedFriendsCopy.size(); i++) {
			try {

				int start = rangeArrayMentionedFriendsCopy.get(i).getStart();
				int end = rangeArrayMentionedFriendsCopy.get(i).getEnd() + 1;

				JSONObject jsonObject = ((MentionedFriendsListAdapter) this.getAdapter()).getFriendMentioned(i);

				String tag = TAG_MENTION_START;

				tag = tag.replaceFirst("%d", MentionUtil.getKoepicsId(jsonObject));
				tag = tag.replaceFirst("%d", MentionUtil.getFacebookId(jsonObject));
				tag = tag.replaceFirst("%d", MentionUtil.getTwitterId(jsonObject));

				comment.insert(start, tag);

				int s = rangeArrayMentionedFriendsCopy.size();
				for (int j = 0; j < s; j++)
					if (start <= rangeArrayMentionedFriendsCopy.get(j).getStart())
						rangeArrayMentionedFriendsCopy.get(j).translate(tag.length());

				end += tag.length();

				comment.insert(end, TAG_MENTION_END);

				s = rangeArrayMentionedFriendsCopy.size();
				for (int j = 0; j < s; j++)
					if (end <= rangeArrayMentionedFriendsCopy.get(j).getStart())
						rangeArrayMentionedFriendsCopy.get(j).translate(TAG_MENTION_END.length());

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		//comment.insert(0, TAG_ROOT_START);
		//comment.insert(comment.length(), TAG_ROOT_END);

		// remplazar & por su codif HTML para evitar problemas con parser XML
		return comment.toString().replaceAll("&", "&amp;");
	}

	private synchronized List<Integer> rangeContainedIn(int start, int end) {
		ArrayList<Integer> index = new ArrayList<Integer>();
		Range range = new Range(start, end);
		for (int i = 0; i < rangeArrayMentionedFriends.size(); i++) {
			Range range2 = rangeArrayMentionedFriends.get(i);
			if (!range.isOver(range2) && !range.isUnder(range2))
				index.add(i);
		}
		return index;
	}

	private synchronized int rangeContains(int start) {
		for (int i = 0; i < rangeArrayMentionedFriends.size(); i++) {
			if (rangeArrayMentionedFriends.get(i).isInside(start))
				return i;
		}
		return -1;
	}

	private synchronized void updateMentionPositions(int start, int count) {
		int s = rangeArrayMentionedFriends.size();
		for (int i = 0; i < s; i++) {
			if (start <= rangeArrayMentionedFriends.get(i).getStart()) {
				rangeArrayMentionedFriends.get(i).translate(count);
			}
		}
	}

	@Override
	public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {

		if (adapter instanceof MentionedFriendsListAdapter)
			multiwordAutocompleteTextView.addTextChangedListener(new MyTextWatcher());

		super.setAdapter(adapter);
	}

	private class MyTextWatcher implements TextWatcher {

		private boolean flag;

		public MyTextWatcher() {
			flag = true;
		}

		@Override
		public void afterTextChanged(Editable editable) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			if (after < count && multiwordAutocompleteTextView.getText().length() < LIMIT_CHARACTERS)
				setMaxLenght(LIMIT_CHARACTERS);

			if (start == 0 && after - count > 0)
				multiwordAutocompleteTextView.updateMentionPositions(start + after, after - count);
			else
				multiwordAutocompleteTextView.updateMentionPositions(start + count, after - count);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

			// s'han esborrat caracters
			if (count < before) {

				Editable text = getEditableText();

				if (rangeArrayMentionedFriends.size() > 0) {

					// Mirar si se borra alguna letra de un mention
					List<Integer> list = rangeContainedIn(start, start + before);

					// s'ha esborrat un, o dintre d'un, mention
					int deleted = 0;
					for (Integer index : list) {
						text = removeMentionedFriend(index.intValue() - deleted, text);
						deleted++;
					}

					setSelection(start);

				}

				if (text.toString().contains(HASHTAG_INITIAL_SEPARATOR)) {
					formatCommentHashtags(text);
				}

			} else if (count > before) {

				Editable text = getEditableText();

				// si se a�ade texto en medio de un mention eliminar de la lista
				// y quitar span
				int index = rangeContains(start);

				// s'ha modificat el text dintre d'un mention
				if (index != -1) {
					rangeArrayMentionedFriends.get(index).translateEnd(count - before + 1);
					text = removeMentionedFriend(index, text);
					multiwordAutocompleteTextView.setSelection(start + count - before);
				}

				if (text.toString().contains(HASHTAG_INITIAL_SEPARATOR)) {
					formatCommentHashtags(text);
				}

			}

		}

		private Editable removeMentionedFriend(int index, Editable editable) {

			Range rangeMentionedFriend = rangeArrayMentionedFriends.get(index);

			// eliminar de FriendsMentionedArray
			((MentionedFriendsListAdapter) multiwordAutocompleteTextView.getAdapter())
					.notifyFriendMentionedDeleted(index);

			rangeArrayMentionedFriends.remove(index);

			int start = rangeMentionedFriend.getStart();
			int end = rangeMentionedFriend.getEnd();

			Editable text = getText();

			if (text.length() >= start) {

				if (text.length() < end)
					end = text.length();

				text.setSpan(new NoUnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.setSpan(new ForegroundColorSpan(Color.BLACK), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			}

			return text;
		}

		private void formatCommentHashtags(Editable text) {

			// check flag for avoiding recursivity in setText
			if (flag) {

				flag = false;

				// save current cursor start/stop
				int start = getSelectionStart();
				int stop = getSelectionEnd();

				// hahstag symbol pattern
				Pattern pattern = Pattern.compile(HASHTAG_INITIAL_SEPARATOR_REGEX);
				Matcher matcher = pattern.matcher(text);

				// Check all occurrences and format text
				while (matcher.find()) {
					text.setSpan(new NoUnderlineSpan(), matcher.start(), matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					text.setSpan(new ForegroundColorSpan(Color.BLACK), matcher.start(), matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				// hahstag pattern
				pattern = Pattern.compile(HASHTAG_REGEX);
				matcher = pattern.matcher(text);

				// Check all occurrences and format text
				while (matcher.find()) {
					text.setSpan(new UnderlineSpan(), matcher.start(), matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					text.setSpan(new ForegroundColorSpan(Color.BLUE), matcher.start(), matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				setText(text, BufferType.SPANNABLE);
				setSelection(start, stop);

				flag = true;

			}

		}

	}

	public void showRanges() {
		int s = rangeArrayMentionedFriends.size();
		StringBuilder builder = new StringBuilder("Ranges: \n");
		for (int i = 0; i < s; i++) {
			Range range = rangeArrayMentionedFriends.get(i);
			builder.append("(" + i + ") ==> start: " + range.getStart() + ", end: " + range.getEnd() + "\n");
		}
		Toast.makeText(getContext(), builder.toString(), Toast.LENGTH_LONG).show();
	}

}
