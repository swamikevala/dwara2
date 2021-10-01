package org.ishafoundation.validation;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class FileValidations implements FileValidationsInterface {
    //public static Logger logger = LoggerFactory.getLogger(LangValidator.class);

    public Set<String> languages = new HashSet<>();
    public Set<String> languageCode = new HashSet<>();
    Map<String, String[]> summary = new HashMap<>();
    List<String> summaryDetails = new ArrayList<>();
    String details[];
    Map<String, String> languageMap = new HashMap<>();

    {
        String[] languages = Locale.getISOLanguages();
        for (String language : languages) {
            Locale locale = new Locale(language);
            this.languages.add(locale.getDisplayLanguage());
            this.languageCode.add(locale.getISO3Language());
            this.languageMap.put(locale.getDisplayLanguage(), locale.getISO3Language());
        }

    }


    public void backupFilesValidatorInPath(String path) {
        try {
            List<Path> pathNames = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            for (Path pathName : pathNames) {
                if (Pattern.matches("^.*\\.(mp4|mov)", pathName.toString())) {
                    System.out.println("\n File Name Validation in the path : {" + pathName.toString() + "}");
                    fileNameValidation(pathName.getFileName().toFile().getName());

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void fileNameValidation(String fileName) {

        String regexAllowedChrsInFileName = "[\\w-.]*";
        Pattern allowedCharInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);

        if (fileName.length() > 245) { // 245 because we need to add sequence number
            //logger.error("Artifact Name is more than 245 characters");
            summaryDetails.add("Artifact Name is more than 245 characters");
            System.out.println("\tArtifact Name is more than 245 characters");
        }

        Matcher m = allowedCharInFileNamePattern.matcher(fileName);
        if (!m.matches()) {
            //logger.error("Artifact Name : {" + fileName + "} contains special characters");
            summaryDetails.add("Artifact Name : {" + fileName + "} contains special characters");
            System.out.println("\tArtifact Name : {" + fileName + "} contains special characters");
        }

        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            decoder.decode(ByteBuffer.wrap(fileName.getBytes()));
        } catch (CharacterCodingException ex) {
            //logger.error("Artifact Name : {" + fileName + "} contains non-unicode characters");
            summaryDetails.add("Artifact Name : {" + fileName + "} contains non-unicode characters");
            System.out.println("\tArtifact Name : {" + fileName + "} contains non-unicode characters");
        }

    }


    @Override
    public void fileUppercaseValidation(String fileName) {
        String[] nameSplit = fileName.split("-");
        for (String name : nameSplit) {
            if (!StringUtils.isAllUpperCase(name.subSequence(0, 1))) {
                //logger.error("In the File : {" + fileName + "} The first letters of words in name : {" + name + "} are not in UPPERCASE");
                summaryDetails.add("In name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
                System.out.println("\tIn name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
            }

        }

    }


    public void validateFilesInPath(String path) {
        try {
            List<Path> pathNames = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile).filter(Files::isWritable)
                    .collect(Collectors.toList());
            findAllValidationsInThePath(pathNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void findAllValidationsInThePath(List<Path> pathNames) throws FileNotFoundException {
        for (Path pathName : pathNames) {

            if (Pattern.matches("^.*\\.(mp4|mov)", pathName.toString())) {
                System.out.println("\nFile Validated in the path : " + pathName);
                if (Pattern.matches("^.*_(DUB|SUB)+_.*$", pathName.getFileName().toString())) {
                    if (Pattern.matches("^[a-zA-Z]+([0-9]*)+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString()) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString())) {
                        String splitter[] = pathName.getFileName().toString().split("_");
                        allNONYTValidations(pathName.getFileName().toString(), splitter);
                    } else {
                        System.out.println("\tArtifact : {" + pathName.getFileName() + "}  Path : {" + pathName.toString() + "} is not named properly.\n \t\t\t or Check the date format (dd-MMM-yyyy) of the file");
                    }
                } else if (Pattern.matches("^.*\\\\(Video Output)\\\\.*$", pathName.toString())) {
                    if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString()) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString())) {
                        String splitter[] = pathName.getFileName().toString().split("_");
                        allYTValidations(pathName.toString(), pathName.getFileName().toString(), splitter);

                    } else {
                        System.out.println("\tArtifact : {" + pathName.getFileName() + "}  Path : {" + pathName.toString() + "} is not named properly.\n \t\t\t or Check the date format (dd-MMM-yyyy) of the file");

                    }
                } else if (Pattern.matches("^(Output)+_([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString()) || Pattern.matches("^(Output)+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString())) {
                    String splitter[] = pathName.getFileName().toString().split("_");
                    allYTValidations(pathName.toString(), pathName.getFileName().toString(), splitter);

                } else {
                    summaryDetails.add("Artifact : {" + pathName.getFileName() + "}  Path : {" + pathName.toString() + "} need to be under 'Video Output' folder or file starting with 'Output_' \n or Check the date format (dd-MMM-yyyy) of the file");
                    System.out.println("\tArtifact : {" + pathName.getFileName() + "}  Path : {" + pathName.toString() + "} need to  be under Video output folder or file starting with Output_ \n \t\t\t\tor Check the date format (dd-MMM-yyyy) of the file");

                }

            } else {
                System.out.println("\nFile Under the Given Path : " + pathName + "\n\tInvalid File");

            }
        }
    }


    private void allYTValidations(String pathName, String fileName, String splitName[]) {

        String fileLanguageName = null, fileNameCheck = null, fileOptionalCode = null;
        if (Pattern.matches("^(Output)+_([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            fileOptionalCode = splitName[1];
            fileNameCheck = splitName[2];
            fileLanguageName = splitName[3];

        } else if (Pattern.matches("^(Output)+_", fileName)) {
            fileNameCheck = splitName[1];
            fileLanguageName = splitName[2];

        } else if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            fileOptionalCode = splitName[0];
            fileNameCheck = splitName[1];
            fileLanguageName = splitName[2];

        } else {
            fileNameCheck = splitName[0];
            fileLanguageName = splitName[1];

        }

        String finalFileLanguageName = fileLanguageName;
        boolean language = languages.stream().anyMatch(l -> l.equals(finalFileLanguageName));
        if (!language) {
            summaryDetails.add("Artifact : {" + fileName + "}  has the language : {" + fileLanguageName + "} which doesn't match with any language");
            System.out.println("\tArtifact : {" + fileName + "}  has the language : {" + fileLanguageName + "} which doesn't match with any language");
        }

        fileNameValidation(fileNameCheck);
        fileUppercaseValidation(fileNameCheck);

        if (!StringUtils.isEmpty(fileOptionalCode)) {
            languageCodeValidator(fileOptionalCode, fileLanguageName, fileName);
        }
    }

    private void languageCodeValidator(String fileOptionalCode, String fileLanguageName, String fileName) {
        if (Pattern.matches("^(Output)+_.*$", fileName)) {
            if (!Pattern.matches("^.*(SGC|IF)+(" + languageMap.get(fileLanguageName) + ").*$", fileName)) {
                summaryDetails.add("Language Code is incorrect in the Name : {" + fileOptionalCode + "} compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
                System.out.println("\tLanguage Code is incorrect in the Name : {" + fileOptionalCode + "}  compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
            }
        } else if (!Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            summaryDetails.add("Language Code is incorrect in the Name : {" + fileOptionalCode + "} compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
            System.out.println("\tLanguage Code is incorrect in the Name : {" + fileOptionalCode + "}  compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
        }

    }

    private void allNONYTValidations(String fileName, String splitName[]) {
        String fileLanguageCode, fileNameCheck;

        if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            fileNameCheck = splitName[1];
            fileLanguageCode = splitName[2];

        } else {
            fileNameCheck = splitName[0];
            fileLanguageCode = splitName[1];

        }
        fileNameValidation(fileNameCheck);
        fileUppercaseValidation(fileNameCheck);

        String finalFileLanguageCode = fileLanguageCode;
        boolean language = languageCode.stream().anyMatch(l -> l.equalsIgnoreCase(finalFileLanguageCode));
        if (!language) {
            summaryDetails.add("Artifact : {" + fileName + "}  has the language : {" + fileLanguageCode + "} which doesn't match with any language");
            System.out.println("\tArtifact : {" + fileName + "}  has the language : {" + fileLanguageCode + "} which doesn't match with any language");
        }

    }

}
      