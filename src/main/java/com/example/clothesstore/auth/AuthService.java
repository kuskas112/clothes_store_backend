package com.example.clothesstore.auth;

import com.example.clothesstore.user.User;
import com.example.clothesstore.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(
            UserService userService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        log.info("AuthService created");
    }

    public User login(String email, String password,
                      HttpServletRequest request,
                      HttpServletResponse response) {
        // 1. Проверка пароля
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // 2. Создаём контекст и кладём в него аутентификацию
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // 3. Сохраняем контекст в сессию → Spring ставит cookie JSESSIONID
        securityContextRepository.saveContext(context, request, response);

        log.info("User {} logged in", email);

        // 4. Возвращаем пользователя
        return userService.getUserByEmail(email);
    }
    public User register(String email, String password){
        log.info("Registering user {}", email);
        return userService.createUser(email, password);
    }

    public void logout(){

    }

    public void me(){

    }



}