package msg.onlineshopapi.unit.service;

import msg.onlineshopapi.exception.ResourceNotFoundException;
import msg.onlineshopapi.model.Supplier;
import msg.onlineshopapi.repository.SupplierRepository;
import msg.onlineshopapi.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private final UUID supplierId = UUID.randomUUID();

    @Test
    void findAll_returnsAllSuppliers() {
        Supplier s1 = Supplier.builder().id(supplierId).name("SupA").build();
        Supplier s2 = Supplier.builder().id(UUID.randomUUID()).name("SupB").build();
        when(supplierRepository.findAll()).thenReturn(List.of(s1, s2));

        List<Supplier> result = supplierService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findById_returnsSupplier_whenExists() {
        Supplier supplier = Supplier.builder().id(supplierId).name("SupA").build();
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        Optional<Supplier> result = supplierService.findById(supplierId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("SupA");
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        Optional<Supplier> result = supplierService.findById(supplierId);

        assertThat(result).isEmpty();
    }

    @Test
    void save_persistsAndReturnsSupplier() {
        Supplier input = Supplier.builder().name("New Supplier").build();
        Supplier saved = Supplier.builder().id(supplierId).name("New Supplier").build();
        when(supplierRepository.save(input)).thenReturn(saved);

        Supplier result = supplierService.save(input);

        assertThat(result.getId()).isEqualTo(supplierId);
        verify(supplierRepository).save(input);
    }

    @Test
    void update_updatesFields_whenSupplierExists() {
        Supplier existing = Supplier.builder().id(supplierId).name("Old").email("old@test.com").build();
        Supplier update = Supplier.builder().name("New").email("new@test.com").phone("123").address("Addr").build();
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Supplier result = supplierService.update(supplierId, update);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getPhone()).isEqualTo("123");
        assertThat(result.getAddress()).isEqualTo("Addr");
    }

    @Test
    void update_throwsResourceNotFoundException_whenNotExists() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.update(supplierId, Supplier.builder().name("X").build()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(supplierId.toString());
    }

    @Test
    void deleteById_delegatesToRepository() {
        doNothing().when(supplierRepository).deleteById(supplierId);

        supplierService.deleteById(supplierId);

        verify(supplierRepository).deleteById(supplierId);
    }
}
