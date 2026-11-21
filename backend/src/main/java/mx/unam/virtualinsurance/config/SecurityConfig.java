package mx.unam.virtualinsurance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
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
	AuthenticationManager authenticationManager(
		UserDetailsService userDetailsService,
		PasswordEncoder passwordEncoder
	) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
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
				.requestMatchers(HttpMethod.POST, "/auth/registro", "/auth/login").permitAll()
				.requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
				.requestMatchers("/cotizaciones/**").authenticated()
				.requestMatchers(HttpMethod.GET, "/polizas/**").authenticated()
				.anyRequest().denyAll())
			.logout(logout -> logout
				.logoutUrl("/auth/logout")
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.deleteCookies("JSESSIONID")
				.logoutSuccessHandler((request, response, authentication) ->
					response.setStatus(HttpStatus.NO_CONTENT.value())))
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint((request, response, exception) ->
					response.sendError(HttpStatus.UNAUTHORIZED.value()))
				.accessDeniedHandler((request, response, exception) ->
					response.sendError(HttpStatus.FORBIDDEN.value())));

		return http.build();
	}
}
