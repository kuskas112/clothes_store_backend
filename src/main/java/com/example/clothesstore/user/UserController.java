package com.example.clothesstore.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public record CreateUserRequest(
            @NotBlank @Email @Size(max = 100) String email,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {}

    public record UpdateUserRequest(
            @NotBlank @Email @Size(max = 100) String email,
            @Size(max = 20) String phone,
            @Size(max = 50) String firstName,
            @Size(max = 50) String lastName
    ) {}

    // === Создание ===
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(
                request.email(),
                request.password()
        );
        return UserDto.from(user);
    }

    // === Чтение одного ===
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return UserDto.from(userService.getUserById(id));
    }

    @GetMapping("/by-email/{email}")
    public UserDto getUserByEmail(@PathVariable String email) {
        return UserDto.from(userService.getUserByEmail(email));
    }

    // === Чтение всех ===
    @GetMapping
    public List<UserDto> getAllUsers() {
        return UserDto.from(userService.getAllUsers());
    }

    // === Обновление ===
    @PutMapping("/{id}")
    public UserDto updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        User user = userService.updateUser(
                id,
                request.email(),
                request.phone(),
                request.firstName(),
                request.lastName()
        );
        return UserDto.from(user);
    }

    // === Удаление ===
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
