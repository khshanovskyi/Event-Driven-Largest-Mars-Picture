package ua.khshanovskyi.listener;

import java.util.Objects;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ua.khshanovskyi.dto.QueueNasaRequest;
import ua.khshanovskyi.exception.PhotoNotFoundException;
import ua.khshanovskyi.service.NasaService;
import ua.khshanovskyi.storage.PhotoStorage;

@Component
@RequiredArgsConstructor
public class NasaQueueListener {
    private final NasaService nasaService;
    private final PhotoStorage photoStorage;
    @Value("${nasa.queue}")
    private String nasaQueueName;

    @RabbitListener(queues = "nasa-photo-queue")
    public void handleMessage(QueueNasaRequest queueNasaRequest) {
        Objects.requireNonNull(queueNasaRequest, "QueueNasaRequest cannot be null");
        var nasaRequest = Objects.requireNonNull(queueNasaRequest.nasaRequest(), "NasaRequest cannot be null");

        var photo = nasaService.getLargestPicture(nasaRequest.sol(), nasaRequest.camera()).blockOptional();

        if (photo.isPresent()) {
            photoStorage.add(queueNasaRequest.commandId(), photo.get());
        } else {
            throw new PhotoNotFoundException(String.format("Photo with sol [%s] and camera [%s] is not found",
              nasaRequest.sol(), nasaRequest.camera()));
        }
    }

}
