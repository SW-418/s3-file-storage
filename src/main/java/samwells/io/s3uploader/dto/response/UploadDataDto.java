package samwells.io.s3uploader.dto.response;

import samwells.io.s3uploader.model.MultipartUploadPart;

public record UploadDataDto(
        long id,
        int partNumber,
        String url,
        String completionTag,
        long partSizeInBytes
) {
    public UploadDataDto(
            long id,
            int partNumber,
            String url,
            long partSizeInBytes
    ) {
        this (
                id,
                partNumber,
                url,
                null,
                partSizeInBytes
        );
    }

    public UploadDataDto(MultipartUploadPart part) {
        this (
                part.id(),
                part.partNumber(),
                part.url(),
                part.completionTag(),
                part.partSizeInBytes()
        );
    }
}
