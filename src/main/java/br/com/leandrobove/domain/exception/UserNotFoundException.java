package br.com.leandrobove.domain.exception;

public class UserNotFoundException extends BusinessRuleException {

	private static final long serialVersionUID = 1L;

	public UserNotFoundException(String username) {
		super(String.format("Username %s not found", username));
	}

	public UserNotFoundException(Long userId) {
		super(String.format("User id %d not found", userId));
	}
}
