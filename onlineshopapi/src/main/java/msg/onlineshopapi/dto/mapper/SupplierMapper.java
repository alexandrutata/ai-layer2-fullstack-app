package msg.onlineshopapi.dto.mapper;

import msg.onlineshopapi.dto.SupplierDto;
import msg.onlineshopapi.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierDto toDto(Supplier supplier) {
        return SupplierDto.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .build();
    }

}
