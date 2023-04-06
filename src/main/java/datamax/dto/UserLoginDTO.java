package datamax.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLoginDTO {
  @ApiModelProperty
  private String email;
  @ApiModelProperty
  private String password;
}
