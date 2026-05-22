package msg.onlineshopapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDto {

    private UUID id;
    @NotBlank
    private String name;
    private String email;
    private String phone;
    private String address;
}
