package br.com.leandrobove.api.dto.response;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Set;

import br.com.leandrobove.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid;

	private String name;

	private String username;
	
	private Set<Role> roles;

	private Boolean active;
	
	private OffsetDateTime createdAt;

}
