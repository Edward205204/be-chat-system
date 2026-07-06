package com.edward.chat_system.features.file;

import com.edward.chat_system.infrastructure.storage.StorageService;
import com.edward.chat_system.shared.utils.DateTimeUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    StorageService storageService;
    UploadedFileRepository uploadedFileRepository;

    public String upload(MultipartFile file) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        storageService.store(file, filename);
        String url = "/files/" + filename;

        uploadedFileRepository.save(
                UploadedFile.builder()
                        .url(url)
                        .filename(filename)
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .claimed(false)
                        .build());

        return url;
    }

    public void claimFile(String url) {
        uploadedFileRepository
                .findByUrl(url)
                .ifPresent(
                        f -> {
                            f.setClaimed(true);
                            uploadedFileRepository.save(f);
                        });
    }

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void cleanOrphanFiles() {

        LocalDateTime cutoff = DateTimeUtils.now().minusMinutes(30);

        List<UploadedFile> orphans =
                uploadedFileRepository.findByClaimedFalseAndUploadedAtBefore(cutoff);

        List<String> deletedIds = new ArrayList<>();

        for (UploadedFile file : orphans) {
            try {
                storageService.delete(file.getFilename());
                deletedIds.add(file.getId());
            } catch (Exception e) {
                log.warn("Cannot delete {}", file.getFilename(), e);
            }
        }

        if (!deletedIds.isEmpty()) {
            uploadedFileRepository.deleteAllByIdInBatch(deletedIds);
        }
    }
}
