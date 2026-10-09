package com.example.clothesstore.color;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorRepository colorRepository;

    private final ObjectMapper objectMapper;

    @GetMapping
    public List<Color> getAll() {
        return colorRepository.findAll();
    }

    @GetMapping("/{id}")
    public Color getOne(@PathVariable Integer id) {
        Optional<Color> colorOptional = colorRepository.findById(id);
        return colorOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @GetMapping("/by-ids")
    public List<Color> getMany(@RequestParam List<Integer> ids) {
        return colorRepository.findAllById(ids);
    }

    @PostMapping
    public Color create(@RequestBody Color color) {
        return colorRepository.save(color);
    }

    @PatchMapping("/{id}")
    public Color patch(@PathVariable Integer id, @RequestBody JsonNode patchNode) throws IOException {
        Color color = colorRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        objectMapper.readerForUpdating(color).readValue(patchNode);

        return colorRepository.save(color);
    }

    @DeleteMapping("/{id}")
    public Color delete(@PathVariable Integer id) {
        Color color = colorRepository.findById(id).orElse(null);
        if (color != null) {
            colorRepository.delete(color);
        }
        return color;
    }

    @DeleteMapping
    public void deleteMany(@RequestParam List<Integer> ids) {
        colorRepository.deleteAllById(ids);
    }
}
