package datamax.dto.response;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import datamax.model.AppUserRole;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponseDTO {
  @ApiModelProperty(position = 0)
  private Integer id;
  @ApiModelProperty(position = 2)
  private String email;
  @ApiModelProperty(position = 3)
  List<AppUserRole> appUserRoles;

}
