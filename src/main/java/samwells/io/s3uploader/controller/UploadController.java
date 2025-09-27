package samwells.io.s3uploader.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import samwells.io.s3uploader.service.UploadService;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {
    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        uploadService.upload(file);
        return ResponseEntity.ok("we made it b");
    }
}
