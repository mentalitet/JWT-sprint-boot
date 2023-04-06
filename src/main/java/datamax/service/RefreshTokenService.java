package datamax.service;

import datamax.exception.CustomException;
import datamax.model.RefreshToken;
import datamax.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  public RefreshToken findByRefreshToken (String token) {
    return refreshTokenRepository.findByRefreshToken(token)
        .orElseThrow(() -> new CustomException("Refresh token is not in database", HttpStatus.UNAUTHORIZED));
  }

  public RefreshToken verifyExpiration (RefreshToken token) {
    if (token.getExpiryDate().compareTo(new Date()) < 0) {
      refreshTokenRepository.delete(token);
      throw new CustomException("Refresh token was expired. Please make a new signin request", HttpStatus.UNAUTHORIZED);
    }
    return token;
  }

  public void save(RefreshToken token) {
    refreshTokenRepository.save(token);
  }
}
