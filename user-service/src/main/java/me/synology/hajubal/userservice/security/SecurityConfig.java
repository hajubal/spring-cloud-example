package me.synology.hajubal.userservice.security;

import lombok.RequiredArgsConstructor;
import me.synology.hajubal.userservice.repository.UserRepository;
import me.synology.hajubal.userservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    private final Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(this.userDetailService())
                .passwordEncoder(this.bCryptPasswordEncoder());

        AuthenticationManager authenticationManager = builder.build();

        return http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/users/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
//                .antMatchers("/**")
//                .hasIpAddress("127.0.0.1")
                .and()
                .authenticationManager(authenticationManager)
                .addFilter(getAuthenticationFilter(authenticationManager))
                .headers().frameOptions().disable()
                .and()
                .build();
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) {
        AuthenticationFilter filter = new AuthenticationFilter(environment, userDetailService());
        filter.setAuthenticationManager(authenticationManager);

        return filter;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService userDetailService() {
        return new UserService(userRepository, bCryptPasswordEncoder());
    }
}
