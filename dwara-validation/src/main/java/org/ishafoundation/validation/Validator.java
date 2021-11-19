package org.ishafoundation.validation;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Validator {

    public static void main(String[] args) {
        FileValidations fileValidations = new FileValidations();
        String dept = args[0];
        String type = args[1];
        String pathName = args[2];

        //System.out.println("\n=============================================================================================================\n\t\t\t\t\t\tSummary of the Validation\n=============================================================================================================");

        if (type.equalsIgnoreCase("backup")) {
            fileValidations.backupFilesValidatorInPath(pathName);

        } else if ((dept.equalsIgnoreCase("glp") || dept.equalsIgnoreCase("ilp")) && type.equalsIgnoreCase("archive")) {
            Map summaryMap = fileValidations.validateFilesInPath(pathName);
            getTheSummary(summaryMap, type);

        } else if (type.equalsIgnoreCase("photo")) {
            Map summaryMap = fileValidations.validatePhotoFoldersInPath(pathName);
            getTheSummary(summaryMap, type);
        }

        System.out.println("\n========================================\n\tEnd of Summary for Files Validation\n========================================");

    }

    private static void getTheSummary(Map<Map, Map> summaryMap, String type) {

        System.out.println("\n\t\tValidations Summary\n========================================");


        int foldersCount = 0, filesCount = 0, failedCount = 0;
        Map<String, String> folders = new HashMap<>();
        Map<String, String> files = new HashMap<>();


        for (Map.Entry<Map, Map> set : summaryMap.entrySet()) {
            int folderFailedCount = 0, fileFailedCount = 0;
            folders = (Map<String, String>) set.getKey();
            files = (Map<String, String>) set.getValue();

            if (type.equalsIgnoreCase("archive")) {
                foldersCount += folders.entrySet().stream().filter(item-> !item.getKey().contains("\\Video Output")).count();
            } else if (type.equalsIgnoreCase("photo")) {
                foldersCount += folders.entrySet().stream().count();
            }


            filesCount += files.entrySet().stream().count();
            folderFailedCount += folders.entrySet().stream().filter(item-> !StringUtils.isEmpty(item.getValue())).count();
            fileFailedCount += files.entrySet().stream().filter(item-> !StringUtils.isEmpty(item.getValue())).count();
            failedCount = folderFailedCount + fileFailedCount;

            /*Map<String, String> fol = set.getKey();
            Map<String, String> fil = set.getValue();
                for (Map.Entry<String, String> folSet : fol.entrySet()) {
                    if(!StringUtils.isEmpty(folSet.getValue())) {
                        System.out.println("\nFolder Name : " + folSet.getKey() + "\t" + folSet.getValue());
                        for (Map.Entry<String, String> filSet : fil.entrySet()) {
                            if(!StringUtils.isEmpty(filSet.getValue())) {
                                System.out.println("\n\tFile Name : " + filSet.getKey() + "\t\t" + filSet.getValue());
                            }
                        }
                    }
            }*/

        }
        System.out.println("\n\t\t Total Folder Count : " + foldersCount + "\n\t\t Total File Count   : " + filesCount + "\n\t\t Total Failed Count : " + failedCount + "\n\n");

        //System.out.println("==========================================================================================================================================================================================================================");

        System.out.println("\n------------------------ Validating the Files in the provided path ------------------------\n");
           /* for (Map.Entry<Map, Map> set : summaryMap.entrySet()) {

                Map<String, String> folders = (Map<String, String>) set.getKey();
                Map<String, String> files = (Map<String, String>) set.getValue();
            }*/
       // Map<String, String> finalFiles = files;

        System.out.println("\n************************* Folder Validation Failures *************************\n");
        folders.entrySet().stream().filter(item-> !StringUtils.isEmpty(item.getValue())).forEach(item-> System.out.println("\t" + item.getKey() + "\n\t\t\tERROR: " + item.getValue() + "\n----------------------------------------------------------------\n"));

        System.out.println("\n************************* File Validation Failures *************************\n");
        files.entrySet().stream().filter(item-> !StringUtils.isEmpty(item.getValue())).forEach(item-> System.out.println("\t" + item.getKey() + "\n\t\t\tERROR: " + item.getValue() + "\n----------------------------------------------------------------\n"));

    }

}
