package com.example.clothesstore.auth;

import com.example.clothesstore.user.User;
import com.example.clothesstore.user.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record RegisterRequest(String email, String password) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@RequestBody RegisterRequest request) {
        User user = authService.register(
                request.email(),
                request.password()
        );
        return UserDto.from(user);
    }

    public record LoginRequest(String email, String password) {}

    @PostMapping("/login")
    public UserDto login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        User user = authService.login(
                request.email(),
                request.password(),
                httpRequest,
                httpResponse
        );
        return UserDto.from(user);
    }

    @PostMapping("/logout")
    public void logout(){
    }

    @GetMapping("/me")
    public void me(){

    }

}