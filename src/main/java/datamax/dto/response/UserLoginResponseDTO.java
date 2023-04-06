package datamax.dto.response;

import datamax.model.AppUserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserLoginResponseDTO {
  private String accessToken;
  private String type = "Bearer ";
  private String refreshToken;
  private List<AppUserRole> roles;

  public UserLoginResponseDTO(String accessToken, String refreshToken, List<AppUserRole> roles) {
    this.accessToken = type + accessToken;
    this.refreshToken = refreshToken;
    this.roles = roles;
  }
}
