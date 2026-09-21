package com.arose.ai;

import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiAIService implements AIService {

    private final Client client;
    private final String model;

    public GeminiAIService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.model = model;
    }

    @Override
    public byte[] generateTryOn(
            byte[] personImage,
            String personMimeType,
            byte[] garmentImage,
            String garmentMimeType
    ) {

        Part personPart = Part.builder()
                .inlineData(
                        Blob.builder()
                                .mimeType(personMimeType)
                                .data(personImage)
                                .build()
                )
                .build();

        Part garmentPart = Part.builder()
                .inlineData(
                        Blob.builder()
                                .mimeType(garmentMimeType)
                                .data(garmentImage)
                                .build()
                )
                .build();

        Part promptPart = Part.builder()
                .text(
                        """
                        Create a virtual try-on image.

                        Use the first image as the person reference.
                        Use the second image as the garment reference.

                        Generate a realistic image of the same person
                        wearing the provided garment.

                        Preserve the person's identity, face, body proportions,
                        pose, and overall appearance as much as possible.

                        Make the garment fit naturally on the person.
                        Preserve the garment's color, design, texture, and shape.

                        Do not add extra people or extra garments.

                        Return the generated image.
                        """
                )
                .build();

        List<Part> parts = new ArrayList<>();
        parts.add(personPart);
        parts.add(garmentPart);
        parts.add(promptPart);

        Content content = Content.builder()
                .parts(parts)
                .build();

        List<String> responseModalities = new ArrayList<>();
        responseModalities.add("TEXT");
        responseModalities.add("IMAGE");

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseModalities(responseModalities)
                        .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        content,
                        config
                );

        return extractGeneratedImage(response);
    }

    private byte[] extractGeneratedImage(
            GenerateContentResponse response
    ) {

        List<Candidate> candidates =
                response.candidates()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Gemini returned no candidates"
                                )
                        );

        for (Candidate candidate : candidates) {

            List<Part> responseParts =
                    candidate.content()
                            .flatMap(Content::parts)
                            .orElse(List.of());

            for (Part part : responseParts) {

                if (part.inlineData().isPresent()) {

                    Blob blob = part.inlineData().get();

                    if (blob.data().isPresent()) {
                        return blob.data().get();
                    }
                }
            }
        }

        throw new RuntimeException(
                "Gemini response did not contain a generated image"
        );
    }
}