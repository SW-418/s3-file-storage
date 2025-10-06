package samwells.io.s3uploader.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import samwells.io.s3uploader.dto.request.CreateUploadRequestDto;
import samwells.io.s3uploader.dto.request.UpdateUploadPartRequest;
import samwells.io.s3uploader.dto.response.UploadResponseDto;
import samwells.io.s3uploader.dto.response.UploadDataDto;
import samwells.io.s3uploader.model.MultipartUpload;
import samwells.io.s3uploader.model.MultipartUploadPart;
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
                        .multipartUploadParts()
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

    @PatchMapping
    @RequestMapping("/{uploadId}/upload-part/{uploadPartId}")
    UploadDataDto updateUploadPart(
            @PathVariable Long uploadId,
            @PathVariable Long uploadPartId,
            @RequestBody @Valid UpdateUploadPartRequest request
    ) {
        // In a real project this should be more generic and extendable ofc
        MultipartUploadPart uploadPart = clientUploadService.completeUploadPart(
                uploadId,
                uploadPartId,
                request.completionTag()
        );

        return new UploadDataDto(uploadPart);
    }

    @DeleteMapping
    @RequestMapping("/{id}")
    ResponseEntity<Void> deleteUpload(@PathVariable Long id) {
        clientUploadService.abortUpload(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    // Would have a body here but omitting now to reduce complexity
    ResponseEntity<Void> completeUpload(@PathVariable Long id) {
        clientUploadService.completeUpload(id);

        return ResponseEntity.noContent().build();
    }
}
