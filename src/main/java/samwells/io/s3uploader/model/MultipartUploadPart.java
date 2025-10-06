package samwells.io.s3uploader.model;

import samwells.io.s3uploader.entity.UploadPart;

public record MultipartUploadPart(
        Long id,
        int partNumber,
        String url,
        String completionTag,
        long partSizeInBytes
) {
    public MultipartUploadPart(int partNumber, String url, long partSizeInBytes) {
        this (
                null,
                partNumber,
                url,
                null,
                partSizeInBytes
        );
    }

    public MultipartUploadPart(UploadPart uploadPart) {
        this (
                uploadPart.getId(),
                uploadPart.getPartNumber(),
                uploadPart.getUrl(),
                uploadPart.getCompletionTag(),
                uploadPart.getPartSizeInBytes()
        );
    }
}
