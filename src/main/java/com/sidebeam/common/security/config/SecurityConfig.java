package com.sidebeam.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidebeam.common.security.config.property.SecurityProperties;
import com.sidebeam.common.web.filter.ApiKeyAuthFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectProvider<SecurityProperties> securityPropertiesProvider,
                                                   ObjectMapper objectMapper) throws Exception {

        SecurityProperties props = securityPropertiesProvider.getIfAvailable(SecurityProperties::new);

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/webhook/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new ApiKeyAuthFilter(props, objectMapper), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
