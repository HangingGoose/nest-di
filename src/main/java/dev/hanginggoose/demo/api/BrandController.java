package dev.hanginggoose.demo.api;

import dev.hanginggoose.demo.application.BrandService;
import dev.hanginggoose.demo.domain.brand.Brand;
import dev.hanginggoose.framework.annotations.Controller;
import dev.hanginggoose.framework.annotations.InputMapping;
import dev.hanginggoose.framework.annotations.Logged;

import java.util.UUID;

@Controller
public class BrandController {
    private final BrandService service;

    public BrandController(BrandService service){
        this.service = service;
    }

    @InputMapping
    public Brand addBrand(String name, String email, String password) {
        Brand brand = new Brand(
                UUID.randomUUID(),
                name,
                email,
                password
        );
        return service.addBrand(brand);
    }

    @InputMapping
    @Logged
    public Brand getBrand(String id) {
        return service.getBrand(UUID.fromString(id));
    }
}