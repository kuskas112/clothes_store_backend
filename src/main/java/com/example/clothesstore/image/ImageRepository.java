package com.example.clothesstore.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByStorageKey(String storageKey);

    boolean existsByStorageKey(String storageKey);

    Optional<Image> findByChecksum(String checksum);

    List<Image> findByChecksumIn(Collection<String> checksums);
}
