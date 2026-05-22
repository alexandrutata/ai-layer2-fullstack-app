package msg.onlineshopapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {

    private String name;
    private String description;
    private BigDecimal price;
    private Double weight;
    private String imageUrl;
    @NotNull
    private UUID categoryId;
    @NotNull
    private UUID supplierId;
}
