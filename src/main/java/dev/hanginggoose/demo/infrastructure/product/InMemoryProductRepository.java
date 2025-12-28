package dev.hanginggoose.demo.infrastructure.product;

import dev.hanginggoose.demo.domain.product.Product;
import dev.hanginggoose.demo.domain.product.ProductRepository;
import dev.hanginggoose.framework.annotations.Repository;

import java.util.*;

@Repository
public class InMemoryProductRepository implements ProductRepository {
    private final Map<UUID, Product> products;

    public InMemoryProductRepository() {
        this.products = new HashMap<>();
    }

    @Override
    public Product createProduct(Product product) {
        return products.put(product.id(), product);
    }

    @Override
    public Product readProduct(UUID id) {
        return products.get(id);
    }

    @Override
    public Collection<Product> readProductsByBrand(UUID brandId) {
        return products.values().stream().filter(p -> p.brandId().equals(brandId)).toList();
    }
}