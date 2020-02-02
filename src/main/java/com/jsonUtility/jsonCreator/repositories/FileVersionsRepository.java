package com.jsonUtility.jsonCreator.repositories;

import com.jsonUtility.jsonCreator.model.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FileVersionsRepository extends JpaRepository<FileVersion,Long> {
    FileVersion getById(Long id);

    Optional<FileVersion> findByFileName(String filename);

    @Modifying
    @Query("delete from FileVersion f where f.fileName=:fileName")
    void deleteByFileName(@Param("fileName") String fileName);
}
