package com.hk.chart.service;

import com.hk.chart.dto.Register;
import com.hk.chart.entity.User;
import com.hk.chart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

  
    @Override
    @Transactional
    public User register(Register dto) throws IllegalArgumentException {
    	System.out.println("회원가입 Service 시작: " + dto);
        // 중복 아이디 확인
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        // DTO -> Entity 변환
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword())); // 암호화
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        // phone은 null 가능, 필요하면 추가로 dto에서 받기

        // DB 저장
        return userRepository.save(user);
    }

   
    @Override
    public Optional<User> login(String username, String rawPassword) {
        Optional<User> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            return Optional.empty();
        }

        User user = maybeUser.get();
        
        // 테스트용 로그: 실제 입력과 DB 저장된 hash 비교
        System.out.println("password matches? " + passwordEncoder.matches(rawPassword, user.getPasswordHash()));
        System.out.println("입력 비밀번호: " + rawPassword);
        System.out.println("DB 해시: " + user.getPasswordHash());

        // 비밀번호 비교
        if (passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }
}