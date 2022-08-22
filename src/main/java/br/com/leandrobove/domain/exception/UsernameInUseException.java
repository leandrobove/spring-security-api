package br.com.leandrobove.domain.exception;

public class UsernameInUseException extends BusinessRuleException {

	private static final long serialVersionUID = 1L;

	public UsernameInUseException(String username) {
		super(String.format("Username %s is already in use, try to use another one", username));
	}

}
