package samwells.io.s3uploader.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import samwells.io.s3uploader.dto.response.ErrorDto;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UploadPartAlreadyCompleteException.class)
    ResponseEntity<ErrorDto> handleConflict(Exception e) {
        return ResponseEntity
                .status(409)
                .body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorDto> handleExceptions(Exception e) {
        return ResponseEntity.internalServerError().body(new ErrorDto("Internal Server error"));
    }
}
