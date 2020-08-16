package org.ishafoundation.dwaraapi.authn;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class AuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException, ServletException {
//        response.addHeader("WWW-Authenticate", "Basic realm=" +getRealmName());
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authEx.getMessage());
//        PrintWriter writer = response.getWriter();
//        writer.println("HTTP Status 401 - " + authEx.getMessage());
    	
    	response.setStatus(HttpStatus.BAD_REQUEST.value());
    	HashMap<String, Object> data = new HashMap<String, Object>();
    	data.put("message", authEx.getMessage());
//    	data.put("message", "Wrong user name or password!");
		ObjectMapper objectMapper = new ObjectMapper();
		response.getOutputStream().println(objectMapper.writeValueAsString(data));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setRealmName("DwaraDevRealm");
        super.afterPropertiesSet();
    }

}