package samwells.io.s3uploader.model;

public record UploadPart(
        Long id,
        int partNumber,
        String url,
        String completionTag,
        long partSizeInBytes
) {
    public UploadPart(int partNumber, String url, long partSizeInBytes) {
        this (
                null,
                partNumber,
                url,
                null,
                partSizeInBytes
        );
    }
}
