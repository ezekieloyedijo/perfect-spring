package com.perfectspring.topic05_security.example;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * EXAMPLE 2 — PASSWORD HASHING WITH BCRYPT
 *
 * Story: the MEAT GRINDER. Meat -> mince is easy. Mince -> meat is impossible.
 * To check a login, grind what the user typed and compare the mince.
 */
public class Example2_PasswordHashing {

    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "ada123";
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        System.out.println("Password:             " + password);
        System.out.println("Hash #1:              " + hash1);
        System.out.println("Hash #2:              " + hash2);
        System.out.println("Same hash both times? " + hash1.equals(hash2) + "  <- random SALT makes each one different");
        System.out.println();
        System.out.println("matches(\"ada123\", hash1) = " + encoder.matches("ada123", hash1));
        System.out.println("matches(\"ada123\", hash2) = " + encoder.matches("ada123", hash2));
        System.out.println("matches(\"Ada123\", hash1) = " + encoder.matches("Ada123", hash1) + "  <- one capital letter is enough to fail");
        System.out.println();
        System.out.println("Reading a hash:  $2a$ = BCrypt version | 10$ = cost (how slow) | next 22 chars = salt | rest = the hash");
        System.out.println("There is NO encoder.decode(...). That's the point.");
    }
}
