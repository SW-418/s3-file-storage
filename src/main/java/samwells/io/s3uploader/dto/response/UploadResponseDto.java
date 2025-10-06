package samwells.io.s3uploader.dto.response;

import java.util.List;

public record UploadResponseDto(
        long id,
        List<UploadDataDto> uploadData
) { }
