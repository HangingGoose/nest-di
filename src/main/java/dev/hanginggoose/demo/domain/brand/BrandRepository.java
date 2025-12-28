package dev.hanginggoose.demo.domain.brand;

import java.util.UUID;

public interface BrandRepository {
    Brand createBrand(Brand brand);
    Brand readBrand(UUID id);
}