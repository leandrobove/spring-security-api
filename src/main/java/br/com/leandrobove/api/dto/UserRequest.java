package br.com.leandrobove.api.dto;

import javax.validation.constraints.NotBlank;

import br.com.leandrobove.domain.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
	
	@NotBlank
	private String username;
	
	@NotBlank
	private String password;
	
	public User of() {
		return new User(null, this.username, this.password);
	}

}
