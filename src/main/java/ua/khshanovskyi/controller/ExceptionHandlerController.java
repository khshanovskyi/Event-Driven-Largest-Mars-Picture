package ua.khshanovskyi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ua.khshanovskyi.exception.PhotoNotFoundException;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(PhotoNotFoundException.class)
    public ResponseEntity<String> handlePhotoNotFoundException(PhotoNotFoundException photoNotFoundException){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(photoNotFoundException.getMessage());
    }
}
