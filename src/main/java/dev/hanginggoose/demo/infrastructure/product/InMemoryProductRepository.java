package dev.hanginggoose.demo.infrastructure.product;

import dev.hanginggoose.demo.domain.product.Product;
import dev.hanginggoose.demo.domain.product.ProductRepository;
import dev.hanginggoose.framework.annotations.Logged;
import dev.hanginggoose.framework.annotations.Repository;
import dev.hanginggoose.framework.annotations.Timed;

import java.util.*;

@Repository
public class InMemoryProductRepository implements ProductRepository {
    private final Map<UUID, Product> products;

    public InMemoryProductRepository() {
        this.products = new HashMap<>();
    }

    @Override
    public Product createProduct(Product product) {
        products.put(product.id(), product);
        return product;
    }

    @Override
    public Product readProduct(UUID id) {
        return products.get(id);
    }

    @Override
    @Logged
    @Timed
    public Collection<Product> readProductsByBrand(UUID brandId) {
        try {
            // Simulate delay for demo
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return products.values().stream().filter(p -> p.brandId().equals(brandId)).toList();
    }
}