package msg.onlineshopapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import msg.onlineshopapi.config.TestSecurityConfig;
import msg.onlineshopapi.dto.SupplierDto;
import msg.onlineshopapi.dto.mapper.SupplierMapper;
import msg.onlineshopapi.exception.ResourceNotFoundException;
import msg.onlineshopapi.model.Supplier;
import msg.onlineshopapi.security.JwtService;
import msg.onlineshopapi.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierController.class)
@Import(TestSecurityConfig.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private SupplierMapper supplierMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final UUID supplierId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_returnsSuppliers_whenAdmin() throws Exception {
        Supplier supplier = Supplier.builder().id(supplierId).name("TechSupply Co.").build();
        SupplierDto dto = supplierDto(supplierId, "TechSupply Co.");

        when(supplierService.findAll()).thenReturn(List.of(supplier));
        when(supplierMapper.toDto(supplier)).thenReturn(dto);

        mockMvc.perform(get("/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(supplierId.toString()))
                .andExpect(jsonPath("$[0].name").value("TechSupply Co."));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAll_returns403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/suppliers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_returnsSupplier_whenFound() throws Exception {
        Supplier supplier = Supplier.builder().id(supplierId).name("TechSupply Co.").build();
        SupplierDto dto = supplierDto(supplierId, "TechSupply Co.");

        when(supplierService.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toDto(supplier)).thenReturn(dto);

        mockMvc.perform(get("/suppliers/{id}", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(supplierId.toString()))
                .andExpect(jsonPath("$.name").value("TechSupply Co."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_returns404_whenNotFound() throws Exception {
        when(supplierService.findById(supplierId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/suppliers/{id}", supplierId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getById_returns403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/suppliers/{id}", supplierId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsSupplier_whenAdmin() throws Exception {
        SupplierDto request = supplierDto(null, "New Supplier");
        Supplier entity = Supplier.builder().name("New Supplier").build();
        Supplier saved = Supplier.builder().id(supplierId).name("New Supplier").build();
        SupplierDto dto = supplierDto(supplierId, "New Supplier");

        when(supplierMapper.toEntity(any(SupplierDto.class))).thenReturn(entity);
        when(supplierService.save(entity)).thenReturn(saved);
        when(supplierMapper.toDto(saved)).thenReturn(dto);

        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(supplierId.toString()))
                .andExpect(jsonPath("$.name").value("New Supplier"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_returns403_whenNotAdmin() throws Exception {
        SupplierDto request = supplierDto(null, "New Supplier");

        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_returnsSupplier_whenAdmin() throws Exception {
        SupplierDto request = supplierDto(supplierId, "Updated Supplier");
        Supplier entity = Supplier.builder().name("Updated Supplier").build();
        Supplier updated = Supplier.builder().id(supplierId).name("Updated Supplier").build();
        SupplierDto dto = supplierDto(supplierId, "Updated Supplier");

        when(supplierMapper.toEntity(any(SupplierDto.class))).thenReturn(entity);
        when(supplierService.update(eq(supplierId), eq(entity))).thenReturn(updated);
        when(supplierMapper.toDto(updated)).thenReturn(dto);

        mockMvc.perform(put("/suppliers/{id}", supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Supplier"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_returns404_whenNotFound() throws Exception {
        SupplierDto request = supplierDto(supplierId, "Updated Supplier");
        Supplier entity = Supplier.builder().name("Updated Supplier").build();

        when(supplierMapper.toEntity(any(SupplierDto.class))).thenReturn(entity);
        when(supplierService.update(eq(supplierId), eq(entity)))
                .thenThrow(new ResourceNotFoundException("Supplier not found with id: " + supplierId));

        mockMvc.perform(put("/suppliers/{id}", supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Supplier not found with id: " + supplierId));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void update_returns403_whenNotAdmin() throws Exception {
        SupplierDto request = supplierDto(supplierId, "Updated Supplier");

        mockMvc.perform(put("/suppliers/{id}", supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returns204_whenAdmin() throws Exception {
        doNothing().when(supplierService).deleteById(supplierId);

        mockMvc.perform(delete("/suppliers/{id}", supplierId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void delete_returns403_whenNotAdmin() throws Exception {
        mockMvc.perform(delete("/suppliers/{id}", supplierId))
                .andExpect(status().isForbidden());
    }

    private SupplierDto supplierDto(UUID id, String name) {
        return SupplierDto.builder()
                .id(id)
                .name(name)
                .email("contact@supplier.com")
                .phone("+1-800-123-4567")
                .address("123 Main St")
                .build();
    }
}
