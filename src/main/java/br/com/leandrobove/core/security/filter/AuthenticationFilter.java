package br.com.leandrobove.core.security.filter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.leandrobove.core.security.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private AuthenticationManager authenticationManager;
	
	private JwtUtil jwtUtil;
	
	public AuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		// get credentials from request form
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		log.info("Username {} and password {}", username, password);

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
				password);
		
		return authenticationManager.authenticate(authenticationToken);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException, ServletException {
		
		log.info("User logged in sucessful!");

		// authenticated user
		User user = (User) authentication.getPrincipal();
		
		// Create JWT accesstoken and refreshtoken
		
		// Access Token
		String accessToken = jwtUtil.generateAccessToken(user, request.getRequestURL().toString());
		
		//Refresh Token
		String refreshToken = jwtUtil.generateRefreshToken(user, request.getRequestURL().toString());
		
		//generate json response with tokens
		var tokens = new HashMap<String, String>();
		
		tokens.put("access_token", accessToken);
		tokens.put("refresh_token", refreshToken);
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);
	}

	@Autowired
	@Override
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		super.setAuthenticationManager(authenticationManager);
	}

}
