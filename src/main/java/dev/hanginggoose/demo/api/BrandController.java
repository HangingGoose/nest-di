package dev.hanginggoose.demo.api;

import dev.hanginggoose.demo.application.BrandService;
import dev.hanginggoose.demo.domain.brand.Brand;
import dev.hanginggoose.framework.annotations.Controller;
import dev.hanginggoose.framework.annotations.InputMapping;

import java.util.UUID;

@Controller
public class BrandController {
    private final BrandService service;

    public BrandController(BrandService service){
        this.service = service;
    }

    @InputMapping
    public Brand addBrand(Brand brand) {
        return service.addBrand(brand);
    }

    @InputMapping
    public Brand getBrand(UUID id) {
        return service.getBrand(id);
    }
}