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

//	//Dealing with login - https://spring.io/blog/2015/01/20/the-resource-server-angular-js-and-spring-security-part-iii
//    @Override
//    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
//            throws IOException, ServletException {
//    	response.addHeader("WWW-Authenticate", "Basic realm=" +getRealmName());
//    	response.setStatus(HttpStatus.UNAUTHORIZED.value());
//    	// TODO: change the status to 401. 
//    	// UI team wants this temporarily 
//    	//response.setStatus(HttpStatus.BAD_REQUEST.value());
//    	HashMap<String, Object> data = new HashMap<String, Object>();
//    	data.put("message", authEx.getMessage());
//		ObjectMapper objectMapper = new ObjectMapper();
//		response.getOutputStream().println(objectMapper.writeValueAsString(data));
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setRealmName("DwaraDevRealm");
        super.afterPropertiesSet();
    }
}