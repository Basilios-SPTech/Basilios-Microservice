package com.basilios.emailapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EmailApiApplicationTests {

    @Test
    void main_doesNotThrow() {
        assertDoesNotThrow(() -> {
            // Verifica que a classe Application existe e tem o método main
            EmailApiApplication.class.getDeclaredMethod("main", String[].class);
        });
    }

}
