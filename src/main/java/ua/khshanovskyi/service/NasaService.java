package ua.khshanovskyi.service;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.khshanovskyi.dto.NasaRequest;
import ua.khshanovskyi.dto.Photo;
import ua.khshanovskyi.dto.QueueNasaRequest;
import ua.khshanovskyi.feign.NasaFeign;
import ua.khshanovskyi.storage.PhotoStorage;

@Service
@RequiredArgsConstructor
@Slf4j
public class NasaService {
    private final NasaFeign nasaFeign;
    private final PhotoStorage photoStorage;
    private final RabbitTemplate rabbitTemplate;

    @Value("${nasa.key}")
    private String apiKey;
    @Value("${nasa.exchange}")
    private String nasaExchangeName;

    public String postToQueueAndGenerateCommandId(NasaRequest nasaRequest) {
        Objects.requireNonNull(nasaRequest, "NasaRequest cannot be null");
        var commandId = generateCommandId();

        rabbitTemplate.convertAndSend(nasaExchangeName, "", new QueueNasaRequest(commandId, nasaRequest));

        log.trace("Command id [{}] is successfully generated and pushed to queue", commandId);
        return commandId;
    }

    public Optional<byte[]> getLargestPicture(String commandId) {
        return Optional.ofNullable(photoStorage.getPhoto(commandId));
    }

    public Mono<byte[]> getLargestPicture(int sol, String camera) {
        return Mono.justOrEmpty(nasaFeign.getAllPhotosBySolAndCamera(apiKey, sol, camera))
          .flatMapMany(photos -> Flux.fromIterable(photos.photos()))
          .map(Photo::url)
          .flatMap(this::getRedirectedLocationUrl)
          .flatMap(this::getPhotoByUrl)
          .reduce((o1, o2) -> o1.getHeaders().getContentLength() > o2.getHeaders().getContentLength() ? o1 : o2)
          .mapNotNull(HttpEntity::getBody);
    }

    private Mono<String> getRedirectedLocationUrl(String url) {
        return WebClient.create(url)
          .head()
          .retrieve()
          .toBodilessEntity()
          .mapNotNull(voidResponseEntity -> voidResponseEntity.getHeaders().getLocation().toString());
    }

    private Mono<ResponseEntity<byte[]>> getPhotoByUrl(String url) {
        return WebClient.create(url)
          .mutate()
          .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(10_000_000))
          .build()
          .get()
          .retrieve()
          .toEntity(byte[].class);
    }

    private String generateCommandId() {
        return RandomStringUtils.randomAlphabetic(7);
    }
}
