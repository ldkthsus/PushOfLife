package com.example.PushOfLife.service;

import com.example.PushOfLife.dto.user.UserJoinDTO;
import com.example.PushOfLife.entity.UserEntity;
import com.example.PushOfLife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public void registerUser(UserJoinDTO joinDTO){
        UserEntity user = UserEntity.builder()
                .userName(joinDTO.getUserName())
                .password(passwordEncoder.encode(joinDTO.getUserPassword1()))
                .userBirthday(joinDTO.getUserBirthday())
                .userGender(joinDTO.getUserGender())
                .userDisease(joinDTO.getUserDisease())
                .userPhone(joinDTO.getUserPhone())
                .userProtector(joinDTO.getUserProtector())
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    public boolean checkPassword(UserJoinDTO joinDTO){
        if (joinDTO.getUserPassword1() == null && joinDTO.getUserPassword2() == null){
            return true;
        }
        return joinDTO.getUserPassword1().equals(joinDTO.getUserPassword2());
    }
}
