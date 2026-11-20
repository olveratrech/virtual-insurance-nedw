package mx.unam.virtualinsurance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		SecurityContextRepository securityContextRepository
	) throws Exception {
		http
			.securityContext(context -> context
				.securityContextRepository(securityContextRepository)
				.requireExplicitSave(true))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/actuator/health").permitAll()
				.requestMatchers(HttpMethod.GET, "/catalogos/generos", "/auth/csrf").permitAll()
				.requestMatchers(HttpMethod.POST, "/auth/registro").permitAll()
				.requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
				.anyRequest().denyAll())
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint((request, response, exception) ->
					response.sendError(HttpStatus.UNAUTHORIZED.value()))
				.accessDeniedHandler((request, response, exception) ->
					response.sendError(HttpStatus.FORBIDDEN.value())));

		return http.build();
	}
}
