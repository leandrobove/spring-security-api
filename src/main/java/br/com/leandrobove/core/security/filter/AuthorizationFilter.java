package br.com.leandrobove.core.security.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.leandrobove.core.security.properties.SecurityProperties;
import br.com.leandrobove.core.security.util.JwtUtil;
import br.com.leandrobove.domain.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthorizationFilter extends OncePerRequestFilter {

	@Autowired
	private SecurityProperties securityProperties;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserService userService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (request.getServletPath().equals(securityProperties.getLoginUrl()) || request.getServletPath().equals("/api/token/refresh")) {
			// do nothing here, let the request go to next filter..
			filterChain.doFilter(request, response);
			return;
		}

		// get jwt token from header and validate
		Optional<String> authorizationHeader = Optional.ofNullable(request.getHeader(SecurityProperties.HEADER_STRING));

		if (authorizationHeader.isEmpty() || !authorizationHeader.get().startsWith(SecurityProperties.TOKEN_PREFIX)) {
			// authorization header not valid, go to the next filter..
			filterChain.doFilter(request, response);
			return;
		}

		// Extract only token from the header
		String token = authorizationHeader.get().substring(SecurityProperties.TOKEN_PREFIX.length());

		log.info("Token header provided: {}", token);

		try {
			// verify if token is valid and not expired
			String username = jwtUtil.extractUsername(token);
			UserDetails user = userService.loadUserByUsername(username);

			if (!jwtUtil.validateToken(token, user)) {
				// token not valid, go to the next filter..
				filterChain.doFilter(request, response);
				return;
			}

			log.info("User found from token: {}", user);

			// define the user as authenticated and valid
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					user.getUsername(), user.getPassword(), user.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(authenticationToken);

			// continue the filter chain
			filterChain.doFilter(request, response);

		} catch (Exception e) {
			log.error("Error type {} validating user JWT: {}", e.getClass().getName(), e.getMessage());

			// error message
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(HttpStatus.FORBIDDEN.value());

			var output = new HashMap<String, String>();
			output.put("error_message", e.getMessage());

			new ObjectMapper().writeValue(response.getOutputStream(), output);
		}

	}

}
