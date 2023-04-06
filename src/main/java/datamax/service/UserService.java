package datamax.service;

import javax.servlet.http.HttpServletRequest;

import datamax.dto.request.RefreshTokenRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import datamax.exception.CustomException;
import datamax.model.AppUser;
import datamax.repository.UserRepository;
import datamax.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;

  public ResponseEntity<?> login(String email, String password) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, password)
      );

      //UserDataDTO userDetails = (UserDataDTO) authentication.getPrincipal();
      AppUser appUser = userRepository.findByEmail(email);
      return jwtTokenProvider.generateTokenPair(email, appUser.getAppUserRoles());

    } catch (AuthenticationException e) {
      throw new CustomException("Invalid email/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  public ResponseEntity<?> register(AppUser appUser) {
    if (!userRepository.existsByEmail(appUser.getEmail())) {
      appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
      userRepository.save(appUser);

      return jwtTokenProvider.generateTokenPair(appUser.getEmail(), appUser.getAppUserRoles());
    } else {
      throw new CustomException("Email is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  public void delete(String username) {
    userRepository.deleteByEmail(username);
  }

  public AppUser search(String username) {
    AppUser appUser = userRepository.findByEmail(username);
    if (appUser == null) {
      throw new CustomException("The user doesn't exist", HttpStatus.NOT_FOUND);
    }
    return appUser;
  }

  public AppUser whoami(HttpServletRequest req) {
    return userRepository.findByEmail(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
  }

  public ResponseEntity<?> refresh(RefreshTokenRequestDTO refreshTokenRequest) {
    return jwtTokenProvider.refreshTokenPair(refreshTokenRequest);
  }

}
