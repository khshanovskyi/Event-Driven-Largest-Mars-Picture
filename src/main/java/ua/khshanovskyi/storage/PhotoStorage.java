package ua.khshanovskyi.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class PhotoStorage {
    private final Map<String, byte[]> container = new ConcurrentHashMap<>();

    public void add(String commandId, byte[] photo) {
        container.put(commandId, photo);
    }

    public byte[] getPhoto(String commandId) {
        return container.get(commandId);
    }

}
