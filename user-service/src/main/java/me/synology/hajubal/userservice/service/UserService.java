package me.synology.hajubal.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.synology.hajubal.userservice.dto.UserDto;
import me.synology.hajubal.userservice.repository.UserEntity;
import me.synology.hajubal.userservice.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserEntity user = mapper.map(userDto, UserEntity.class);
        user.setEncryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()));

        userRepository.save(user);

        UserDto responseUserDto = mapper.map(user, UserDto.class);

        return responseUserDto;
    }

    public UserDto findByEmail(String email) {
        UserEntity byEmail = userRepository.findByEmail(email);

        ModelMapper mapper = new ModelMapper();
        UserDto userDto = mapper.map(byEmail, UserDto.class);

        log.info("userDto: {}", userDto);

        return userDto;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("username: {}", username);

        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null) {
            throw new IllegalArgumentException("User not found exception");
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(), new ArrayList<>());
    }

    public List<UserDto> findAll() {
        List<UserEntity> userList = userRepository.findAll();
        ModelMapper mapper = new ModelMapper();

        return userList.stream().map(userEntity -> mapper.map(userEntity, UserDto.class)).collect(Collectors.toList());
    }
}
