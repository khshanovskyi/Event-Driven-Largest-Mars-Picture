package ua.khshanovskyi.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ua.khshanovskyi.dto.Photos;

@FeignClient(name = "nasa-client", url = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos")
public interface NasaFeign {

    @GetMapping
    Photos getAllPhotosBySolAndCamera(@RequestParam(value = "api_key") String apiKey,
                        @RequestParam int sol,
                        @RequestParam(required = false) String camera);
}
