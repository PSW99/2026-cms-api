package com.malgn.contents.repository;

import com.malgn.contents.entity.Contents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContentsRepository extends JpaRepository<Contents, Long> {

    @Query("SELECT c FROM Contents c WHERE c.id = :id AND c.deleted = false")
    Optional<Contents> findById(@Param("id") Long id);

    @Query("SELECT c FROM Contents c WHERE c.deleted = false")
    Page<Contents> findAll(Pageable pageable);

    @Query("SELECT c FROM Contents c WHERE c.id = :id")
    Optional<Contents> findByIdIncludingDeleted(@Param("id") Long id);

    @Query("SELECT c FROM Contents c WHERE c.deleted = true")
    Page<Contents> findAllDeleted(Pageable pageable);

}
