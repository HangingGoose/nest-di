package dev.hanginggoose.demo.application;

import dev.hanginggoose.demo.domain.product.Product;
import dev.hanginggoose.demo.domain.product.ProductRepository;
import dev.hanginggoose.framework.annotations.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product AddProduct(Product product) {
        return repository.createProduct(product);
    }

    public Product getProduct(UUID id) {
        return repository.readProduct(id);
    }

    public Collection<Product> getProductsByBrand(UUID brandId) {
        return repository.readProductsByBrand(brandId);
    }
}