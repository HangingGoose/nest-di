package dev.hanginggoose.demo.application;

import dev.hanginggoose.demo.domain.brand.Brand;
import dev.hanginggoose.demo.domain.brand.BrandRepository;
import dev.hanginggoose.framework.annotations.Service;

import java.util.UUID;

@Service
public class BrandService {
    private final BrandRepository repository;

    public BrandService(BrandRepository repository) {
        this.repository = repository;
    }

    public Brand addBrand(Brand brand) {
        return repository.createBrand(brand);
    }

    public Brand getBrand(UUID id) {
        return repository.readBrand(id);
    }
}