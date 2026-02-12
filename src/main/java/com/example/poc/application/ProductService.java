package com.example.poc.application;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.poc.domain.Product;
import com.example.poc.domain.ProductRepository;
import com.example.poc.domain.vo.Money;
import com.example.poc.infrastructure.mapping.ProductMapper;
import com.example.poc.web.ProductDto;
import com.example.poc.web.ProductCreateDto;

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

        Product product = mapper.toDomain(dto);
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

        product.updatePrice(new Money(new BigDecimal(dto.price()), dto.currency() != null ? dto.currency() : Money.DEFAULT_CURRENCY));
        product.updateDescription(dto.description());
        if (dto.specifications() != null) {
            product.updateSpecifications(dto.specifications());
        }
        product.updateImages(dto.images());
        Product updated = repository.save(product);

        return mapper.toDto(updated);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
