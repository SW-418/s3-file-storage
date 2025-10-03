package samwells.io.s3uploader.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import samwells.io.s3uploader.dto.CreateUploadRequestDto;
import samwells.io.s3uploader.dto.CreateUploadResponseDto;
import samwells.io.s3uploader.dto.UploadDataDto;
import samwells.io.s3uploader.model.MultipartUpload;
import samwells.io.s3uploader.service.ClientUploadService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v2/upload")
public class UploadControllerV2 {
    private final ClientUploadService clientUploadService;

    @PostMapping
    CreateUploadResponseDto createUpload(@Valid @RequestBody CreateUploadRequestDto createUploadRequestDto) {
        MultipartUpload multipartUpload = clientUploadService.initiateUpload(
                createUploadRequestDto.fileName(),
                createUploadRequestDto.fileSizeInBytes()
        );

        return new CreateUploadResponseDto(
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
}
