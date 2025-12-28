package dev.hanginggoose.demo.api;

import dev.hanginggoose.demo.application.ProductService;
import dev.hanginggoose.demo.domain.product.Product;
import dev.hanginggoose.framework.annotations.Controller;
import dev.hanginggoose.framework.annotations.InputMapping;
import dev.hanginggoose.framework.annotations.Logged;
import dev.hanginggoose.framework.annotations.Timed;

import java.util.Collection;
import java.util.UUID;

@Controller
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @InputMapping
    public Product addProduct(Product product) {
        return service.AddProduct(product);
    }

    public Product getProduct(UUID id) {
        return service.getProduct(id);
    }

    @InputMapping
    public Collection<Product> getProductsByBrand(UUID brandId) {
        return service.getProductsByBrand(brandId);
    }
}