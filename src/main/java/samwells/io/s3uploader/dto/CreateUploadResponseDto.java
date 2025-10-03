package samwells.io.s3uploader.dto;

import java.util.List;

public record CreateUploadResponseDto(
        long id,
        List<UploadDataDto> uploadData
) { }
