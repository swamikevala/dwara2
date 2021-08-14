package org.ishafoundation.dwaraapi.authn;

import java.util.Base64;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MyPasswordEncoder implements PasswordEncoder{
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Override
    public String encode(CharSequence rawPassword) {
        /* String encodedString = Base64.getEncoder().encodeToString(rawPassword.toString().getBytes());
        StringBuilder sb = new StringBuilder(encodedString);
        return sb.reverse().toString(); */

        return bCryptPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // System.out.println("rawPassword: " + rawPassword.toString() + ", encodedPassword: " + encodedPassword);
        // return encode(rawPassword).equals(encodedPassword);
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
