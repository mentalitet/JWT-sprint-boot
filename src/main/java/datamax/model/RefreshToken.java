package datamax.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data // Create getters and setters
@NoArgsConstructor
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "app_user_id")
  private AppUser appUser;

  @Column(nullable = false, unique = true)
  private String refreshToken;

  @Column(nullable = false)
  private Date expiryDate;

}