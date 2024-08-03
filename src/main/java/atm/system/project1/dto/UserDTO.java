
package atm.system.project1.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserDTO {
    private Long id;
    private String accountNumber;
    private String pin;
    private BigDecimal balance;
}