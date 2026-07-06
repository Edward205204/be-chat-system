package com.edward.chat_system.features.file;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, String> {
    List<UploadedFile> findByClaimedFalseAndUploadedAtBefore(LocalDateTime cutoff);

    Optional<UploadedFile> findByUrl(String url);
}
