package datamax.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import datamax.exception.CustomException;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
    String token = jwtTokenProvider.resolveToken(httpServletRequest);
    try {
      if (token != null && jwtTokenProvider.validateToken(token)) {
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (CustomException ex) {
      SecurityContextHolder.clearContext();

      Map<String, Object> errorDetails = new HashMap<>();
      errorDetails.put("message", ex.getMessage());
      errorDetails.put("status", ex.getHttpStatus());

      httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
      httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

      ObjectMapper mapper = new ObjectMapper();
      String errorResponse =  mapper.writeValueAsString(errorDetails);
      httpServletResponse.getWriter().write(errorResponse);
      return;
    }

    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }

}
