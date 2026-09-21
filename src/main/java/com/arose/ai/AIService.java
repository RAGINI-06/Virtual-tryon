package com.arose.ai;

public interface AIService {

    byte[] generateTryOn(
            byte[] personImage,
            String personMimeType,
            byte[] garmentImage,
            String garmentMimeType
    );
}