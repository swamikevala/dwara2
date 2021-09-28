package org.ishafoundation.validation;


public class Validator {

    public static void main(String[] args) {
        LangValidator langValidator = new LangValidator();
        String dept = args[0];
        String type = args[1];
        String pathName = args[2];
        langValidator.validations(dept, type, pathName);

        System.out.println("\nplease check the logs for invalid validations");

    }
}
