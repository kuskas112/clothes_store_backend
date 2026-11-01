package com.example.clothesstore.user;

import com.example.clothesstore.exception.EmailAlreadyExistsException;
import com.example.clothesstore.exception.UserNotFoundException;
import com.example.clothesstore.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUserHashesPassword() {
        String rawPassword = "super-secret-123";
        String email = uniqueEmail();

        User created = userService.createUser(email, rawPassword);

        assertThat(created.getEmail()).isEqualTo(email);
        assertThat(created.getRole()).isEqualTo(Role.USER);
        assertThat(created.getPassword()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, created.getPassword())).isTrue();
    }

    @Test
    void createUserWithDuplicateEmailThrows() {
        String email = uniqueEmail();
        userService.createUser(email, TestFixtures.DEFAULT_PASSWORD);

        assertThatThrownBy(() -> userService.createUser(email, TestFixtures.DEFAULT_PASSWORD))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void findByEmailReturnsEmptyForUnknownUser() {
        assertThat(userService.findByEmail(uniqueEmail())).isEmpty();
    }

    @Test
    void getUserByUnknownEmailThrows() {
        assertThatThrownBy(() -> userService.getUserByEmail(uniqueEmail()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateChangesProfileFields() {
        User user = userService.createUser(uniqueEmail(), TestFixtures.DEFAULT_PASSWORD);
        String newEmail = uniqueEmail();

        User updated = userService.updateUser(user.getId(), newEmail, "+79990000000", "Иван", "Петров");

        assertThat(updated.getEmail()).isEqualTo(newEmail);
        assertThat(updated.getPhone()).isEqualTo("+79990000000");
        assertThat(updated.getFirstName()).isEqualTo("Иван");
        assertThat(updated.getLastName()).isEqualTo("Петров");
    }

    @Test
    void updateToExistingEmailThrows() {
        User first = userService.createUser(uniqueEmail(), TestFixtures.DEFAULT_PASSWORD);
        User second = userService.createUser(uniqueEmail(), TestFixtures.DEFAULT_PASSWORD);

        assertThatThrownBy(() ->
                userService.updateUser(second.getId(), first.getEmail(), null, null, null))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updateUnknownUserThrows() {
        assertThatThrownBy(() ->
                userService.updateUser(999_999L, uniqueEmail(), null, null, null))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteRemovesUserAndUnknownIdThrows() {
        User user = userService.createUser(uniqueEmail(), TestFixtures.DEFAULT_PASSWORD);

        userService.deleteUser(user.getId());

        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThatThrownBy(() -> userService.deleteUser(user.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsersContainsCreatedUsers() {
        User first = userService.createUser(uniqueEmail(), TestFixtures.DEFAULT_PASSWORD);
        User second = userService.createUser(uniqueEmail(), TestFixtures.DEFAULT_PASSWORD);

        assertThat(userService.getAllUsers())
                .extracting(User::getId)
                .contains(first.getId(), second.getId());
    }

    private static String uniqueEmail() {
        return "service-" + UUID.randomUUID() + "@test.local";
    }
}
