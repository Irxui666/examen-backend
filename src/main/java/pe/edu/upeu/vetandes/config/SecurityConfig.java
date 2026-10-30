package pe.edu.upeu.vetandes.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upeu.vetandes.security.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtService jwt,
      UserDetailsService users,
      RestAuthenticationEntryPoint entry,
      RestAccessDeniedHandler denied)
      throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .cors(org.springframework.security.config.Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/auth/me")
                    .authenticated()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/propietarios",
                        "/api/v1/mascotas",
                        "/api/v1/mascotas/**")
                    .hasAuthority("MASCOTA_LEER")
                    .requestMatchers(
                        HttpMethod.GET, "/api/v1/prestaciones", "/api/v1/prestaciones/**")
                    .hasAuthority("PRESTACION_LEER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/atenciones", "/api/v1/atenciones/**")
                    .hasAuthority("ATENCION_LEER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/reportes/**")
                    .hasAuthority("REPORTE_VER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/mascotas")
                    .hasAuthority("MASCOTA_ESCRIBIR")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/mascotas/**")
                    .hasAuthority("MASCOTA_ESCRIBIR")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/mascotas/**")
                    .hasAuthority("MASCOTA_ESCRIBIR")
                    .requestMatchers(HttpMethod.POST, "/api/v1/prestaciones")
                    .hasAuthority("PRESTACION_ESCRIBIR")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/prestaciones/**")
                    .hasAuthority("PRESTACION_ESCRIBIR")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/prestaciones/**")
                    .hasAuthority("PRESTACION_ESCRIBIR")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/prestaciones/*/estado")
                    .hasAuthority("PRESTACION_ESCRIBIR")
                    .requestMatchers(HttpMethod.POST, "/api/v1/atenciones")
                    .hasAuthority("ATENCION_REGISTRAR")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/atenciones/*/anular")
                    .hasAuthority("ATENCION_ANULAR")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(jwt, users), UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
