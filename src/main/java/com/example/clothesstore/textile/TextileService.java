package com.example.clothesstore.textile;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TextileService {

    private final TextileRepository textileRepository;
    private final TextileMapper textileMapper;

    // ===== READ =====

    @Transactional(readOnly = true)
    public Page<TextileDto> getAll(Pageable pageable) {
        return textileRepository.findAll(pageable)
                .map(textileMapper::toDto);
    }

    @Transactional(readOnly = true)
    public TextileDto getOne(Integer id) {
        return textileMapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<TextileDto> getMany(List<Integer> ids) {
        return textileMapper.toDto(textileRepository.findAllById(ids));
    }

    // ===== CREATE =====

    @Transactional
    public TextileDto create(TextileDto dto) {
        Textile textile = textileMapper.toEntity(dto);
        textile.setActive(true); // серверное поле
        Textile saved = textileRepository.save(textile);
        return textileMapper.toDto(saved);
    }

    // ===== UPDATE (PATCH) =====

    @Transactional
    public TextileDto patch(Integer id, TextilePatchDto patch) {
        Textile textile = findOrThrow(id);
        textileMapper.updateEntity(patch, textile);
        Textile saved = textileRepository.save(textile);
        return textileMapper.toDto(saved);
    }

    @Transactional
    public List<Integer> patchMany(List<Integer> ids, TextilePatchDto patch) {
        List<Textile> textiles = textileRepository.findAllById(ids);

        for (Textile textile : textiles) {
            textileMapper.updateEntity(patch, textile);
        }

        return textileRepository.saveAll(textiles).stream()
                .map(Textile::getId)
                .toList();
    }

    // ===== DELETE =====

    @Transactional
    public void delete(Integer id) {
        Textile textile = findOrThrow(id);
        textileRepository.delete(textile);
    }

    @Transactional
    public void deleteMany(List<Integer> ids) {
        textileRepository.deleteAllById(ids);
    }

    // ===== HELPERS =====

    private Textile findOrThrow(Integer id) {
        return textileRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Textile with id `%s` not found".formatted(id)));
    }
}