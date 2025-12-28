package dev.hanginggoose.nestdi.demo.domain.product;

import java.util.Collection;
import java.util.UUID;

public interface ProductRepository {
    Product createProduct(Product product);
    Product readProduct(UUID id);
    Collection<Product> readProductsByBrand(UUID brandId);
}