package dev.hanginggoose.nestdi.demo.api;

import dev.hanginggoose.nestdi.demo.application.ProductService;
import dev.hanginggoose.nestdi.demo.domain.product.Product;
import dev.hanginggoose.nestdi.framework.annotations.Controller;
import dev.hanginggoose.nestdi.framework.annotations.InputMapping;

import java.util.Collection;
import java.util.UUID;

@Controller
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @InputMapping
    public Product addProduct(String name, double price, String brandId) {
        Product product = new Product(
                UUID.randomUUID(),
                name,
                price,
                UUID.fromString(brandId)
        );
        return service.AddProduct(product);
    }

    public Product getProduct(String id) {
        return service.getProduct(UUID.fromString(id));
    }

    @InputMapping("brandProducts")
    public Collection<Product> getProductsByBrand(String brandId) {
        return service.getProductsByBrand(UUID.fromString(brandId));
    }
}