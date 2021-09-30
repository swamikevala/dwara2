package org.ishafoundation.dwaraapi.authn;

import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;


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
    public void afterPropertiesSet() {
        setRealmName("DwaraDevRealm");
        super.afterPropertiesSet();
    }
}