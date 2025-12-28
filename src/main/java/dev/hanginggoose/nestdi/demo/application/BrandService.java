package dev.hanginggoose.nestdi.demo.application;

import dev.hanginggoose.nestdi.demo.domain.brand.Brand;
import dev.hanginggoose.nestdi.demo.domain.brand.BrandRepository;
import dev.hanginggoose.nestdi.framework.annotations.Autowired;
import dev.hanginggoose.nestdi.framework.annotations.Service;

import java.util.UUID;

@Service
public class BrandService {
    private final BrandRepository repository;

    public BrandService() {
        //no arg constructor to showcase @Autowired functionality
        this.repository = null;
    }

    @Autowired
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