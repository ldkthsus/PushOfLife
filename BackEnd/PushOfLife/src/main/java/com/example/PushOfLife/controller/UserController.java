//package com.example.PushOfLife.controller;
//
//import com.example.PushOfLife.dto.user.UserJoinDTO;
//import com.example.PushOfLife.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/POL/users")
//@RequiredArgsConstructor
//public class UserController {
//
//    private final UserService userService;
//
//    @PostMapping("/join")
//    public ResponseEntity<?> registerUser(@RequestBody UserJoinDTO joinDTO) {
//        try{
//            Boolean result = userService.checkPassword(joinDTO);
//            if(!result){
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(joinDTO);
//            }
//            else{
//                userService.registerUser(joinDTO);
//                return ResponseEntity.status(HttpStatus.OK).body(joinDTO);
//            }
//        }
//        catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//}
