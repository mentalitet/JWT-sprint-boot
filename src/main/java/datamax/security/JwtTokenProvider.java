package datamax.security;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import datamax.dto.request.RefreshTokenRequestDTO;
import datamax.model.AppUserRole;
import datamax.model.RefreshToken;
import datamax.repository.UserRepository;
import datamax.service.RefreshTokenService;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import datamax.exception.CustomException;

@Component
public class JwtTokenProvider {

  /**
   * THIS IS NOT A SECURE PRACTICE! For simplicity, we are storing a static key here. Ideally, in a
   * microservices' environment, this key would be kept on a config-server.
   */
  @Value("${security.jwt.token.secret-key:secret-key}")
  private String secretKey;

  @Value("${security.jwt.token.expire-length:3600000}")
  private long validityInMilliseconds = 3600000; // 1h

  @Value("${security.jwt.token.jwtRefreshExpirationMs:refreshTokenDurationMs}")
  private Long refreshTokenDurationMs;

  @Autowired
  private MyUserDetails myUserDetails;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private UserRepository userRepository;

  @PostConstruct
  protected void init() {
    secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
  }

  public ResponseEntity<?> generateTokenPair(String email, List<AppUserRole> appUserRoles) {
    Date nowTimestamp  = new Date();

    Claims claims = Jwts.claims().setSubject(email);
    claims.put("auth", appUserRoles.stream().map(s -> new SimpleGrantedAuthority(s.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));

    String accessToken = Jwts.builder()//
        .setClaims(claims)//
        .setIssuedAt(nowTimestamp)//
        .setExpiration(new Date(nowTimestamp.getTime() + validityInMilliseconds))//
        .signWith(SignatureAlgorithm.HS256, secretKey)//
        .compact();

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setAppUser(userRepository.findByEmail(email));
    refreshToken.setExpiryDate(new Date(nowTimestamp.getTime() + refreshTokenDurationMs));
    refreshToken.setRefreshToken(UUID.randomUUID().toString());
    refreshTokenService.save(refreshToken);

    HashMap<String, String> tokens = new HashMap<>();
    tokens.put("AccessToken:", accessToken);
    tokens.put("RefreshToken:", refreshToken.getRefreshToken());

    return ResponseEntity.ok(tokens);
  }

  public Authentication getAuthentication(String token) {
    UserDetails userDetails = myUserDetails.loadUserByUsername(getUsername(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getUsername(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
  }

  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
      return true;
    } catch (IllegalArgumentException e) {
      throw new CustomException("Access JWT claims string is empty: {}", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (ExpiredJwtException e) {
      throw new CustomException("Access JWT Access token has expired", HttpStatus.UNAUTHORIZED);
    } catch (MalformedJwtException e) {
      throw new CustomException("Invalid Access JWT token", HttpStatus.BAD_REQUEST);
    } catch (SignatureException e) {
      throw new CustomException("Invalid Access JWT signature", HttpStatus.UNAUTHORIZED);
    } catch (JwtException e) {
      throw new CustomException("Access JWT validation failed", HttpStatus.UNAUTHORIZED);
    }
  }

  public ResponseEntity<?> refreshTokenPair(RefreshTokenRequestDTO refreshTokenRequest) {
    RefreshToken refreshToken = refreshTokenService.findByRefreshToken(refreshTokenRequest.getRefreshToken());
    refreshTokenService.verifyExpiration(refreshToken);
    return generateTokenPair(refreshToken.getAppUser().getEmail(), refreshToken.getAppUser().getAppUserRoles());
  }
}
