package com.hhl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hhl.exception.CustomAccessDeniedHandler;
import com.hhl.exception.CustomAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final AuthenticationProvider authenticationProvider;
	private final LogoutService logoutService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http.csrf().disable()
	        .authorizeHttpRequests()
	        .requestMatchers("/api/v1/auth/**", "/v2/api-docs", "/v3/api-docs", "/v3/api-docs/**",
	                        "/swagger-resources", "/swagger-resources/**", "/configuration/ui", "/configuration/security",
	                        "/swagger-ui/**", "/webjars/**", "/swagger-ui.html", "/assets/**")
	        .permitAll()
	        
	        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

	        .requestMatchers("/api/v1/secured/**")
	        .authenticated()

	        .and()
	        .sessionManagement()
	        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        .and()
	        .authenticationProvider(authenticationProvider)
	        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
	        
//	    http.exceptionHandling()
//	    .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
//	    .accessDeniedHandler(new CustomAccessDeniedHandler());

	    http.cors();

	    return http.build();
	}

}

//.requestMatchers("/api/v1/management/**").hasAnyRole(ADMIN.name(), MANAGER.name())
//
//.requestMatchers(GET, "/api/v1/management/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
//.requestMatchers(POST, "/api/v1/management/**")
//.hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
//.requestMatchers(PUT, "/api/v1/management/**")
//.hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())
//.requestMatchers(DELETE, "/api/v1/management/**")
//.hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())
//
//.anyRequest().authenticated()
