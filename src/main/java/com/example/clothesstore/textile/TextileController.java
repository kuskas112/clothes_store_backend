package com.example.clothesstore.textile;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/textiles")
@RequiredArgsConstructor
public class TextileController {

    private final TextileService textileService;

    @GetMapping
    public PagedModel<TextileDto> getAll(Pageable pageable) {
        Page<TextileDto> page = textileService.getAll(pageable);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public TextileDto getOne(@PathVariable Integer id) {
        return textileService.getOne(id);
    }

    @GetMapping("/by-ids")
    public List<TextileDto> getMany(@RequestParam List<Integer> ids) {
        return textileService.getMany(ids);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TextileDto create(@RequestBody TextileDto dto) {
        return textileService.create(dto);
    }

    @PatchMapping("/{id}")
    public TextileDto patch(@PathVariable Integer id,
                            @RequestBody TextilePatchDto patch) {
        return textileService.patch(id, patch);
    }

    @PatchMapping
    public List<Integer> patchMany(@RequestParam List<Integer> ids,
                                   @RequestBody TextilePatchDto patch) {
        return textileService.patchMany(ids, patch);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        textileService.delete(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMany(@RequestParam List<Integer> ids) {
        textileService.deleteMany(ids);
    }
}