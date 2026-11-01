package com.example.clothesstore.color;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ColorRequest(
        @NotBlank @Size(max = 20)
        String code,

        @NotBlank @Size(max = 50)
        String name,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "hex должен быть в формате #RRGGBB")
        String hex
) {
}
