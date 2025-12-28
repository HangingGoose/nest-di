package dev.hanginggoose.demo.infrastructure.brand;

import dev.hanginggoose.demo.domain.brand.Brand;
import dev.hanginggoose.demo.domain.brand.BrandRepository;
import dev.hanginggoose.framework.annotations.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class InMemoryBrandRepository implements BrandRepository {
    private final Map<UUID, Brand> brands;

    public InMemoryBrandRepository() {
        this.brands = new HashMap<>();
    }

    @Override
    public Brand createBrand(Brand brand) {
        brands.put(brand.id(), brand);
        return brand;
    }

    @Override
    public Brand readBrand(UUID id) {
        return brands.get(id);
    }
}