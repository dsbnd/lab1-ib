package ru.itmo.security.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.initial-admin-username}") String username,
            @Value("${app.initial-admin-password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("INITIAL_ADMIN_PASSWORD must be set");
        }
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(new AppUser(username, passwordEncoder.encode(password)));
        }
    }
}

