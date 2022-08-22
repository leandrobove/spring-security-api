package br.com.leandrobove.api.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.leandrobove.api.dto.RoleToUserRequest;
import br.com.leandrobove.api.dto.UserRequest;
import br.com.leandrobove.domain.model.Role;
import br.com.leandrobove.domain.model.User;
import br.com.leandrobove.domain.repository.RoleRepository;
import br.com.leandrobove.domain.service.RoleService;
import br.com.leandrobove.domain.service.UserService;

@RestController
@RequestMapping(value = "/api")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private RoleRepository roleRepository;

	@PreAuthorize("permitAll()")
	@PostMapping(value = "/users")
	public ResponseEntity<User> saveUser(@RequestBody @Valid UserRequest userRequest) {
		
		User userSaved = userService.saveUser(userRequest.of());
		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/users").pathSegment(userSaved.getId().toString()).toUriString());

		return ResponseEntity.created(uri).body(userSaved);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping(value = "/roles")
	public ResponseEntity<Role> saveRole(@RequestBody Role role) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/roles").toUriString());

		return ResponseEntity.created(uri).body(roleService.saveRole(role));
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping(value = "/users/{username}/roles")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void addRoleToUser(@RequestBody @Valid RoleToUserRequest roleToUserRequest, @PathVariable String username) {
		userService.addRoleToUser(username, roleToUserRequest.getRoleName());
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/roles")
	public List<Role> getRoles() {
		return roleRepository.findAll();
	}

	@PreAuthorize("permitAll()")
	@GetMapping(value = "/users/findByUsername")
	public User findByUsername(@RequestParam String username) {
		return userService.findByUsernameOrFail(username);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/users")
	public Page<User> getUsers(Pageable pageable) {
		return userService.getUsers(pageable);
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/users/{userId}")
	public User getUserById(@PathVariable Long userId) {
		return userService.findByIdOrFail(userId);
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/users/{username}/roles")
	public List<Role> getRolesByUsername(@PathVariable String username) {
		User user = this.findByUsername(username);
		
		return user.getAuthorities().stream().map(
					role -> new Role(null, role.getAuthority())
				).collect(Collectors.toList());
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PutMapping(value = "/users/{username}/active")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> activateUser(@PathVariable String username) {
		userService.activate(username);

		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PutMapping(value = "/users/{username}/deactive")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> deactivateUser(@PathVariable String username) {
		userService.deactivate(username);

		return ResponseEntity.noContent().build();
	}

}