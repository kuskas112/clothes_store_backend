package com.example.clothesstore.color;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ColorPatchRequest(
        @Size(max = 20)
        String code,

        @Size(max = 50)
        String name,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "hex должен быть в формате #RRGGBB")
        String hex
) {
}
