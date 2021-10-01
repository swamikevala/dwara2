package org.ishafoundation.validation;


import java.io.FileNotFoundException;

public class Validator {

    public static void main(String[] args) throws FileNotFoundException {
        FileValidations fileValidations = new FileValidations();
        String dept = args[0];
        String type = args[1];
        String pathName = args[2];

        System.out.println("\n=========================================================================\n\t\t\tSummary of the Files Validation\n======================================================================");

        if (type.equals("backup")) {
            fileValidations.backupFilesValidatorInPath(pathName);

        } else if ((dept.equals("glp") || dept.equals("ilp")) && type.equals("archive")) {
            fileValidations.validateFilesInPath(pathName);

        }

    }
}
