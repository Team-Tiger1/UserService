package com.teamtiger.userservice.users.controllers;

import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.users.exceptions.UsernameAlreadyTakenException;
import com.teamtiger.userservice.users.models.CreateUserDTO;
import com.teamtiger.userservice.users.models.CreatedUserDTO;
import com.teamtiger.userservice.users.models.LoginDTO;
import com.teamtiger.userservice.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        try {
            CreatedUserDTO createdUserDTO = userService.createUser(createUserDTO);
            return ResponseEntity.ok(createdUserDTO);
        }

        catch (UsernameAlreadyTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            CreatedUserDTO createdUserDTO = userService.userLogin(loginDTO);
            return ResponseEntity.ok(createdUserDTO);
        }

        catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (PasswordIncorrectException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
