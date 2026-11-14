package com.edward.chat_system.features.file;

import com.edward.chat_system.infrastructure.storage.StorageService;
import com.edward.chat_system.shared.aop.annotation.SafeFilename;
import com.edward.chat_system.shared.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    FileService fileService;
    StorageService storageService;

    @PostMapping("/upload")
    ApiResponse<String> upload(@RequestParam MultipartFile file) {
        String url = fileService.upload(file);
        return ApiResponse.<String>builder().message("Upload successfully").result(url).build();
    }

    @GetMapping("/{filename:.+}")
    ResponseEntity<Resource> serve(@PathVariable @SafeFilename String filename) {
        Resource resource = storageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
