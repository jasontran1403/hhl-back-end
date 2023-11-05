package com.hhl.exception;

public class JwtExpiredException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public JwtExpiredException() {
	}

	public JwtExpiredException(String message) {
		super(message);
	}

	public JwtExpiredException(Throwable cause) {
		super(cause);
	}

	public JwtExpiredException(String message, Throwable cause) {
		super(message, cause);
	}
}
