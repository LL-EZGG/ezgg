package com.matching.ezgg.global.exception;

public class ExistRiotUsernameandRiotTagException extends BaseException {
	private static final String MESSAGE = "이미 존재하는 Riot Username과 Riot Tag입니다.";

	public ExistRiotUsernameandRiotTagException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 409; // Conflict
	}
}
