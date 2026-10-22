package Rastoder.HoneyManagementSystem.config;

import Rastoder.HoneyManagementSystem.service.FileStorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    // Comma-separated list, e.g. "http://localhost:4200,https://honig.example.com"
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry){
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location");
    }

    // Serve uploads from exactly the folder FileStorageService writes to,
    // e.g. /images/x.jpg -> <app.storage.root>/images/x.jpg
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        for (FileStorageService.Area area : FileStorageService.Area.values()) {
            String location = fileStorageService.directory(area).toUri().toString();
            registry.addResourceHandler(area.publicPrefix() + "**")
                    .addResourceLocations(location.endsWith("/") ? location : location + "/");
        }
    }
}
