package org.ishafoundation.dwaraapi.authn;

import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MyPasswordEncoder implements PasswordEncoder{

    @Override
    public String encode(CharSequence rawPassword) {
        String encodedString = Base64.getEncoder().encodeToString(rawPassword.toString().getBytes());
        StringBuilder sb = new StringBuilder(encodedString);
        return sb.reverse().toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // System.out.println("rawPassword: " + rawPassword.toString() + ", encodedPassword: " + encodedPassword);
        return encode(rawPassword).equals(encodedPassword);
    }
}
