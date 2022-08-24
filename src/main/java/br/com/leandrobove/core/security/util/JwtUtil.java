package br.com.leandrobove.core.security.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import br.com.leandrobove.core.security.properties.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

	@Autowired
	private SecurityProperties securityProperties;

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	@SuppressWarnings("deprecation")
	private Claims extractAllClaims(String token) {
		return Jwts.parser().setSigningKey(securityProperties.getSecret()).parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public String generateAccessToken(UserDetails userDetails, String issuer) {
		Map<String, Object> claims = new HashMap<>();

		claims.put("roles",
				userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

		return createAccessToken(claims, userDetails.getUsername(), issuer);
	}

	public String generateRefreshToken(UserDetails userDetails, String issuer) {
		Map<String, Object> claims = new HashMap<>();

		claims.put("roles",
				userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

		return createRefreshToken(claims, userDetails.getUsername(), issuer);
	}

	@SuppressWarnings("deprecation")
	private String createAccessToken(Map<String, Object> claims, String subject, String issuer) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setIssuer(issuer).setExpiration(SecurityProperties.ACCESS_TOKEN_EXPIRATION_TIME)
				.signWith(SignatureAlgorithm.HS256, securityProperties.getSecret()).compact();
	}

	@SuppressWarnings("deprecation")
	private String createRefreshToken(Map<String, Object> claims, String subject, String issuer) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setIssuer(issuer).setExpiration(SecurityProperties.REFRESH_TOKEN_EXPIRATION_TIME)
				.signWith(SignatureAlgorithm.HS256, securityProperties.getSecret()).compact();
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

}
