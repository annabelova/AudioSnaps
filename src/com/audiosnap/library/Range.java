package com.audiosnap.library;

public class Range {

	private int start;
	private int end;
	
	public Range(int start, int end){
		this.start = start;
		this.end = end;
	}
	
	public int getStart(){
		return start;
	}
	
	public int getEnd(){
		return end;
	}
	
	public void setStart(int start){
		this.start = start;
	}
	
	public void setEnd(int end){
		this.end = end;
	}
	
	/**
	 * Checks if the given range is over this range
	 * 
	 * @param range
	 * @return
	 */
	public boolean isUnder(Range range){
		return range.getStart() < start && range.getEnd() < start;
	}
	
	/**
	 * Checks if the given range is under this range
	 * 
	 * @param range
	 * @return
	 */
	public boolean isOver(Range range){
		return range.getStart() > end && range.getEnd() > end;
	}
	
	
	public boolean isInside(int point){
		return start < point && point < end;
	}
	
	/**
	 * 
	 * @param translation
	 */
	public void translateStart(int translation){
		start += translation;
	}
	
	/**
	 * 
	 * @param translation
	 */
	public void translateEnd(int translation){
		end += translation;
	}
	
	/**
	 * Translates the inferior limit the quantity specified by startTranslation and the superior limit the quantity specified by endTranslation.
	 * All translations are done towards higher positions, at least if the quantity is not negative.
	 *   
	 * @param startTranslation
	 * @param endTranslation
	 */
	public void translate(int startTranslation, int endTranslation){
		translateStart(startTranslation);
		translateEnd(endTranslation);
	}
	
	/**
	 * Translates the inferior and superior limits by the quantity specified by translation.
	 * 
	 * @param translation
	 */
	public void translate(int translation){
		translate(translation, translation);
	}

}

