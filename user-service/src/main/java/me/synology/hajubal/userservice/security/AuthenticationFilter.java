package me.synology.hajubal.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.synology.hajubal.userservice.dto.UserDto;
import me.synology.hajubal.userservice.service.UserService;
import me.synology.hajubal.userservice.vo.LoginRequest;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Environment env;

    private final UserService userService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);

            log.info("loginRequest: {}", loginRequest);

            AuthenticationManager authenticationManager = getAuthenticationManager();

            log.info("authenticationManager: {}", authenticationManager);

            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            log.info("token: {}", token);

            return authenticationManager.authenticate(token);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDto = userService.findByEmail(username);

        String token = Jwts.builder()
                .setSubject(userDto.getUserId())
                .setExpiration(new Date(LocalDateTime.now().plusNanos(Long.valueOf(env.getProperty("token.expiration_time"))).getNano()))
                .signWith(SignatureAlgorithm.HS256, env.getProperty("token.secret"))
                .compact();

        response.addHeader("token", token);
        response.addHeader("userID", userDto.getUserId());
    }
}
