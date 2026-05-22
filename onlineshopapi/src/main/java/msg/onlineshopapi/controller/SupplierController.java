package msg.onlineshopapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import msg.onlineshopapi.dto.SupplierDto;
import msg.onlineshopapi.dto.mapper.SupplierMapper;
import msg.onlineshopapi.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier management")
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierMapper supplierMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all suppliers", description = "Returns a list of all suppliers. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public List<SupplierDto> getAll() {
        return supplierService.findAll().stream()
                .map(supplierMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get supplier by ID", description = "Returns a single supplier by its ID. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Supplier found")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Supplier not found")
    public ResponseEntity<SupplierDto> getById(@Parameter(description = "Supplier ID") @PathVariable UUID id) {
        return supplierService.findById(id)
                .map(supplierMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a supplier", description = "Creates a new supplier. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Supplier created successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public SupplierDto create(@Valid @RequestBody SupplierDto dto) {
        return supplierMapper.toDto(supplierService.save(supplierMapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a supplier", description = "Updates an existing supplier. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Supplier updated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Supplier not found")
    public SupplierDto update(@Parameter(description = "Supplier ID") @PathVariable UUID id,
                              @Valid @RequestBody SupplierDto dto) {
        return supplierMapper.toDto(supplierService.update(id, supplierMapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a supplier", description = "Deletes a supplier. Requires ADMIN role.")
    @ApiResponse(responseCode = "204", description = "Supplier deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public ResponseEntity<Void> delete(@Parameter(description = "Supplier ID") @PathVariable UUID id) {
        supplierService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
