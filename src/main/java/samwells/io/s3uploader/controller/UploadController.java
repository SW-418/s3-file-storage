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
    private final UploadService managedUploadService;

    public UploadController(
            @Qualifier("sync") UploadService uploadService,
            @Qualifier("async") UploadService asyncUploadService,
            @Qualifier("managed") UploadService managedUploadService
    ) {
        this.syncUploadService = uploadService;
        this.asyncUploadService = asyncUploadService;
        this.managedUploadService = managedUploadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("mode") String mode) {
        if (mode.equals("sync")) {
            syncUploadService.upload(file);
            return ResponseEntity.ok("we made it b - sync");
        }
        if (mode.equals("async")) {
            asyncUploadService.upload(file);
            return ResponseEntity.ok("we made it b - async");
        }
        if (mode.equals("managed")) {
            managedUploadService.upload(file);
            return ResponseEntity.ok("we made it b - managed");
        }

        return ResponseEntity.badRequest().body("Bad request b");
    }
}
