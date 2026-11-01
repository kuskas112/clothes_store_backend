package com.example.clothesstore.color;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorRepository colorRepository;

    @GetMapping
    public List<ColorDto> getAll() {
        return colorRepository.findAll().stream().map(ColorController::toDto).toList();
    }

    @GetMapping("/{id}")
    public ColorDto getOne(@PathVariable Integer id) {
        return toDto(findOrThrow(id));
    }

    @GetMapping("/by-ids")
    public List<ColorDto> getMany(
            @RequestParam @Size(max = 100) List<Integer> ids) {
        return colorRepository.findAllById(ids).stream().map(ColorController::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ColorDto create(@RequestBody @Valid ColorRequest request) {
        Color color = new Color();
        color.setCode(request.code());
        color.setName(request.name());
        color.setHex(request.hex());
        return toDto(colorRepository.save(color));
    }

    @PatchMapping("/{id}")
    public ColorDto patch(@PathVariable Integer id,
                          @RequestBody @Valid ColorPatchRequest request) {
        Color color = findOrThrow(id);

        if (request.code() != null) {
            color.setCode(request.code());
        }
        if (request.name() != null) {
            color.setName(request.name());
        }
        if (request.hex() != null) {
            color.setHex(request.hex());
        }

        return toDto(colorRepository.save(color));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Color color = colorRepository.findById(id).orElse(null);
        if (color != null) {
            colorRepository.delete(color);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMany(
            @RequestParam @Size(max = 100) List<Integer> ids) {
        colorRepository.deleteAllById(ids);
    }

    private Color findOrThrow(Integer id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Color with id `%s` not found".formatted(id)));
    }

    private static ColorDto toDto(Color color) {
        return new ColorDto(color.getId(), color.getCode(), color.getName(), color.getHex());
    }
}
