package com.arose.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String store(MultipartFile file, String folder);

    Resource load(String storageKey);

    void delete(String storageKey);
}