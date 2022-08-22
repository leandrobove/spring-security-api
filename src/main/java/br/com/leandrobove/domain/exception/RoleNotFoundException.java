package br.com.leandrobove.domain.exception;

public class RoleNotFoundException extends BusinessRuleException {

	private static final long serialVersionUID = 1L;

	public RoleNotFoundException(String name) {
		super(String.format("Role name: %s not found", name));
	}
}
