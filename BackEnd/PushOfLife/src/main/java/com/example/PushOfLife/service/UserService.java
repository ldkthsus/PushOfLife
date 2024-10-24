//package com.example.PushOfLife.service;
//
//import com.example.PushOfLife.dto.user.UserJoinDTO;
//import com.example.PushOfLife.entity.UserEntity;
//import com.example.PushOfLife.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//
//    private final UserRepository userRepository;
//
//
//    public void registerUser(UserJoinDTO joinDTO){
//        UserEntity user = UserEntity.builder()
//                .userName(joinDTO.getUserName())
//                .userBirthday(joinDTO.getUserBirthday())
//                .userGender(joinDTO.getUserGender())
//                .userDisease(joinDTO.getUserDisease())
//                .userPhone(joinDTO.getUserPhone())
//                .userProtector(joinDTO.getUserProtector())
//                .build();
//
//        userRepository.save(user);
//    }
//
//    public boolean checkPassword(UserJoinDTO joinDTO){
//        if (joinDTO.getUserPassword1() == null && joinDTO.getUserPassword2() == null){
//            return true;
//        }
//        return joinDTO.getUserPassword1().equals(joinDTO.getUserPassword2());
//    }
//}
