package com.example.poc.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.poc.application.CustomerService;

import java.net.URI;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerDto> create(@RequestBody CustomerDto dto) {
        CustomerDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/customers/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getById(@PathVariable String id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
