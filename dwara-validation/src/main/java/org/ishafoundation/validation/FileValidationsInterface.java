package org.ishafoundation.validation;

import java.nio.file.Path;

public interface FileValidationsInterface {

   void fileNameValidation(String artifactName, String file);

   void fileUppercaseValidation(String fileName, String file);

}
