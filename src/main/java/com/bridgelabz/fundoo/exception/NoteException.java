package com.bridgelabz.fundoo.exception;

public class NoteException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	int errorCode;
	public NoteException(int errorCode, String msg) {
		super(msg);
		this.errorCode=errorCode;
	}
	public int getErrorCode() {
		return errorCode;
	}
}
