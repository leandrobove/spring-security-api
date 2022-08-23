package br.com.leandrobove.core.security.properties;

import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Configuration
@ConfigurationProperties("jwt.security")
public class SecurityProperties {

	private String secret;
	private String loginUrl;

	public static final Date ACCESS_TOKEN_EXPIRATION_TIME = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
	public static final Date REFRESH_TOKEN_EXPIRATION_TIME = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = HttpHeaders.AUTHORIZATION;

}
