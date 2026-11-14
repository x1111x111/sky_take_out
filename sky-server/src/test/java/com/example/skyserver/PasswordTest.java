package com.example.skyserver;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {
    @Test
    void encodedPasswordMatchesOnlyOriginal() {
        var encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("example-test-password");
        assertNotEquals("example-test-password", encoded);
        assertTrue(encoder.matches("example-test-password", encoded));
        assertFalse(encoder.matches("incorrect", encoded));
    }
}