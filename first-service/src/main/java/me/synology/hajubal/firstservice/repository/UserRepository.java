package me.synology.hajubal.firstservice.repository;

import me.synology.hajubal.firstservice.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
    // 사용자 정보를 Redis에 저장하고 조회하기 위한 메소드 추가
    User findByUsername(String username);
    void deleteByUsername(String username);
}
