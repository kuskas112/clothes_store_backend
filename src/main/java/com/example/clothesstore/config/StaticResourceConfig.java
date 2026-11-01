package com.example.clothesstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.storage.location}")
    private String location;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Путь всегда заканчивается разделителем, иначе Spring трактует его как файл.
        String path = Paths.get(location).toAbsolutePath().toUri().toString();
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        registry.addResourceHandler("/files/**")
                .addResourceLocations(path)
                // Имена файлов — UUID, содержимое неизменяемо: можно кэшировать надолго.
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }
}
