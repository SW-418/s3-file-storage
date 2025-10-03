package samwells.io.s3uploader.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUploadRequestDto(
        @NotNull @Size(min = 1, max = 150, message = "fileName must be between 1 and 150 characters long")
        String fileName,
        // 200MB max file size
        @NotNull @Min(1) @Max(value = 1024 * (200 * 1024), message = "Max file size is 200MiB")
        long fileSizeInBytes
) {
}
