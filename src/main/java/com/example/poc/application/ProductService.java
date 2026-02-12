package com.example.poc.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.poc.domain.Product;
import com.example.poc.domain.ProductRepository;
import com.example.poc.domain.vo.Money;
import com.example.poc.domain.vo.ProductImage;
import com.example.poc.infrastructure.mapping.ProductMapper;
import com.example.poc.web.ProductCreateDto;
import com.example.poc.web.ProductDto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"}, justification = "Referências finais injetadas via construtor são intencionais; não expomos coleções mutáveis diretamente aqui")
@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ProductDto create(ProductCreateDto dto) {
        if (repository.existsBySku(dto.sku())) {
            throw new IllegalArgumentException("Product with SKU " + dto.sku() + " already exists");
        }

        // DTO já normaliza nulls para Map.of()/List.of(); fazemos apenas cópias defensivas
        Map<String, Object> safeSpecifications = Map.copyOf(dto.specifications());
        List<ProductImage> safeImages = List.copyOf(dto.images());

        var dtoSafe = new ProductCreateDto(
            dto.sku(), dto.name(), dto.description(), dto.category(), dto.price(), dto.currency(), dto.initialStock(),
            safeSpecifications, safeImages
        );

        Product product = mapper.toDomain(dtoSafe);
        Product saved = repository.save(product);

        return mapper.toDto(saved);
    }

    public Optional<ProductDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public Optional<ProductDto> findBySku(String sku) {
        return repository.findBySku(sku).map(mapper::toDto);
    }

    public Page<ProductDto> findByCategory(String category, Pageable pageable) {
        return repository.findByCategory(category, pageable).map(mapper::toDto);
    }

    public Page<ProductDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    public ProductDto update(String id, ProductCreateDto dto) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        if (!product.getSku().equals(dto.sku()) && repository.existsBySku(dto.sku())) {
            throw new IllegalArgumentException("Product with SKU " + dto.sku() + " already exists");
        }

        // DTO já normatiza nulls; cópias defensivas
        Map<String, Object> safeSpecifications = Map.copyOf(dto.specifications());
        List<ProductImage> safeImages = List.copyOf(dto.images());

        product.updatePrice(new Money(new BigDecimal(dto.price()), dto.currency() != null ? dto.currency() : Money.DEFAULT_CURRENCY));
        product.updateDescription(dto.description());
        if (!safeSpecifications.isEmpty()) {
            product.updateSpecifications(safeSpecifications);
        }
        product.updateImages(safeImages);
        Product updated = repository.save(product);

        return mapper.toDto(updated);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
