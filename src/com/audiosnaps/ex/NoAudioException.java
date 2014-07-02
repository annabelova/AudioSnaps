package com.audiosnaps.ex;

public class NoAudioException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2259729349438244317L;

	public NoAudioException() {
		super();
	}
	
	public NoAudioException(String detailMessage) {
		super(detailMessage);
	}

	public NoAudioException(Throwable throwable) {
		super(throwable);
	}

	public NoAudioException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
