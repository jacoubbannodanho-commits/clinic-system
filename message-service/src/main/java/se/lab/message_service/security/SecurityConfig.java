package se.lab.message_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Actuator (om ni vill ha den öppen)
                        .requestMatchers("/actuator/**").permitAll()

                        // Resten skyddas – detaljerad access styrs av @PreAuthorize i controllers
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    /**
     * Gör så att @PreAuthorize("hasAnyRole('patient','doctor','staff')")
     * fungerar genom att mappa Keycloak-roller till Spring authorities:
     * - realm_access.roles -> ROLE_patient, ROLE_doctor, ROLE_staff
     * - resource_access.<client>.roles -> ROLE_xxx (om ni använder client-roller)
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractKeycloakRolesAsSpringRoles);
        return converter;
    }

    private Collection<GrantedAuthority> extractKeycloakRolesAsSpringRoles(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1) Realm roles: realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof Collection<?> roles) {
                for (Object role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString()));
                }
            }
        }

        // 2) Client roles: resource_access.<client>.roles (robust ifall ni använder client-roller)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Object clientObj : resourceAccess.values()) {
                if (clientObj instanceof Map<?, ?> clientMap) {
                    Object clientRolesObj = clientMap.get("roles");
                    if (clientRolesObj instanceof Collection<?> roles) {
                        for (Object role : roles) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString()));
                        }
                    }
                }
            }
        }

        return authorities;
    }
}
