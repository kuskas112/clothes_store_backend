package com.example.clothesstore.auth;

import com.example.clothesstore.user.User;
import com.example.clothesstore.user.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(
        AuthController.class
    );
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record RegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName
    ) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(
        @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        User user = authService.register(
            request.email(),
            request.password(),
            request.firstName(),
            request.lastName()
        );

        authService.login(
            request.email(),
            request.password(),
            httpRequest,
            httpResponse
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
    }

    @PostMapping("/me")
    public UserDto me() {
        log.info("User requests itself");
        return UserDto.from(authService.me());
    }
}
