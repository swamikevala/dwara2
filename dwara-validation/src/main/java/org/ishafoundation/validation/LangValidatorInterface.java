package org.ishafoundation.validation;

public interface LangValidatorInterface {

    void folderValidator(String pathName);

    void directorySizeValidator(String pathName);

    void basicFileNameValidation(String artifactName);

    void languageValidator(String artifactName);

    void datePatternValidation(String fileName);

    void fileUppercaseValidation(String fileName);

    void fileValidator(String pathName);

}
