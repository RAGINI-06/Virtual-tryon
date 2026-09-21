package com.arose.ai;

import org.springframework.stereotype.Service;

@Service
public class MockAIService implements AIService {

    @Override
    public byte[] generateTryOn(
            byte[] personImage,
            String personMimeType,
            byte[] garmentImage,
            String garmentMimeType
    ) {
        throw new UnsupportedOperationException(
                "Virtual try-on AI is not implemented yet."
        );
    }
}