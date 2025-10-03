package samwells.io.s3uploader.model;

import java.util.ArrayList;
import java.util.List;

public record MultipartUpload(
        Long id,
        List<UploadPart> uploadParts
) {
    public MultipartUpload(Long id) {
        this (id, new ArrayList<>());
    }
}
