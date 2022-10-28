package me.synology.hajubal.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.synology.hajubal.userservice.dto.UserDto;
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
    public String status() {
        return String.format("It's working. %s", env.getProperty("local.server.port"));
    }

    @GetMapping("/welcome")
    public String welcome() {
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
    public ResponseEntity<List<ResponseUser>> userList() {
        List<UserDto> userList = userService.findAll();

        ModelMapper mapper = new ModelMapper();

        List<ResponseUser> collect = userList.stream().map(userDto -> mapper.map(userDto, ResponseUser.class))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(collect);
    }
}
