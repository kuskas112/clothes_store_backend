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
    private final TextileMapper textileMapper;

    @GetMapping
    public PagedModel<TextileDto> getAll(Pageable pageable) {
        Page<TextileDto> page = textileService.getAll(pageable)
                .map(textileMapper::toDto);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public TextileDto getOne(@PathVariable Integer id) {
        return textileMapper.toDto(textileService.getOne(id));
    }

    @GetMapping("/by-ids")
    public List<TextileDto> getMany(@RequestParam List<Integer> ids) {
        return textileMapper.toDto(textileService.getMany(ids));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TextileDto create(@RequestBody TextileDto dto) {
        Textile textile = textileMapper.toEntity(dto);
        Textile saved = textileService.create(textile);
        return textileMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public TextileDto patch(@PathVariable Integer id,
                            @RequestBody TextilePatchDto patch) {
        Textile saved = textileService.patch(id, patch, textileMapper);
        return textileMapper.toDto(saved);
    }

    @PatchMapping
    public List<Integer> patchMany(@RequestParam List<Integer> ids,
                                   @RequestBody TextilePatchDto patch) {
        return textileService.patchMany(ids, patch, textileMapper);
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