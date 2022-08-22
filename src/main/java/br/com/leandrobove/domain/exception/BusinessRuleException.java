package br.com.leandrobove.domain.exception;

public class BusinessRuleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BusinessRuleException() {
		super();
	}

	public BusinessRuleException(String message) {
		super(message);
	}

	public BusinessRuleException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
