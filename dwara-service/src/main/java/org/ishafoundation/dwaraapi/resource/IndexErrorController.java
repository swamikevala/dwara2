package org.ishafoundation.dwaraapi.resource;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexErrorController implements ErrorController{

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error() {
        return "<!DOCTYPE html>\n" + 
        		"<html>\n" + 
        		"<body>\n" + 
        		"<h1>Something went wrong! </h1>\n" + 
        		"<h2>Watch your breath.</h2>\n" + 
        		"<a href=\"/\">Go Home</a>\n" + 
        		"</body>\n" + 
        		"</html>\n";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
