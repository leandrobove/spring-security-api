package br.com.leandrobove.core.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.leandrobove.core.security.filter.AuthenticationFilter;
import br.com.leandrobove.core.security.filter.AuthorizationFilter;
import br.com.leandrobove.core.security.properties.SecurityProperties;
import br.com.leandrobove.core.security.util.JwtUtil;
import br.com.leandrobove.domain.service.UserService;

//@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
	
	@Autowired
	private AuthenticationConfiguration configuration;
	
	@Autowired
	private SecurityProperties securityProperties;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	@Lazy
	private UserService userService;
			
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		AuthenticationFilter authenticationFilter = new AuthenticationFilter(this.authenticationManager(), jwtUtil);
		
		//change default login url
		authenticationFilter.setFilterProcessesUrl(securityProperties.getLoginUrl());
		
		http
			.csrf().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
			.authorizeRequests()
				.antMatchers(HttpMethod.POST, securityProperties.getLoginUrl() + "/**").permitAll()
				.anyRequest().authenticated();
		
		//enable exception handling
		http.exceptionHandling().authenticationEntryPoint((request, response, exception) -> {
			response.sendError(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
		});
		
		//add custom filter to do login and create JWT
		http.addFilter(authenticationFilter);
		
		//add priority filter to check if user is logged in or not
		http.addFilterBefore(new AuthorizationFilter(securityProperties, jwtUtil, userService), UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager() throws Exception {
		return configuration.getAuthenticationManager();
	}
	
}
