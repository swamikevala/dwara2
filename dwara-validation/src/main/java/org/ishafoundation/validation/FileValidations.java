package org.ishafoundation.validation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
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

    int pathNameCount = 0, pathNameFailedValidation = 0, failedValidations = 0;
    public Set<String> languages = new HashSet<>();
    public Set<String> languageCode = new HashSet<>();
    Map<Map<String, String>, Map<String, String>> folderFileSummary = new HashMap<>();
    Map<String, String> folderSummary = new HashMap<>();
    Map<String, String> fileSummary = new HashMap<>();
    //List<String> folderErrorSummary = new ArrayList<>();
    StringBuilder fileError = new StringBuilder();
    StringBuilder folderError = new StringBuilder();
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
        System.out.println("---------------------- Validating the Backup File ---------------------- ");
        String fileLanguageName = null, fileNameCheck = null, fileOptionalCode = null;
        try {
            List<Path> pathNames = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            if (pathNames.size() == 0) {
                System.out.println("\nThe given path doesn't have any files");
            }
            for (Path pathName : pathNames) {
                if (Pattern.matches("^.*\\.(mp4|mov)", pathName.toString())) {
                    pathNameCount++;
                    System.out.println("\n File Name Validation in the path : {" + pathName.toString() + "}");
                    String fileName = pathName.getFileName().toFile().getName().split("\\.")[0];
                    String splitName[] = pathName.getFileName().toString().split("_");
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
                        if(splitName.length == 2) {
                            fileNameCheck = splitName[0];
                            fileLanguageName = splitName[1];
                        } else {
                            System.out.println("\n Invalid File");
                            fileNameCheck = splitName[0];
                        }

                    }

                    String finalFileLanguageName = fileLanguageName;
                    boolean language = languages.stream().anyMatch(l -> l.equals(finalFileLanguageName));
                    if (!language) {
                        System.out.println("\tArtifact : {" + fileName + "}  has the language : {" + fileLanguageName + "} which doesn't match with any language");
                        pathNameFailedValidation += 1;
                    }

                    fileNameValidation(fileNameCheck, fileName);
                    fileUppercaseValidation(fileNameCheck, fileName);
                    datePatternValidation(fileNameCheck, fileName);

                    if (!StringUtils.isEmpty(fileOptionalCode)) {
                        languageCodeValidator(fileOptionalCode, fileLanguageName, fileName);
                    }


                } /*else {
                    System.out.println("\nFile Under the Given Path : " + pathName + "\n\tInvalid File");
                }*/
                if(pathNameFailedValidation > 0) {
                    failedValidations += 1;
                    pathNameFailedValidation = 0;
                }
            }
            System.out.println("\n\n \t\tTotal No. of Files : " + pathNameCount + " \t Validation Failed for No. of Files : " + failedValidations);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void fileNameValidation(String fileName, String file) {

        String regexAllowedChrsInFileName = "[\\w-.]*";
        Pattern allowedCharInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);

        if (fileName.length() > 245) { // 245 because we need to add sequence number
            if (Pattern.matches("^.*\\.(mp4|mov|arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", file)) {
                //System.out.println("\tFile Name: {" + file + "} is more than 245 characters");
                //fileErrorSummary.add("\tFile Name: {" + file + "} is more than 245 characters");
                fileError.append("\n\tFile Name: {" + file + "} is more than 245 characters");
            } else {
                //System.out.println("\tFolder Name: {" + fileName + "} is more than 245 characters");
                folderError.append("\n\tFolder Name: {" + fileName + "} is more than 245 characters");
            }
            pathNameFailedValidation += 1;
            /*if(FilenameUtils.getExtension(file).equals("mp4") || FilenameUtils.getExtension(file).equals("mov")) {
                System.out.println("\tFile Name: {" + file + "} is more than 245 characters");
            } */
            //logger.error("Artifact Name is more than 245 characters");
        }

        Matcher m = allowedCharInFileNamePattern.matcher(fileName);
        if (!m.matches()) {
            if (Pattern.matches("^.*\\.(mp4|mov|arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", file)) {
                //System.out.println("\tFile Name : {" + file + "} contains special characters");
                //fileErrorSummary.add("\tFile Name : {" + file + "} contains special characters");
                fileError.append("\n\t\t\t\tFile Name : {" + file + "} contains special characters");
            } else {
                //System.out.println("\tFolder Name : {" + fileName + "} contains special characters");
                folderError.append("\n\t\t\t\tFolder Name : {" + fileName + "} contains special characters");
            }
            pathNameFailedValidation += 1;
            /*if(FilenameUtils.getExtension(file).equals("mp4") || FilenameUtils.getExtension(file).equals("mov")) {
                System.out.println("\tFile Name : {" + file + "} contains special characters");
            }*/
            //logger.error("Artifact Name : {" + fileName + "} contains special characters");
        }

        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            decoder.decode(ByteBuffer.wrap(fileName.getBytes()));
        } catch (CharacterCodingException ex) {
            if (Pattern.matches("^.*\\.(mp4|mov|arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", file)) {
              //  System.out.println("\tFile Name : {" + file + "} contains non-unicode characters");
                //fileErrorSummary.add("\tFile Name : {" + file + "} contains non-unicode characters");
                fileError.append("\n\t\t\t\tFile Name : {" + file + "} contains non-unicode characters");
            } else {
                //System.out.println("\tFolder Name : {" + fileName + "} contains special characters");
                folderError.append("\n\t\t\t\tFolder Name : {" + fileName + "} contains special characters");
            }
            pathNameFailedValidation += 1;
            /*if(FilenameUtils.getExtension(file).equals("mp4") || FilenameUtils.getExtension(file).equals("mov")) {
                System.out.println("\tFile Name : {" + file + "} contains non-unicode characters");
            } */
            //logger.error("Artifact Name : {" + fileName + "} contains non-unicode characters");

        }

       /* Pattern whitespace = Pattern.compile("\\s");
        Matcher matcher = whitespace.matcher(file);
        if (!matcher.matches()) {
            if (Pattern.matches("^.*\\.(mp4|mov|arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", file)) {
                System.out.println("\tFile Name : {" + file + "} contains space in between");
            } else {
                System.out.println("\tFolder Name : {" + fileName + "} contains space in between");
            }
            pathNameFailedValidation += 1;
        }*/

    }


    @Override
    public void fileUppercaseValidation(String fileName, String file) {
        String[] nameSplit = fileName.split("-");
        for (String name : nameSplit) {
            if (!StringUtils.isAllUpperCase(name.subSequence(0, 1))) {
                if (Pattern.matches("^.*\\.(mp4|mov|arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", file)) {
                    //System.out.println("\tIn File : {" + file +"} the name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
                    //fileErrorSummary.add("\tIn File : {" + file +"} the name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
                    fileError.append("\n\t\t\t\tIn File : {" + file +"} the name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
                } else {
                    //System.out.println("\tThe Folder :{" + file + "}name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
                    folderError.append("\n\t\t\t\tThe Folder :{" + file + "}name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
                }
                pathNameFailedValidation += 1;
                /*if(FilenameUtils.getExtension(file).equals("mp4") || FilenameUtils.getExtension(file).equals("mov")) {
                    System.out.println("\tIn File : {" + file +"} the name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
                }*/
                //logger.error("In the File : {" + fileName + "} The first letters of words in name : {" + name + "} are not in UPPERCASE");

            }

        }

    }

    public void photoFileCodeUppercaseValidation(String fileName, String file) {
            if (!StringUtils.isAllUpperCase(fileName)) {
                if (Pattern.matches("^.*\\.(arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", file)) {
                    //System.out.println("\tIn File : {" + file +"} the letters of the name : {" + fileName + "} are not in UPPERCASE");
                    //fileErrorSummary.add("\tIn File : {" + file +"} the letters of the name : {" + fileName + "} are not in UPPERCASE");
                    fileError.append("\n\t\t\t\tIn File : {" + file +"} the letters of the name : {" + fileName + "} are not in UPPERCASE");
                } else {
                    //System.out.println("\tThe Folder :{" + file + "} the letters of the name : {" + fileName + "} are not in UPPERCASE");
                    folderError.append("\n\t\t\t\tThe Folder :{" + file + "} the letters of the name : {" + fileName + "} are not in UPPERCASE");
                }
                pathNameFailedValidation += 1;
                /*if(FilenameUtils.getExtension(file).equals("mp4") || FilenameUtils.getExtension(file).equals("mov")) {
                    System.out.println("\tIn File : {" + file +"} the name : {" + fileName + "} the first letters of words {" + name + "} are not in UPPERCASE");
                }*/
                //logger.error("In the File : {" + fileName + "} The first letters of words in name : {" + name + "} are not in UPPERCASE");

            }

        }


    public void datePatternValidation(String fileName, String file) {
             Pattern datePattern = Pattern.compile("^(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|JAN|Feb|FEB|Mar|MAR|Apr|APR|May|MAY|Jun|JUN|Jul|JUL|Aug|AUG|Sep|SEP|Oct|OCT|Nov|NOV|Dec|DEC)-[0-9]{4}$");
             Matcher m1 = datePattern.matcher(fileName);
             if (!m1.matches()) {
                 if (Pattern.matches("^.*\\.(mp4|mov)", file)) {
                     //System.out.println("\tFile Name : {" + file + "} date should be in the format dd-MMM-yyyy");
                     //fileErrorSummary.add("\tFile Name : {" + file + "} date should be in the format dd-MMM-yyyy");
                     fileError.append("\n\t\t\t\tFile Name : {" + file + "} date should be in the format dd-MMM-yyyy");
                 } else if (Pattern.matches("^.*\\.(arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", file)) {
                     //System.out.println("\tFile Name : {" + file + "} date should be in the format yyyyMMdd");
                     //fileErrorSummary.add("\tFile Name : {" + file + "} date should be in the format yyyyMMdd");
                     fileError.append("\n\t\t\t\tFile Name : {" + file + "} date should be in the format yyyyMMdd");
                 } else {
                     //System.out.println("\tFolder Name : {" + fileName + "} date format is incorrect");
                     folderError.append("\n\t\t\t\tFolder Name : {" + fileName + "} date format is incorrect");
                 }
                 pathNameFailedValidation += 1;
                 /*if(FilenameUtils.getExtension(file).equals("mp4") || FilenameUtils.getExtension(file).equals("mov")) {
                     System.out.println("\tFile Name : {" + file + "} date should be in the format dd-MMM-yyyy");
                 }*/
                  //logger.error("Artifact Name : {" + fileName + "} date should be in the format dd-MMM-yyyy pattern");

             }
    }

    public Map validateFilesInPath(String path) {

        try {
            List<Path> pathNames = Files.walk(Paths.get(path))
                    .filter(Files::isDirectory).filter(Files::isWritable)
                    .collect(Collectors.toList());
            //findAllValidationsInThePath(pathNames);
            //findAllValidationsInTheFolder(pathNames);
            return findAllValidations(pathNames, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
            return Collections.emptyMap();
    }

    public Map validatePhotoFoldersInPath(String path) {
        System.out.println("------------------------------- Validating the Photo Folders in the provided path -------------------------------");
        try {
            List<Path> pathNames = Files.walk(Paths.get(path))
                    .filter(Files::isDirectory).filter(Files::isWritable)
                    .collect(Collectors.toList());
            /*List<Path> filePathNames = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile).filter(Files::isWritable)
                    .collect(Collectors.toList());*/
            return findAllPhotoValidationsInThePath(pathNames);//, filePathNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    private Map findAllPhotoValidationsInThePath(List<Path> pathNames) { //, List<Path> filePathNames) throws IOException {
        if (pathNames.size() == 0) {
            System.out.println("\nThe given path doesn't have any Folders");
        } else {
            for (Path pathName : pathNames) {
                File file = new File(pathName.toFile().getAbsolutePath());
                if(file.isDirectory()) {
                    boolean name = Arrays.stream(file.listFiles()).filter(rFile-> !rFile.isDirectory()).filter(extFile -> Pattern.matches("^.*\\.(arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", extFile.getName())).findAny().isPresent();
                    if (name) {
                        folderError.delete(0, folderError.length());

                        //pathNameCount++;
                        //System.out.println("\n\nFolder Validated in the path : " + pathName);
                        if (Pattern.matches("^([0-9]{4})(([0-1][0-9]))(([3][0-1])|([0-2][0-9]))+_([A-Z]{3})+_([a-zA-Z]+(-[a-zA-Z]+)+)", pathName.getFileName().toString())) {
                            String splitter[] = pathName.getFileName().toString().split("_");
                            if (splitter.length == 3) {

                                fileNameValidation(splitter[2], pathName.getFileName().toString());
                                photoFileCodeUppercaseValidation(splitter[1], pathName.getFileName().toString());
                                fileUppercaseValidation(splitter[2], pathName.getFileName().toString());
                                photoDirectoryDatePatternValidation(splitter[0], pathName.getFileName().toString());
                            } else {
                                //System.out.println("\tFolder Name : {" + pathName.getFileName().toString() + "} in the Path : {" + pathName.toString() + " is not in the format yyyyMMdd_XXX_dddd");
                                folderError.append("\tFolder Name : {" + pathName.getFileName().toString() + "} in the Path : {" + pathName.toString() + " is not in the format yyyyMMdd_XXX_dddd");
                                //failedValidations++;
                            }
                            /*if(pathNameFailedValidation > 0) {
                                pathNameFailedValidation = 0;
                                failedValidations++;
                            }*/
                        } else {
                            //System.out.println("\tFolder Name : {" + pathName.getFileName().toString() + "} in the Path : {" + pathName.toString() + "} is not in the format yyyyMMdd_XXX_dddd");
                            folderError.append("\tFolder Name : {" + pathName.getFileName().toString() + "} in the Path : {" + pathName.toString() + "} is not in the format yyyyMMdd_XXX_dddd");
                            //failedValidations++;
                        }

                        for (File photoFile: file.listFiles()) {
                            if (!photoFile.isDirectory()) {
                                fileError.delete(0, fileError.length());
                                //pathNameCount++;
                                //System.out.println("\n\nFile Validated in the path : " + photoFile.getPath());
                                if (Pattern.matches("^.*\\.(arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", photoFile.getName())) {
                                    if (Pattern.matches("^([0-9]{4})(([0-1][0-9]))(([3][0-1])|([0-2][0-9]))+_([A-Z]{3})+_([0-9]{4})+\\.[a-zA-Z]+$", photoFile.getName())) {
                                        String splitter[] = photoFile.getName().split("_");
                                        if (splitter.length == 3) {
                                            fileNameValidation(splitter[2], photoFile.getName());
                                            photoFileCodeUppercaseValidation(splitter[1], photoFile.getName());
                                            photoDirectoryDatePatternValidation(splitter[0], photoFile.getName());
                                        } else {
                                            //System.out.println("\tFile Name : {" + photoFile.getName() + "} in the Path : {" + photoFile.getPath() + " is not in the format yyyyMMdd_XXX_dddd");
                                            fileError.append("\tFile Name : {" + photoFile.getName() + "} in the Path : {" + photoFile.getPath() + " is not in the format yyyyMMdd_XXX_dddd");
                                            //failedValidations++;
                                        }
                                        /*if (pathNameFailedValidation > 0) {
                                            pathNameFailedValidation = 0;
                                            failedValidations++;
                                        }*/
                                    } else {
                                        //System.out.println("\tFile Name : {" + photoFile.getName() + "} in the Path : {" + photoFile.getPath() + "} is not in the format yyyyMMdd_XXX_dddd");
                                        fileError.append("\tFile Name : {" + photoFile.getName() + "} in the Path : {" + photoFile.getPath() + "} is not in the format yyyyMMdd_XXX_dddd");
                                        //failedValidations++;
                                    }
                                } else {
                                    //System.out.println("\tInvalid File");
                                    fileError.append("\tInvalid File");
                                    //failedValidations++;
                                }
                                fileSummary.put("FolderPath : " + pathName.toFile().getPath() + "  |  " + "FileName : " + photoFile.getName(), fileError.toString());
                            }

                        }


                    }

                }
                folderSummary.put("FolderPath : " +  pathName.toFile().getPath() + "\t\tFolderName : " + pathName.toFile().getName(), folderError.toString());
            }
            folderFileSummary.put(folderSummary, fileSummary);
        }
            /*for (Path filePathName : filePathNames) {
                pathNameCount++;
                if (Pattern.matches("^.*\\.(arw|cr2|cr3|crw|dng|fff|heic|jpg|jpeg|nef|nrw|png|psd|sr2|srf|tif|xmp)", filePathName.getFileName().toString())) {
                    System.out.println("\n\nFile Validated in the path : " + filePathName);
                    if (Pattern.matches("^[0-9]{4}(([0-9])|([0-1][0-9]))(([0-9])|([0-2][0-9])|([3][0-1]))+_[A-Z]+_.*", filePathName.getFileName().toString())) {
                        String splitter[] = filePathName.getFileName().toString().split("_");
                        if (splitter.length == 3) {
                            fileNameValidation(splitter[2], filePathName.getFileName().toString());
                            fileUppercaseValidation(splitter[1], filePathName.getFileName().toString());
                            photoDirectoryDatePatternValidation(splitter[0], filePathName.getFileName().toString());
                        } else {
                            System.out.println("\tFile Name : {" + filePathName.getFileName().toString() + "} in the Path : {" + filePathName.toString() + " is not in the format yyyyMMdd_XXX_dddd");
                            failedValidations++;
                        }
                        if(pathNameFailedValidation > 0) {
                            pathNameFailedValidation = 0;
                            failedValidations++;
                        }
                    } else {
                        System.out.println("\tFile Name : {" + filePathName.getFileName().toString() + "} in the Path : {" + filePathName.toString() + "} is not in the format yyyyMMdd_XXX_dddd");
                        failedValidations++;
                    }

                }

            }*/
        //System.out.println("\n\n \t\tTotal No. of Files : " + pathNameCount + " \t Validation Failed for No. of Files : " + failedValidations);
        return folderFileSummary;
    }

    public void photoDirectoryDatePatternValidation(String fileName, String file) {
        Pattern datePattern = Pattern.compile("^([0-9]{4})(([0-1][0-9]))(([3][0-1])|([0-2][0-9]))$");
        Matcher m1 = datePattern.matcher(fileName);
        if (!m1.matches()) {
            System.out.println("\n\tFolder Name : {" + file + "} date should be in the format dd-MMM-yyyy");
        }
    }

    private void findAllValidationsInThePath(List<Path> pathNames) throws FileNotFoundException {

        if (pathNames.size() == 0) {
            System.out.println("\nThe given path doesn't have any files");
        } else {
            for (Path pathName : pathNames) {
                pathNameCount++;
                System.out.println("\n\nFile Validated in the path : " + pathName);

                if (Pattern.matches("^.*\\.(mp4|mov)", pathName.toString())) {
                    //System.out.println("\n\nFile Validated in the path : " + pathName);
                    //pathNameCount++;
                    long fileSize = FileUtils.sizeOf(pathName.toFile());
                    if (fileSize > 1024 ) {
                        if (Pattern.matches("^.*_(DUB|SUB)+_.*$", pathName.getFileName().toString())) {
                            if (Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|JAN|Feb|FEB|Mar|MAR|Apr|APR|May|MAY|Jun|JUN|Jul|JUL|Aug|AUG|Sep|SEP|Oct|OCT|Nov|NOV|Dec|DEC)-[0-9]{4}.*$", pathName.getFileName().toString()) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString())) {
                                String splitter[] = pathName.getFileName().toString().split("_");
                                allNONYTValidations(pathName.getFileName().toString(), splitter);
                            } else {
                                String splitter[] = pathName.getFileName().toString().split("_");
                                nonYTValidationsFailed(pathName.getFileName().toString(), splitter, pathName);
                                //System.out.println("\tArtifact : {" + pathName.getFileName() + "}  Path : {" + pathName.toString() + "} is not named properly.\n \t\t\t or Check the date format (dd-MMM-yyyy) of the file");
                            }
                            if(pathNameFailedValidation > 0) {
                                failedValidations++;
                                pathNameFailedValidation = 0;
                            }
                        } else if (Pattern.matches("^.*\\\\(Video Output)\\\\.*$", pathName.toString())) {
                            if (Pattern.matches("^.*\\\\([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}\\\\(Video Output)\\\\.*$", pathName.toString()) || Pattern.matches("^.*\\\\([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}\\\\(Video Output)\\\\.*$", pathName.toString())) {
                                String splitter[] = pathName.getFileName().toString().split("_");
                                String splitSlash[] = pathName.toString().split("\\\\");
                                String pathFileName = null;
                                for (int i = 0; i < splitSlash.length; i++) {
                                    if (splitSlash[i].equals("Video Output")) {
                                        pathFileName = splitSlash[i - 1];
                                    }
                                }
                                String splitPathName[] = pathFileName.split("_");
                                if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}", pathFileName) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}", pathFileName)) {
                                    allYTValidations(pathFileName, splitPathName);

                                } else {
                                    ytValidationsFailed(pathFileName, splitPathName, pathName);
                                }
                                /*if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString()) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString())) {
                                    allYTValidations(pathName.getFileName().toString(), splitter);

                                } else {
                                    ytValidationsFailed(pathName.getFileName().toString(), splitter);
                                }*/
                                if(pathNameFailedValidation > 0) {
                                    failedValidations++;
                                    pathNameFailedValidation = 0;
                                }
                                System.out.println("\n\tFile Validated : " + pathName.getFileName().toString() + " in the Path : " + pathName);
                                pathNameCount +=1;
                                fileNameValidation(pathName.getFileName().toString(), pathName.getFileName().toString());

                            } else {

                                String splitSlash[] = pathName.toString().split("\\\\");
                                String pathFileName = null;
                                for (int i = 0; i < splitSlash.length; i++) {
                                    if (splitSlash[i].equals("Video Output")) {
                                        pathFileName = splitSlash[i - 1];
                                    }
                                }
                                String splitter[] = pathFileName.split("_");
                                if (splitter.length > 2) {
                                    ytValidationsFailed(pathFileName, splitter, pathName);
                                } else {
                                    System.out.println("\tFolder : {" + pathFileName + "} in the Path : {" + pathName.toString() + "} the language is not present or is not at right position.");
                                    pathNameFailedValidation ++;
                                }
                                if(pathNameFailedValidation > 0) {
                                    failedValidations ++;
                                    pathNameFailedValidation = 0;
                                }

                                System.out.println("\n\tFile Validated : " + pathName.getFileName().toString() + " in the Path : " + pathName);
                                pathNameCount +=1;
                                String fileSplitter[] = pathName.getFileName().toString().split("_");
                                ytValidationsFailed(pathName.getFileName().toString(), fileSplitter, pathName);

                            }
                            if(pathNameFailedValidation > 0) {
                                failedValidations ++;
                                pathNameFailedValidation = 0;
                            }
                        } else if (Pattern.matches("^(Output)+_([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", pathName.getFileName().toString()) || Pattern.matches("^(Output)+_([a-zA-Z]+(-[a-zA-Z]+)+)_.*$", pathName.getFileName().toString())) {
                            String splitter[] = pathName.getFileName().toString().split("_");
                            if (Pattern.matches("^(Output)+_([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString()) || Pattern.matches("^(Output)+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString())) {
                                allYTValidations(pathName.getFileName().toString(), splitter);
                            } else {
                                ytValidationsFailed(pathName.getFileName().toString(), splitter, pathName);
                            }
                            if(pathNameFailedValidation > 0) {
                                failedValidations ++;
                                pathNameFailedValidation = 0;
                            }

                        } else {
                            System.out.println("\tPath : {" + pathName.toString() + "} Artifact : {" + pathName.getFileName() + "} need to  be under 'Video Output' folder or file should start with 'Output_'");
                            failedValidations ++;
                        }
                    } else {
                        System.out.println("\tSize of File : {" + pathName.getFileName().toString() + "} in the Path : {" + pathName.toString() + "} is less than 1 MB");
                        failedValidations ++;
                    }
                } else {
                    System.out.println("\tInvalid File");
                    failedValidations ++;
                }

            }
            System.out.println("\n\n \t\tTotal No. of Files : " + pathNameCount + " \t Validation Failed for No. of Files : " + failedValidations);
        }
    }





    private void findAllValidationsInTheFolder(List<Path> pathNames) throws FileNotFoundException {

        if (pathNames.size() == 0) {
            System.out.println("\nThe given path doesn't have any files");
        } else {
            for (Path pathName : pathNames) {
                pathNameCount++;
                if (Pattern.matches("^.*\\.(mp4|mov)", pathName.toString())) {
                    long fileSize = FileUtils.sizeOf(pathName.toFile());
                    if (fileSize > 1024 ) {
                        if (Pattern.matches("^.*\\\\(Video Output)\\\\.*$", pathName.toString())) {
                            System.out.println("\n\nFolder Validated of the path : " + pathName);
                            String splitSlash[] = pathName.toString().split("\\\\");
                            String pathFileName = null;
                            for (int i = 0; i < splitSlash.length; i++) {
                                if (splitSlash[i].equals("Video Output")) {
                                    pathFileName = splitSlash[i - 1];
                                }
                            }
                            String splitPathName[] = pathFileName.split("_");
                            if (Pattern.matches("^.*\\\\([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}\\\\(Video Output)\\\\.*$", pathName.toString()) || Pattern.matches("^.*\\\\([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}\\\\(Video Output)\\\\.*$", pathName.toString())) {
                                if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}", pathFileName) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}", pathFileName)) {
                                    allYTValidations(pathFileName, splitPathName);

                                } else {
                                    ytValidationsFailed(pathFileName, splitPathName, pathName);
                                }
                                if(pathNameFailedValidation > 0) {
                                    failedValidations++;
                                    pathNameFailedValidation = 0;
                                }
                                System.out.println("\n\tFile Validated : " + pathName.getFileName().toString() + " in the Path : " + pathName);
                                pathNameCount +=1;
                                fileNameValidation(pathName.getFileName().toString(), pathName.getFileName().toString());

                            } else if (Pattern.matches("^.*_(DUB|SUB)+_.*$", pathName.toString())) {
                                if (Pattern.matches("^.*\\\\([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|JAN|Feb|FEB|Mar|MAR|Apr|APR|May|MAY|Jun|JUN|Jul|JUL|Aug|AUG|Sep|SEP|Oct|OCT|Nov|NOV|Dec|DEC)-[0-9]{4}\\\\(Video Output)\\\\.*$", pathName.toString()) || Pattern.matches("^.*\\\\([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}\\\\(Video Output)\\\\.*$", pathName.toString())) {
                                    if (Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|JAN|Feb|FEB|Mar|MAR|Apr|APR|May|MAY|Jun|JUN|Jul|JUL|Aug|AUG|Sep|SEP|Oct|OCT|Nov|NOV|Dec|DEC)-[0-9]{4}", pathFileName) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}$", pathFileName)) {
                                        allNONYTValidations(pathFileName, splitPathName);
                                    } else {
                                        nonYTValidationsFailed(pathFileName, splitPathName, pathName);
                                    }
                                } else {
                                    if (splitPathName.length > 2) {
                                        nonYTValidationsFailed(pathFileName, splitPathName, pathName);
                                    } else {
                                        System.out.println("\tFolder : {" + pathFileName + "} in the Path : {" + pathName.toString() + "} the language is not present or is not at right position.");
                                        pathNameFailedValidation ++;
                                    }
                                }
                                if(pathNameFailedValidation > 0) {
                                    failedValidations++;
                                    pathNameFailedValidation = 0;
                                }
                                System.out.println("\n\tFile Validated : " + pathName.getFileName().toString() + " in the Path : " + pathName);
                                pathNameCount ++;
                                fileNameValidation(pathName.getFileName().toString(), pathName.getFileName().toString());

                            } else {
                                if (splitPathName.length > 2) {
                                    ytValidationsFailed(pathFileName, splitPathName, pathName);
                                } else {
                                    System.out.println("\tFolder : {" + pathFileName + "} in the Path : {" + pathName.toString() + "} the language is not present or is not at right position.");
                                    pathNameFailedValidation ++;
                                }
                                if(pathNameFailedValidation > 0) {
                                    failedValidations ++;
                                    pathNameFailedValidation = 0;
                                }

                                System.out.println("\n\tFile Validated : " + pathName.getFileName().toString() + " in the Path : " + pathName);
                                pathNameCount ++;
                                fileNameValidation(pathName.getFileName().toString(), pathName.getFileName().toString());

                            }
                            if(pathNameFailedValidation > 0) {
                                failedValidations++;
                                pathNameFailedValidation = 0;
                            }

                        } else if (Pattern.matches("^(Output)+_([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", pathName.getFileName().toString()) || Pattern.matches("^(Output)+_([a-zA-Z]+(-[a-zA-Z]+)+)_.*$", pathName.getFileName().toString())) {
                            System.out.println("\n\nFile Validated in the path : " + pathName);
                            String splitter[] = pathName.getFileName().toString().split("_");
                            if (Pattern.matches("^(Output)+_([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString()) || Pattern.matches("^(Output)+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}.*$", pathName.getFileName().toString())) {
                                allYTValidations(pathName.getFileName().toString(), splitter);
                            } else {
                                ytValidationsFailed(pathName.getFileName().toString(), splitter, pathName);
                            }
                            if(pathNameFailedValidation > 0) {
                                failedValidations ++;
                                pathNameFailedValidation = 0;
                            }

                        } else {
                            System.out.println("\n\nFile Validated in the path : " + pathName);
                            System.out.println("\tPath : {" + pathName.toString() + "} Artifact : {" + pathName.getFileName() + "} need to  be under 'Video Output' folder or file should start with 'Output_'");
                            failedValidations ++;
                        }
                    } else {
                        System.out.println("\tSize of File : {" + pathName.getFileName().toString() + "} in the Path : {" + pathName.toString() + "} is less than 1 MB");
                        failedValidations ++;
                    }
                } else {
                    System.out.println("\n\nFile Validated in the path : " + pathName + "\n\tInvalid File");
                    failedValidations ++;
                }

            }
            System.out.println("\n\n \t\tTotal No. of Files : " + pathNameCount + " \t Validation Failed for No. of Files : " + failedValidations);
        }
    }



    private Map findAllValidations(List<Path> pathNames, String sourcePath) throws FileNotFoundException {

        if (pathNames.size() == 0) {
        } else {
            for (Path pathName : pathNames) {
                folderError.delete(0, folderError.length());

                if (Pattern.matches("^.*\\\\(Video Output)$", pathName.toString())) {

                    String splitSlash[] = pathName.toString().split("\\\\");
                    String pathFileName = null;
                    for (int i = 0; i < splitSlash.length; i++) {
                        if (splitSlash[i].equals("Video Output")) {
                            pathFileName = splitSlash[i - 1];
                        }
                    }
                    String splitPathName[] = pathFileName.split("_");
                    if (Pattern.matches("^.*\\\\([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}\\\\(Video Output)$", pathName.toString()) || Pattern.matches("^.*\\\\([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}\\\\(Video Output)$", pathName.toString())) {
                        if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}", pathFileName) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}", pathFileName)) {
                            allYTValidations(pathFileName, splitPathName);

                        } else {
                            ytValidationsFailed(pathFileName, splitPathName, pathName);
                        }

                        if(!(pathName.toFile().listFiles().length > 0)) {
                            folderError.append("\n\t\t\t\tPath : {" + pathName + " } doesn't have any files.");
                        } else {

                            fileValidationOfVideOutput(pathName);
                        }

                    } else if (Pattern.matches("^.*_(DUB|SUB)+_.*$", pathName.toString())) {
                        if (Pattern.matches("^.*\\\\([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|JAN|Feb|FEB|Mar|MAR|Apr|APR|May|MAY|Jun|JUN|Jul|JUL|Aug|AUG|Sep|SEP|Oct|OCT|Nov|NOV|Dec|DEC)-[0-9]{4}\\\\(Video Output)\\\\.*$", pathName.toString()) || Pattern.matches("^.*\\\\([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}\\\\(Video Output)\\\\.*$", pathName.toString())) {
                            if (Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|JAN|Feb|FEB|Mar|MAR|Apr|APR|May|MAY|Jun|JUN|Jul|JUL|Aug|AUG|Sep|SEP|Oct|OCT|Nov|NOV|Dec|DEC)-[0-9]{4}", pathFileName) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(DUB|SUB)+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}$", pathFileName)) {
                                allNONYTValidations(pathFileName, splitPathName);
                            } else {
                                nonYTValidationsFailed(pathFileName, splitPathName, pathName);
                            }
                        } else {
                            if (splitPathName.length > 2) {
                                nonYTValidationsFailed(pathFileName, splitPathName, pathName);
                            } else {
                                //System.out.println("\tFolder : {" + pathFileName + "} in the Path : {" + pathName.toString() + "} the language is not present or is not at right position.");
                                folderError.append("\n\t\t\t\tFolder : {" + pathFileName + "} in the Path : {" + pathName.toString() + "} the language is not present or is not at right position.");
                            }
                        }

                        if(!(pathName.toFile().listFiles().length > 0)) {
                            folderError.append("\n\t\t\t\tPath : {" + pathName + " } doesn't have any files.");
                        } else {

                            fileValidationOfVideOutput(pathName);
                        }

                    } else {
                        if (splitPathName.length > 2) {
                            ytValidationsFailed(pathFileName, splitPathName, pathName);
                        } else {
                            folderError.append("\n\t\t\t\tFolder : {" + pathFileName + "} in the Path : {" + pathName.toString() + "} the language is not present or is not at right position.");
                        }

                        if(!(pathName.toFile().listFiles().length > 0)) {
                            folderError.append("\n\t\t\t\tPath : {" + pathName + " } doesn't have any files.");
                        } else {

                            fileValidationOfVideOutput(pathName);
                        }

                    }
                    folderSummary.put("FolderPath : " +  pathName.toFile().getPath() + "\n\tFolderName : " + pathFileName, folderError.toString());
                } else if (Pattern.matches("^.*\\\\([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}$", pathName.toString()) || Pattern.matches("^.*\\\\([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}$", pathName.toString())) {
                    String splitSlash[] = pathName.toString().split("\\\\");
                    String pathFileName = pathName.toString().substring(pathName.toString().lastIndexOf("\\")+1);

                    String splitPathName[] = pathFileName.split("_");
                    if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}", pathFileName) || Pattern.matches("^([a-zA-Z]+(-[a-zA-Z]+)+)_[a-zA-Z]+_(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}", pathFileName)) {
                        allYTValidations(pathFileName, splitPathName);

                    } else {
                        ytValidationsFailed(pathFileName, splitPathName, pathName);
                    }

                    if(!(pathName.toFile().listFiles().length > 0)) {
                        folderError.append("\n\t\t\t\tPath : {" + pathName + " } doesn't have any files.");
                    }

                    fileValidationOfOutput(pathName);

                    folderSummary.put("FolderPath : " +  pathName.toFile().getPath() + "\n\tFolderName : " + pathFileName, folderError.toString());

                } else {
                    if(!(pathName.toFile().listFiles().length > 0)) {
                        folderError.append("\n\t\t\t\tPath : {" + pathName + " } doesn't have any files.");
                    } else {
                        String pathFileName = pathName.toString().substring(pathName.toString().lastIndexOf("\\") + 1);
                        String splitPathName[] = pathFileName.split("_");

                        boolean outputFiles = Arrays.stream(pathName.toFile().listFiles()).anyMatch(file -> Pattern.matches("^.*\\.(mp4|mov)", file.getName()));

                        if(!pathName.getFileName().equals("Video Output")) {
                            if ((pathName.toFile().getPath().equals(sourcePath) && outputFiles) || outputFiles) {
                                ytValidationsFailed(pathFileName, splitPathName, pathName);
                            }
                        }
                        fileValidationOfOutput(pathName);
                    }
                    folderSummary.put("FolderPath : " +  pathName.toFile().getPath() + "\n\tFolderName : " + pathName.getFileName(), folderError.toString());
                }


            }
            folderFileSummary.put(folderSummary, fileSummary);
        }

        return folderFileSummary;
    }

    private void fileValidationOfVideOutput(Path pathName) {
        for(File videoFile : Objects.requireNonNull(pathName.toFile().listFiles())) {
            fileError.delete(0, fileError.length());
            if (!videoFile.isDirectory()) {
                if (Pattern.matches("^.*\\.(mp4|mov)", videoFile.getName())) {
                    long fileSize = FileUtils.sizeOf(pathName.toFile());
                    if (fileSize > 1024) {

                        fileNameValidation(videoFile.getName(), videoFile.getName());
                        //TODO ffmpeg test


                    } else {
                        fileError.append("\n\t\t\t\tSize of File : {" + videoFile.getName() + "} in the Path : {" + videoFile.getPath() + "} is less than 1 MB");
                        //failedValidations++;
                    }
                } else {
                    fileError.append("\n\t\t\t\tFile Validated in the path : " + videoFile.getPath() + "\t\tInvalid File");
                    //failedValidations++;
                }
                fileSummary.put("FolderPath : " + pathName.toFile().getPath() + "  |  " + "FileName : " + videoFile.getName(), fileError.toString());
            }
        }
    }


    private void fileValidationOfOutput(Path pathName) {

        for(File videoFile : Objects.requireNonNull(pathName.toFile().listFiles())) {
            fileError.delete(0, fileError.length());
            if (!videoFile.isDirectory()) {
                if (Pattern.matches("^.*\\.(mp4|mov)", videoFile.getName())) {
                    long fileSize = FileUtils.sizeOf(videoFile);
                    if (fileSize > 1024) {
                        if (Pattern.matches("^(Output)+_.*$", videoFile.getName())) {
                            fileNameValidation(videoFile.getName(), videoFile.getName());
                        } else {
                            fileError.append("\n\t\t\t\tPath : {" + videoFile.getPath() + "} Artifact : {" + videoFile.getName() + "} need to be under 'Video Output' folder or file should start with 'Output_'");
                            //failedValidations++;
                        }

                    } else {
                        fileError.append("\n\t\t\t\tSize of File : {" + videoFile.getName() + "} in the Path : {" + videoFile.getPath() + "} is less than 1 MB");
                        //failedValidations++;
                    }

                } else {
                    fileError.append("\n\t\t\t\tFile Validated in the path : " + videoFile.getPath() + "\t\tInvalid File");
                }
                fileSummary.put("FolderPath : " + pathName.toFile().getPath() + "  |  " + "FileName : " + videoFile.getName(), fileError.toString());

            }
        }
    }



    private void allYTValidations(String fileName, String splitName[]) {

        String fileLanguageName, fileNameCheck, fileOptionalCode = null, fileDate;
        if (Pattern.matches("^(Output)+_([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            fileOptionalCode = splitName[1];
            fileNameCheck = splitName[2];
            fileLanguageName = splitName[3];
            fileDate = splitName[4].split("\\.")[0];

        } else if (Pattern.matches("^(Output)+_", fileName)) {
            fileNameCheck = splitName[1];
            fileLanguageName = splitName[2];
            fileDate = splitName[3].split("\\.")[0];

        } else if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            fileOptionalCode = splitName[0];
            fileNameCheck = splitName[1];
            fileLanguageName = splitName[2];
            fileDate = splitName[3].split("\\.")[0];

        } else {
            fileNameCheck = splitName[0];
            fileLanguageName = splitName[1];
            fileDate = splitName[2].split("\\.")[0];

        }

        allRequiredValidations(fileNameCheck, fileName, fileDate, fileLanguageName, null);

        if (!StringUtils.isEmpty(fileOptionalCode)) {
            languageCodeValidator(fileOptionalCode, fileLanguageName, fileName);
        }
    }

    private void ytValidationsFailed(String fileName, String splitName[], Path pathName) {

        String fileLanguageName = null, fileNameCheck = null, fileDate = null, fileOptionalCode;
        if (Pattern.matches("^(Output)+_([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            if(splitName.length == 5) {
                fileOptionalCode = splitName[1];
                fileNameCheck = splitName[2];
                fileLanguageName = splitName[3];
                fileDate = splitName[4].split("\\.")[0];
                allRequiredValidations(fileNameCheck, fileName, fileDate, fileLanguageName, null);

                if (!StringUtils.isEmpty(fileOptionalCode)) {
                    languageCodeValidator(fileOptionalCode, fileLanguageName, fileName);
                }
            } else {
                folderError.append("\n\t\t\t\tFor {" + fileName + "} the language is not present or is not at right position.");
            }

        } else if (Pattern.matches("^(Output)+_", fileName)) {
            if(splitName.length == 4) {
                fileNameCheck = splitName[1];
                fileLanguageName = splitName[2];
                fileDate = splitName[3].split("\\.")[0];
                allRequiredValidations(fileNameCheck, fileName, fileDate, fileLanguageName, null);
            } else {
                folderError.append("\n\t\t\t\tFor {" + fileName + "} the language is not present or is not at right position.");
            }

        } else if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            if(splitName.length == 4) {
                fileOptionalCode = splitName[0];
                fileNameCheck = splitName[1];
                fileLanguageName = splitName[2];
                fileDate = splitName[3].split("\\.")[0];
                allRequiredValidations(fileNameCheck, fileName, fileDate, fileLanguageName, null);

                if (!StringUtils.isEmpty(fileOptionalCode)) {
                    languageCodeValidator(fileOptionalCode, fileLanguageName, fileName);
                }
            } else {
                folderError.append("\n\t\t\t\tFor {" + fileName + "} the language is not present or is not at right position.");

            }

        } else {
            if(splitName.length == 3) {
                fileNameCheck = splitName[0];
                fileLanguageName = splitName[1];
                fileDate = splitName[2].split("\\.")[0];
                allRequiredValidations(fileNameCheck, fileName, fileDate, fileLanguageName, null);
            } else {
                folderError.append("\n\t\t\t\tFor {" + fileName + "} the language is not present or is not at right position.");
            }

        }

    }

    private void languageCheckValidator (String fileName, String language) {
        boolean lang = languages.stream().anyMatch(l -> l.equals(language));
        if (!lang) {
            //System.out.println("\tArtifact : {" + fileName + "}  has the language : {" + language + "} which doesn't match with any language");
            folderError.append("\n\t\t\t\tArtifact : {" + fileName + "}  has the language : {" + language + "} which doesn't match with any language");
            pathNameFailedValidation += 1;
        }
    }

    private void languageCodeCheckValidator (String fileName, String language) {
        boolean lang = languageCode.stream().anyMatch(l -> l.equals(language.toLowerCase()));
        if (!lang) {
            //System.out.println("\tArtifact : {" + fileName + "}  has the language : {" + language + "} which doesn't match with any language");
            folderError.append("\n\t\t\t\tArtifact : {" + fileName + "}  has the language : {" + language + "} which doesn't match with any language");
            pathNameFailedValidation += 1;
        }
    }

    private void languageCodeValidator(String fileOptionalCode, String fileLanguageName, String fileName) {
        /*if (Pattern.matches("^(Output)+_.*$", fileName)) {
            if (!Pattern.matches("^.*(SGC|IF)+(" + languageMap.get(fileLanguageName) + ").*$", fileName)) {
                System.out.println("\tLanguage Code is incorrect in the Name : {" + fileOptionalCode + "}  compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
                pathNameFailedValidation += 1;
            }
        } else*/ if (!Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            //System.out.println("\tLanguage Code is incorrect in the Name : {" + fileOptionalCode + "}  compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
            folderError.append("\n\t\t\t\tLanguage Code is incorrect in the Name : {" + fileOptionalCode + "}  compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
            pathNameFailedValidation += 1;
        } else if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            if (!Pattern.matches("^(SGC|IF)+(" + languageMap.get(fileLanguageName) + ")+[YT].*$", fileName)) {
                //System.out.println("\tLanguage Code is incorrect in the Name : {" + fileOptionalCode + "}  compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
                folderError.append("\n\t\t\t\tLanguage Code is incorrect in the Name : {" + fileOptionalCode + "}  compared with Language Name : {" + fileLanguageName + "} in the ArtifactName : " + fileName);
                pathNameFailedValidation += 1;
            }
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
        fileNameValidation(fileNameCheck, fileName);
        fileUppercaseValidation(fileNameCheck, fileName);
        languageCodeCheckValidator(fileName, fileLanguageCode);

    }

    private void nonYTValidationsFailed(String fileName, String splitName[], Path pathName) {
        String fileLanguage = null, fileNameCheck, fileDate = null;

        if (Pattern.matches("^([A-Z]+[a-z]+[YT]+([0-9]*))+_.*$", fileName)) {
            if(splitName.length == 5) {
                fileNameCheck = splitName[1];
                fileLanguage = splitName[2];
                fileDate = splitName[4].split("\\.")[0];
                allRequiredValidations(fileNameCheck, fileName, fileDate, null, fileLanguage);

                if(!fileLanguage.equalsIgnoreCase(fileLanguage)) {
                    //System.out.println("\tArtifact : {" + fileName + "}  has the language : {" + fileLanguage + "} should be in UpperCase");
                    folderError.append("\n\t\t\t\tArtifact : {" + fileName + "}  has the language : {" + fileLanguage + "} should be in UpperCase");
                    pathNameFailedValidation++;
                }
            } else {
                //System.out.println("For FileName : {" + fileName + "} the language is not present or is not at right position.");
                folderError.append("\n\t\t\t\tFor {" + fileName + "} the language is not present or is not at right position.");
                pathNameFailedValidation++;
            }

        } else {
            if(splitName.length == 4) {
                fileNameCheck = splitName[0];
                fileLanguage = splitName[1];
                fileDate = splitName[3].split("\\.")[0];
                allRequiredValidations(fileNameCheck, fileName, fileDate, null, fileLanguage);

                if(!fileLanguage.equalsIgnoreCase(fileLanguage)) {
                    //System.out.println("\tArtifact : {" + fileName + "}  has the language : {" + fileLanguage + "} should be in UpperCase");
                    folderError.append("\n\t\t\t\tArtifact : {" + fileName + "}  has the language : {" + fileLanguage + "} should be in UpperCase");
                    pathNameFailedValidation++;
                }
            } else {
                /*if(pathName.toFile().isDirectory()) {
                    //System.out.println("In Folder : {" + fileName + "} the language is not present or is not at right position.");
                    folderErrorSummary.add("For FileName : {" + fileName + "} the language is not present or is not at right position.");
                    pathNameFailedValidation++;
                } else {
                    System.out.println("For FileName : {" + fileName + "} the language is not present or is not at right position.");
                    pathNameFailedValidation++;
                }*/

                folderError.append("\n\t\t\t\tFor {" + fileName + "} the language is not present or is not at right position.");

            }

        }
    }

    private void allRequiredValidations(String fileNameCheck, String fileName, String dateCheck, String fileLanguage, String fileLanguageCode) {

        fileNameValidation(fileNameCheck, fileName);
        fileUppercaseValidation(fileNameCheck, fileName);
        datePatternValidation(dateCheck, fileName);
        if(StringUtils.isEmpty(fileLanguageCode)) {
            languageCheckValidator(fileName, fileLanguage);
        } else {
            languageCodeCheckValidator(fileName, fileLanguageCode);
        }

    }

}
      