package org.example.usermanagement.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class SwaggerConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/");
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/api-docs/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowCredentials(true)
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .exposedHeaders("Set-Cookie");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title(""));
    }

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(org.springframework.beans.factory.annotation.Value.class);
    }

}
