package br.com.leandrobove.api.controller;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.leandrobove.api.dto.request.RoleToUserRequest;
import br.com.leandrobove.api.dto.request.UserRequest;
import br.com.leandrobove.api.dto.response.UserResponse;
import br.com.leandrobove.core.security.properties.SecurityProperties;
import br.com.leandrobove.core.security.util.JwtUtil;
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

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private JwtUtil jwtUtil;

	@PreAuthorize("permitAll()")
	@PostMapping(value = "/users")
	public ResponseEntity<UserResponse> saveUser(@RequestBody @Valid UserRequest userRequest) {

		User userSaved = userService.saveUser(userRequest.of());

		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users")
				.pathSegment(userSaved.getId().toString()).toUriString());

		UserResponse userResponse = this.toUserResponse(userSaved);

		return ResponseEntity.created(uri).body(userResponse);
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
	public UserResponse findByUsername(@RequestParam String username) {
		return this.toUserResponse(userService.findByUsernameOrFail(username));
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/users")
	public List<UserResponse> getUsers() {
		return this.toListUserResponse(userService.getUsers());
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/users/{userId}")
	public UserResponse getUserById(@PathVariable Long userId) {
		return this.toUserResponse(userService.findByIdOrFail(userId));
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/users/{username}/roles")
	public List<Role> getRolesByUsername(@PathVariable String username) {
		User user = userService.findByUsernameOrFail(username);

		return user.getAuthorities().stream().map(role -> new Role(null, role.getAuthority()))
				.collect(Collectors.toList());
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

	@GetMapping(value = "/token/refresh")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// get jwt token from header and validate
		Optional<String> authorizationHeader = Optional.ofNullable(request.getHeader(SecurityProperties.HEADER_STRING));
		
		if (authorizationHeader.isEmpty() || !authorizationHeader.get().startsWith(SecurityProperties.TOKEN_PREFIX)) {
			// authorization header not valid, throw an exception
			throw new RuntimeException("header access_token is missing");
		}

		// Extract only token from the header
		String refreshToken = authorizationHeader.get().substring(SecurityProperties.TOKEN_PREFIX.length());

		try {
			// verify if token is valid and not expired
			String username = jwtUtil.extractUsername(refreshToken);
			//UserDetails user = userService.loadUserByUsername(username);
			User userModel = userService.findByUsernameOrFail(username);

			/*if (!jwtUtil.validateToken(refreshToken, user)) {
				// token not valid, go to the next filter..
				throw new RuntimeException("access_token is invalid");
			}*/

			// Access Token
			String accessToken = jwtUtil.generateAccessToken(userModel, request.getRequestURL().toString());

			// generate json response with tokens
			var tokens = new HashMap<String, String>();

			tokens.put("access_token", accessToken);
			tokens.put("refresh_token", refreshToken);

			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			new ObjectMapper().writeValue(response.getOutputStream(), tokens);

		} catch (Exception e) {
			// error message
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(HttpStatus.FORBIDDEN.value());

			var output = new HashMap<String, String>();
			output.put("error_message", e.getMessage());

			new ObjectMapper().writeValue(response.getOutputStream(), output);
		}
	}

	public UserResponse toUserResponse(User user) {
		return modelMapper.map(user, UserResponse.class);
	}

	public List<UserResponse> toListUserResponse(List<User> users) {
		return users.stream().map(this::toUserResponse).collect(Collectors.toList());
	}

}