package com.edward.chat_system.features.file;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
        name = "uploaded_files",
        indexes = {@Index(name = "idx_uploadedfile_claimed", columnList = "claimed, uploadedAt")},
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_uploadedfile_url",
                    columnNames = {"url"}),
            @UniqueConstraint(
                    name = "uc_uploadedfile_filename",
                    columnNames = {"filename"})
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String url;
    String filename;
    String contentType;
    long size;

    boolean claimed;

    @CreationTimestamp LocalDateTime uploadedAt;
}
