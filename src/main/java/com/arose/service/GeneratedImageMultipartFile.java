package com.arose.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GeneratedImageMultipartFile
        implements MultipartFile {

    private final byte[] content;
    private final String filename;
    private final String contentType;

    public GeneratedImageMultipartFile(
            byte[] content,
            String filename,
            String contentType
    ) {
        this.content = content;
        this.filename = filename;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(
            java.io.File dest
    ) throws IOException {
        java.nio.file.Files.write(
                dest.toPath(),
                content
        );
    }
}