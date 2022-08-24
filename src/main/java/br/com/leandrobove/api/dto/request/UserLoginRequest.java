package br.com.leandrobove.api.dto.request;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank
	private String username;
	
	@NotBlank
	private String password;

}
