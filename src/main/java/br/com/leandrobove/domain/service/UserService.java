package br.com.leandrobove.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leandrobove.domain.exception.BusinessRuleException;
import br.com.leandrobove.domain.exception.UserNotFoundException;
import br.com.leandrobove.domain.exception.UsernameInUseException;
import br.com.leandrobove.domain.model.Role;
import br.com.leandrobove.domain.model.User;
import br.com.leandrobove.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleService roleService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = this.findByUsernameOrFail(username);
		
		log.info("USER LOADED BY USERDETAILS ---> {}", user);

		log.info("User object: {}", user);
		
		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				user.getActive(), 
				user.isAccountNonExpired(),
				user.isCredentialsNonExpired(), 
				user.isAccountNonLocked(),
				user.getAuthorities());
	}

	@Transactional
	public User saveUser(User user) {
		log.info("Saving new user {} to the database", user.getUsername());

		// Verify if the username already exists
		if (!userRepository.findByUsername(user.getUsername()).isEmpty()) {
			throw new UsernameInUseException(user.getUsername());
		}

		// Encrypt user password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Define default role as user
		user.addRole(roleService.findByName("ROLE_USER"));

		return userRepository.save(user);
	}

	@Transactional
	public void addRoleToUser(String username, String roleName) {
		User user = this.findByUsernameOrFail(username);
		Role role = roleService.findByName(roleName);

		log.info("Adding role: {} to username: {}", role.getName(), user.getUsername());

		user.addRole(role);
	}

	public User findByUsernameOrFail(String username) {
		log.info("Fetching user by username: {}", username);

		return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
	}

	public User findByIdOrFail(Long userId) {
		log.info("Fetching user by id: {}", userId);

		return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
	}

	public Page<User> getUsers(Pageable pageable) {
		log.info("Fetching all users");

		return userRepository.findAll(pageable);
	}

	@Transactional
	public void activate(String username) {
		User user = this.findByUsernameOrFail(username);

		user.activate();
	}

	@Transactional
	public void deactivate(String username) {
		User user = this.findByUsernameOrFail(username);
		
		// do not allow disable admin's
		if(user.isAdmin()) {
			throw new BusinessRuleException("You cannot deactivate an admin user");
		}

		user.deactivate();
	}
}
