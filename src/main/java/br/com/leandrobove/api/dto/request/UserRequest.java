package br.com.leandrobove.api.dto.request;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import br.com.leandrobove.domain.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank
	private String name;

	@NotBlank
	private String username;

	@NotBlank
	private String password;

	public User of() {
		return new User(null, this.name, this.username, this.password);
	}

}
