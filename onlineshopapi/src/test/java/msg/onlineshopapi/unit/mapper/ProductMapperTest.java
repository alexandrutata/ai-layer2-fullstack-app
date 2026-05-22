package msg.onlineshopapi.unit.mapper;

import msg.onlineshopapi.dto.ProductCategoryDto;
import msg.onlineshopapi.dto.ProductRequestDto;
import msg.onlineshopapi.dto.ProductResponseDto;
import msg.onlineshopapi.dto.SupplierDto;
import msg.onlineshopapi.dto.mapper.ProductCategoryMapper;
import msg.onlineshopapi.dto.mapper.ProductMapper;
import msg.onlineshopapi.dto.mapper.SupplierMapper;
import msg.onlineshopapi.exception.ResourceNotFoundException;
import msg.onlineshopapi.model.Product;
import msg.onlineshopapi.model.ProductCategory;
import msg.onlineshopapi.model.Supplier;
import msg.onlineshopapi.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ProductMapper productMapper;

    private final UUID productId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID supplierId = UUID.randomUUID();

    @Test
    void toDto_mapsAllFields_includingSupplier() {
        ProductCategory category = ProductCategory.builder().id(categoryId).name("Electronics").build();
        Supplier supplier = Supplier.builder().id(supplierId).name("SupA").build();
        Product product = Product.builder()
                .id(productId).name("Laptop").description("Desc")
                .price(BigDecimal.valueOf(999)).weight(1.5)
                .imageUrl("http://img.url")
                .category(category).supplier(supplier)
                .build();

        ProductCategoryDto categoryDto = ProductCategoryDto.builder().id(categoryId).name("Electronics").build();
        SupplierDto supplierDto = SupplierDto.builder().id(supplierId).name("SupA").build();
        when(productCategoryMapper.toDto(category)).thenReturn(categoryDto);
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDto);

        ProductResponseDto result = productMapper.toDto(product);

        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getCategory()).isEqualTo(categoryDto);
        assertThat(result.getSupplier()).isEqualTo(supplierDto);
    }

    @Test
    void toDto_returnsNullSupplier_whenProductHasNoSupplier() {
        ProductCategory category = ProductCategory.builder().id(categoryId).name("Electronics").build();
        Product product = Product.builder()
                .id(productId).name("Laptop")
                .price(BigDecimal.valueOf(999))
                .category(category).supplier(null)
                .build();

        when(productCategoryMapper.toDto(category))
                .thenReturn(ProductCategoryDto.builder().id(categoryId).name("Electronics").build());

        ProductResponseDto result = productMapper.toDto(product);

        assertThat(result.getSupplier()).isNull();
    }

    @Test
    void toEntity_resolvesSupplierFromRepository() {
        Supplier supplier = Supplier.builder().id(supplierId).name("SupA").build();
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        ProductRequestDto dto = ProductRequestDto.builder()
                .name("Laptop").price(BigDecimal.valueOf(999))
                .categoryId(categoryId).supplierId(supplierId)
                .build();

        Product result = productMapper.toEntity(dto);

        assertThat(result.getSupplier()).isEqualTo(supplier);
        assertThat(result.getSupplier().getId()).isEqualTo(supplierId);
    }

    @Test
    void toEntity_throwsResourceNotFoundException_whenSupplierNotFound() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        ProductRequestDto dto = ProductRequestDto.builder()
                .name("Laptop").price(BigDecimal.valueOf(999))
                .categoryId(categoryId).supplierId(supplierId)
                .build();

        assertThatThrownBy(() -> productMapper.toEntity(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(supplierId.toString());
    }
}
