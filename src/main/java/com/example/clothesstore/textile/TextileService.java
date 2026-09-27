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

    // ===== READ =====

    @Transactional(readOnly = true)
    public Page<Textile> getAll(Pageable pageable) {
        return textileRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Textile getOne(Integer id) {
        return findOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Textile> getMany(List<Integer> ids) {
        return textileRepository.findAllById(ids);
    }

    // ===== CREATE =====

    @Transactional
    public Textile create(Textile textile) {
        textile.setId(null); // защита от подсунутого id
        textile.setActive(true);
        return textileRepository.save(textile);
    }

    // ===== UPDATE (PATCH) =====

    @Transactional
    public Textile patch(Integer id, TextilePatchDto patch, TextileMapper mapper) {
        // patch всё ещё требует DTO — иначе не отличить «поле не пришло»
        Textile textile = findOrThrow(id);
        mapper.updateEntity(patch, textile);
        return textileRepository.save(textile);
    }

    @Transactional
    public List<Integer> patchMany(List<Integer> ids, TextilePatchDto patch, TextileMapper mapper) {
        List<Textile> textiles = textileRepository.findAllById(ids);
        for (Textile textile : textiles) {
            mapper.updateEntity(patch, textile);
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