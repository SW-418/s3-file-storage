package samwells.io.s3uploader.dto;

import java.util.List;

public record UploadResponseDto(
        long id,
        List<UploadDataDto> uploadData
) { }
