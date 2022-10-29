package me.synology.hajubal.userservice.controller;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.synology.hajubal.userservice.dto.UserDto;
import me.synology.hajubal.userservice.repository.UserEntity;
import me.synology.hajubal.userservice.service.UserService;
import me.synology.hajubal.userservice.vo.Greeting;
import me.synology.hajubal.userservice.vo.RequestUser;
import me.synology.hajubal.userservice.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/")
@RestController
public class UserController {

    private final Environment env;

    private final UserService userService;

    private final Greeting greeting;

    @GetMapping("/health_check")
    @Timed(value="users.status", longTask = true)
    public String status() {
        return String.format("It's Working in User Service"
                + ", port(local.server.port)=" + env.getProperty("local.server.port")
                + ", port(server.port)=" + env.getProperty("server.port")
                + ", gateway ip=" + env.getProperty("gateway.ip")
                + ", message=" + env.getProperty("greeting.message")
                + ", token secret=" + env.getProperty("token.secret")
                + ", token expiration time=" + env.getProperty("token.expiration_time"));
    }

    @GetMapping("/welcome")
    @Timed(value="users.welcome", longTask = true)
    public String welcome(HttpServletRequest request, HttpServletResponse response) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            Arrays.stream(cookies).forEach(cookie -> {
//                System.out.print(cookie.getName() + "=" + cookie.getValue());
//            });
//        }
//        Cookie c1 = new Cookie("myuser_token", "abcd1234");
//        response.addCookie(c1);

//        return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        List<UserDto> userList = userService.findAll();

        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser returnValue = new ModelMapper().map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }
}
