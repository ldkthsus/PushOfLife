package com.example.PushOfLife.service;

import com.example.PushOfLife.dto.user.UpdateRequestDTO;
import com.example.PushOfLife.dto.user.UpdateResponseDTO;
import com.example.PushOfLife.dto.user.UserJoinDTO;
import com.example.PushOfLife.dto.user.UserListDTO;
import com.example.PushOfLife.entity.UserEntity;
import com.example.PushOfLife.jwt.JwtUtil;
import com.example.PushOfLife.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;


    public void registerUser(UserJoinDTO joinDTO){
        UserEntity user = UserEntity.builder()
                .userName(joinDTO.getUserName())
                .password(bCryptPasswordEncoder.encode(joinDTO.getUserPassword1()))
                .userBirthday(joinDTO.getUserBirthday())
                .userGender(joinDTO.getUserGender())
                .userDisease(joinDTO.getUserDisease())
                .userPhone(joinDTO.getUserPhone())
                .userProtector(joinDTO.getUserProtector())
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    public boolean checkPassword(String password1, String password2){
        if (password1 == null && password2 == null){
            return true;
        }
        return password1.equals(password2);
    }

    public UserEntity findByRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        UserEntity user = userRepository.findByUserPhone(jwtUtil.getPhone(token));
        return user;
    }

    public UpdateResponseDTO updateUserInfo(UserEntity user, UpdateRequestDTO updateDTO){
       Boolean result = checkPassword(updateDTO.getPassword1(), updateDTO.getPassword2());
       if (result){
           user.updateInfo(updateDTO, bCryptPasswordEncoder.encode(updateDTO.getPassword1()));
           userRepository.save(user);
           UpdateResponseDTO updateResponseDTO = new UpdateResponseDTO();
           updateResponseDTO.fromEntity(user);
           return updateResponseDTO;
       }
       else {
           throw new RuntimeException("User not found");
       }
    }

    public List<UserListDTO> getUserList(){
        List<UserEntity> findUserList = userRepository.findAll();
        List<UserListDTO> allUsers = new ArrayList<>();
        for (UserEntity user : findUserList) {
            UserListDTO userInfo = new UserListDTO();
            userInfo.fromEntity(user);
            allUsers.add(userInfo);
        }
        return allUsers;
    }

    public void deleteUser(UserEntity user){
        userRepository.delete(user);
    }
}
