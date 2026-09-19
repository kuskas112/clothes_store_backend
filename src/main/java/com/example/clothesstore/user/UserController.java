package com.example.clothesstore.user;

import com.example.clothesstore.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
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

    public record CreateUserRequest(String email, String password) {}
    public record UpdateUserRequest(String email, String phone, String firstName, String lastName) {}

    // === Создание ===
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody CreateUserRequest request) {
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
            @RequestBody UpdateUserRequest request
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