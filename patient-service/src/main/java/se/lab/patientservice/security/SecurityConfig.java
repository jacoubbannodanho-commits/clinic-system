package se.lab.patientservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Keycloak realm roles are in JWT claim: realm_access.roles
     * We map BOTH:
     *   - "doctor" (for hasAnyAuthority("doctor"))
     *   - "ROLE_doctor" (for hasAnyRole("doctor"))
     */
    private Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakJwtAuthConverter() {
        return jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) {
                return new JwtAuthenticationToken(jwt, List.of());
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.getOrDefault("roles", List.of());

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority(role));            // "doctor"
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));  // "ROLE_doctor"
            }

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Health
                        .requestMatchers("/actuator/**").permitAll()

                        // READ patients – doctor, staff, patient
                        .requestMatchers(HttpMethod.GET, "/api/patients/**")
                        .hasAnyAuthority("doctor", "staff", "patient")

                        // WRITE patients – doctor, staff
                        .requestMatchers("/api/patients/**")
                        .hasAnyAuthority("doctor", "staff")

                        // Everything else
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthConverter()))
                );

        return http.build();
    }
}
