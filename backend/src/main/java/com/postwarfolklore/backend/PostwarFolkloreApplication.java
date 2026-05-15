package com.postwarfolklore.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@SpringBootApplication
public class PostwarFolkloreApplication {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    public static void main(String[] args) {
        SpringApplication.run(PostwarFolkloreApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("App started — max-file-size={}", maxFileSize);
    }
}
