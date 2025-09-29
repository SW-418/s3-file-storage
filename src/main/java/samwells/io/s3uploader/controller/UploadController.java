package samwells.io.s3uploader.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import samwells.io.s3uploader.service.UploadService;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {
    private final UploadService syncUploadService;
    private final UploadService asyncUploadService;

    public UploadController(@Qualifier("sync") UploadService uploadService, @Qualifier("async") UploadService asyncUploadService) {
        this.syncUploadService = uploadService;
        this.asyncUploadService = asyncUploadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("mode") String mode) {
        if (mode.equals("sync")) {
            syncUploadService.upload(file);
            return ResponseEntity.ok("we made it b");
        }
        if (mode.equals("async")) {
            asyncUploadService.upload(file);
            return ResponseEntity.ok("we made it b");
        }

        return ResponseEntity.badRequest().body("Bad request b");
    }
}
