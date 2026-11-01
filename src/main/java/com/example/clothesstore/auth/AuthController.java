package com.example.clothesstore.auth;

import com.example.clothesstore.exception.UnauthorizedUserException;
import com.example.clothesstore.user.User;
import com.example.clothesstore.user.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName
    ) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(
        @RequestBody @Valid RegisterRequest request,
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

    public record LoginRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank String password
    ) {}

    @PostMapping("/login")
    public UserDto login(
        @RequestBody @Valid LoginRequest request,
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

    @GetMapping("/me")
    public UserDto me() {
        log.info("User requests itself");
        User user = authService.me();
        if (user == null) {
            // Не 404: для неавторизованного клиента это именно 401.
            throw new UnauthorizedUserException();
        }
        return UserDto.from(user);
    }
}
