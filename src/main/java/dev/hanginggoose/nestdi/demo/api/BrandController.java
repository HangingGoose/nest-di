package dev.hanginggoose.nestdi.demo.api;

import dev.hanginggoose.nestdi.demo.application.BrandService;
import dev.hanginggoose.nestdi.demo.domain.brand.Brand;
import dev.hanginggoose.nestdi.framework.annotations.Controller;
import dev.hanginggoose.nestdi.framework.annotations.InputMapping;

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
    public Brand getBrand(String id) {
        return service.getBrand(UUID.fromString(id));
    }
}