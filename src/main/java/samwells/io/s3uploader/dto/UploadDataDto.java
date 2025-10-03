package samwells.io.s3uploader.dto;

public record UploadDataDto(
        long id,
        int partNumber,
        String url,
        long partSizeInBytes
) { }
