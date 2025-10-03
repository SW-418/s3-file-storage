package samwells.io.s3uploader.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import samwells.io.s3uploader.dto.CreateUploadRequestDto;
import samwells.io.s3uploader.dto.UploadResponseDto;
import samwells.io.s3uploader.dto.UploadDataDto;
import samwells.io.s3uploader.model.MultipartUpload;
import samwells.io.s3uploader.service.ClientUploadService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v2/upload")
public class UploadControllerV2 {
    private final ClientUploadService clientUploadService;

    @PostMapping
    UploadResponseDto createUpload(@Valid @RequestBody CreateUploadRequestDto createUploadRequestDto) {
        MultipartUpload multipartUpload = clientUploadService.initiateUpload(
                createUploadRequestDto.fileName(),
                createUploadRequestDto.fileSizeInBytes()
        );

        return new UploadResponseDto(
                multipartUpload.id(),
                multipartUpload
                        .uploadParts()
                        .stream()
                        .map(uploadPart -> new UploadDataDto(
                                uploadPart.id(),
                                uploadPart.partNumber(),
                                uploadPart.url(),
                                uploadPart.partSizeInBytes()
                        ))
                        .toList()
        );
    }

    @DeleteMapping
    @RequestMapping("/{id}")
    ResponseEntity<Void> deleteUpload(@PathVariable Long id) {
        clientUploadService.abortUpload(id);

        return ResponseEntity.noContent().build();
    }
}
