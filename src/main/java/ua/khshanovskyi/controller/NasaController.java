package ua.khshanovskyi.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ua.khshanovskyi.dto.NasaRequest;
import ua.khshanovskyi.exception.PhotoNotFoundException;
import ua.khshanovskyi.service.NasaService;

@RestController
@RequestMapping("/mars/pictures")
@RequiredArgsConstructor
public class NasaController {

    private final NasaService nasaService;
    @Value("${default.url}")
    private String defaultUrl;

    @PostMapping("largest")
    public ResponseEntity<?> provideLargestUrlFromQueue(@RequestBody NasaRequest nasaRequest) {
        String commandId = nasaService.postToQueueAndGenerateCommandId(nasaRequest);

        return ResponseEntity.accepted()
          .location(URI.create(String.format("%s/%s", defaultUrl, commandId)))
          .build();
    }


    @GetMapping(value = "largest/{commandId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getLargestPicture(@PathVariable String commandId) {
        return nasaService.getLargestPicture(commandId)
          .orElseThrow(() -> new PhotoNotFoundException(String.format(
            "Picture by commandId [%s] is not found.", commandId))
          );
    }
}
