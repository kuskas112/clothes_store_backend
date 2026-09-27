package com.example.clothesstore.textile;

import org.hibernate.internal.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextileRepository extends JpaRepository<Textile, Integer> {

    Optional<Textile> findByName(String name);

    boolean existsByName(String name);

    List<Textile> findAllByActiveTrue();

    List<Textile> findAllByWeaveTypeAndActiveTrue(WeaveType weaveType);

    Page<Textile> findAllByActiveTrue(Pageable pageable);

    List<Textile> findByNameContainingIgnoreCase(String fragment);
}