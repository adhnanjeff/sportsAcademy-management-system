package com.badminton.academy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.badminton.academy.repository.UserRepository;

@Configuration
public class OpenAPIConfig {

        @Bean
        CommandLineRunner fixPasswords(UserRepository userRepository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {

                userRepository.findByEmail("seed.coach@academy.com")
                        .ifPresent(user -> {
                        user.setPassword(passwordEncoder.encode("coach123"));
                        userRepository.save(user);
                        System.out.println("Password updated for seed.coach@academy.com");
                        });

                userRepository.findByEmail("admin@academy.com")
                        .ifPresent(user -> {
                        user.setPassword(passwordEncoder.encode("admin123"));
                        userRepository.save(user);
                        System.out.println("Password updated for admin@academy.com");
                        });
        };
        }


        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                        .info(new Info()
                                .title("Badminton Academy Management API")
                                .version("1.0")
                                .description("API for managing badminton academy - First Release (Basic System + Attendance)")
                                .contact(new Contact()
                                        .name("Your Name")
                                        .email("your.email@example.com"))
                                .license(new License()
                                        .name("Apache 2.0")
                                        .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .components(new Components()
                                .addSecuritySchemes("Bearer Authentication",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")));
        }
}