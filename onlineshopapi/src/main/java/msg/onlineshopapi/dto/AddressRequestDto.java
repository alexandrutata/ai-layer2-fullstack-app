package msg.onlineshopapi.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@JsonDeserialize(builder = AddressRequestDto.AddressRequestDtoBuilder.class)
public class AddressRequestDto {

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    @NotBlank
    private String county;

    @NotBlank
    private String streetAddress;
}
