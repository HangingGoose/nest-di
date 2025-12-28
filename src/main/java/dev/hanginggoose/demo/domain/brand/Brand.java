package dev.hanginggoose.demo.domain.brand;

import java.util.UUID;

public record Brand(UUID id, String name, String email, String password) {
}