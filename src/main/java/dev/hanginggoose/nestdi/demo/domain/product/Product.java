package dev.hanginggoose.nestdi.demo.domain.product;

import java.util.UUID;

public record Product(UUID id, String name, double price, UUID brandId) {
}