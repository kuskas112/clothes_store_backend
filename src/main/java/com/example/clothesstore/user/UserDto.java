package com.example.clothesstore.user;

import java.util.List;

/**
 * DTO for {@link User}
 */
public record UserDto(
    Long id,
    String email,
    Role role,
    String firstName,
    String lastName,
    String phone
) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone()
        );
    }

    public static List<UserDto> from(List<User> users) {
        return users
                .stream()
                .map(UserDto::from)
                .toList();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "email = " + email + ", " +
                "role = " + role + ", " +
                "firstName = " + firstName + ", " +
                "lastName = " + lastName + ", " +
                "phone = " + phone + ")";
    }
}