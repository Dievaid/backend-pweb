package acs.upb.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.awt.*;
import java.net.URI;
import java.util.List;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication implements CommandLineRunner {
    @Value("${server.port}")
    private String port;

    private final Environment environment;

    public void openBrowserInOpenApi() {
        if (List.of(environment.getActiveProfiles()).contains("local")) {
            System.setProperty("java.awt.headless", "false");
        }

        if (Desktop.isDesktopSupported()) {
            String url = String.format("http://localhost:%s/swagger-ui/index.html", port);
            try {
                Desktop.getDesktop().browse(new URI(url));
                log.info("Launched browser with OpenAPI");
            } catch (Exception e) {
                log.error("Could not launch browser", e.getCause());
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        openBrowserInOpenApi();
    }
}
