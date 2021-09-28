package org.ishafoundation.validation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LangValidator implements LangValidatorInterface {
    //public static Logger logger = LoggerFactory.getLogger(LangValidator.class);

    public Set<String> languages = new HashSet<>();
    public Set<String> languageCode = new HashSet<>();

    {
        String[] languages = Locale.getISOLanguages();
        for (String language : languages) {
            Locale locale = new Locale(language);
            this.languages.add(locale.getDisplayLanguage());
            this.languageCode.add(locale.getISO3Language());
        }

    }


    public void validations(String dept, String type, String pathName) {
        if (type.equals("backup")) {
            backupValidator(pathName);

        } else if ((dept.equals("glp") || dept.equals("ilp")) && type.equals("archive")) {
            fileValidator(pathName);
        }
    }

    @Override
    public void folderValidator(String pathName) {

        System.out.println("Folder Path Validating : " + pathName);
        pathDirectoryValidator(pathName);

        File artifact = new File(pathName);
        List<File> files = Arrays.asList(Objects.requireNonNull(artifact.listFiles()));

        if (!(files.size() > 0)) {
            //logger.error("There are no files in the Artifact : {" + pathName + "}");
            System.out.println("There are no files in the Artifact : {" + pathName + "}");
        }


        for (File file : files) {
            if (!file.canWrite()) {
                //logger.error("Do not have File : {" + file + "} write permission.");
                System.out.println("Do not have File : {" + file + "} write permission.");
            }
            if (file.isDirectory()) {
                System.out.println("Folder Path Validating : " + file.getPath());
                pathDirectoryValidator(file.getPath());
                File[] innerFiles = file.listFiles();
                assert innerFiles != null;
                for (File innerFile : innerFiles) {
                    System.out.println("Folder Path Validating : " + innerFile.getPath());
                    pathDirectoryValidator(innerFile.getPath());
                    if (innerFile.isDirectory()) {
                        for (File internalFile : Objects.requireNonNull(innerFile.listFiles())) {
                            System.out.println("Folder Path Validating : " + internalFile.getPath());
                            pathDirectoryValidator(internalFile.getPath());
                            if (internalFile.getPath().contains("\\Video Output")) {
                                for (String videoFile : Objects.requireNonNull(innerFile.list())) {
                                    String extension = FilenameUtils.getExtension(videoFile);
                                    if (extension.equals("mp4") || extension.equals("mov")) {
                                        System.out.println("File is : " + videoFile);
                                    }
                                }

                            }
                        }

                    } else if (!file.getName().startsWith("Output_")) {
                        //logger.error("Artifact need to have Video output folder or file starting with Output_");
                        System.out.println("Artifact : {" + file.getName() + "}  need to have Video output folder or file starting with Output_");
                    }
                }

            } else {
                System.out.println("Folder Path Validating : " + file.getPath());
                if (!file.getName().startsWith("Output_")) {
                    //logger.error("Artifact need to have Video output folder or file starting with Output_");
                    System.out.println("Artifact : {" + file.getName() + "} need to have Video output folder or file starting with Output_");
                }

            }

        }

    }

    @Override
    public void directorySizeValidator(String pathName) {
        pathDirectoryValidator(pathName);

    }


    @Override
    public void basicFileNameValidation(String fileName) {

        String regexAllowedChrsInFileName = "[\\w-.]*";
        Pattern allowedCharInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);

        if (fileName.length() > 245) { // 245 because we need to add sequence number
            //logger.error("Artifact Name is more than 245 characters");
            System.out.println("Artifact Name is more than 245 characters");
        }

        Matcher m = allowedCharInFileNamePattern.matcher(fileName);
        if (!m.matches()) {
            //logger.error("Artifact Name : {" + fileName + "} contains special characters");
            System.out.println("Artifact Name : {" + fileName + "} contains special characters");
        }

        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            decoder.decode(ByteBuffer.wrap(fileName.getBytes()));
        } catch (CharacterCodingException ex) {
            //logger.error("Artifact Name : {" + fileName + "} contains non-unicode characters");
            System.out.println("Artifact Name : {" + fileName + "} contains non-unicode characters");
        }

    }


    @Override
    public void languageValidator(String lang) {
        boolean isLanguage = false;
        for (String language : languages) {
            if (language.equals(lang)) {
                isLanguage = true;
                break;
            }
        }
        if (!isLanguage) {
            for (String language : languageCode) {
                if (language.equalsIgnoreCase(lang)) {
                    isLanguage = true;
                    break;
                }
            }
        }
        if (!isLanguage) {
            //logger.error("Language name or code is not present after name");
            System.out.println("Language name or code is not present after name");
        }
    }

    @Override
    public void datePatternValidation(String fileName) {
        Pattern datePattern = Pattern.compile("^(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}$");
        Matcher m1 = datePattern.matcher(fileName);

        if (!m1.matches()) {
            //logger.error("Artifact Name : {" + fileName + "} date should be in ddMonyyyy pattern");
            System.out.println("Artifact Name : {" + fileName + "} date should be in ddMonyyyy pattern");
        }
    }

    @Override
    public void fileUppercaseValidation(String fileName) {
        String[] nameSplited = fileName.split("-");
        for (String name : nameSplited) {
            if (!StringUtils.isAllUpperCase(name.subSequence(0, 1))) {
                //logger.error("In the File : {" + fileName + "} The first letters of words in name : {" + name + "} are not in UPPERCASE");
                System.out.println("In the File : {" + fileName + "}The first letters of words in name : {" + name + "} are not in UPPERCASE");
            }

        }

    }

    @Override
    public void fileValidator(String pathName) {

        File artifact = new File(pathName);
        File[] files = artifact.listFiles();
        assert files != null;
        for (File file : files) {
            if (artifact.isDirectory()) {

                folderValidator(file.getPath());
                boolean type = videoDifferentiator(artifact.getName());

                if (file.isDirectory()) {
                    File[] innerFiles = file.listFiles();
                    assert innerFiles != null;
                    for (File innerFile : innerFiles) {
                        if (innerFile.isDirectory()) {
                            File[] internalFolders = innerFile.listFiles();
                            assert internalFolders != null;
                            for (File internalFile : internalFolders) {
                                if (internalFile.isDirectory()) {
                                    for (String videoFile : Objects.requireNonNull(internalFile.list())) {
                                        if (type) {
                                            artifactYTValidation(FilenameUtils.getName(videoFile));
                                        } else {
                                            artifactNYTValidation(FilenameUtils.getName(videoFile));
                                        }

                                    }
                                }
                            }

                        } else {
                            if (type) {
                                artifactYTValidation(innerFile.getName());
                            } else {
                                artifactNYTValidation(innerFile.getName());
                            }

                        }

                    }
                } else {
                    if (type) {
                        artifactYTValidation(file.getName());
                    } else {
                        artifactNYTValidation(file.getName());
                    }

                }

            } else {
                folderValidator(file.getPath());
                boolean type = videoDifferentiator(artifact.getName());
                if (type) {
                    artifactYTValidation(file.getName());
                } else {
                    artifactNYTValidation(file.getName());
                }

            }
        }


    }


    private void pathDirectoryValidator(String pathName) {
        File artifact = new File(pathName);
        if (artifact.isDirectory()) {
            if (FileUtils.sizeOfDirectory(artifact) == 0 && !(Objects.requireNonNull(artifact.list()).length > 0)) {
                //logger.error("Directory is empty in the path : {" + pathName + "}");
                System.out.println("Directory is empty in the path : {" + pathName + "}");
            }
        } else {
            if (FileUtils.sizeOf(artifact) == 0 && !(Objects.requireNonNull(artifact.list()).length > 0)) {
                //logger.error("Directory is empty in the path : {" + pathName + "}");
                System.out.println("Directory is empty in the path : {" + pathName + "}");
            }
        }
    }


    public void backupValidator(String pathName) {
        File root = new File(pathName);
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (file.isDirectory()) {
                backupValidator(file.getPath());
            } else {
                basicFileNameValidation(file.getName());
                directorySizeValidator(file.getPath());
            }

        }

    }

    public boolean videoDifferentiator(String name) {
        //TODO to find the YT and NYT difference
        return !name.toLowerCase().contains("nyt");

    }


    public void artifactYTValidation(String artifactName) {
        List<String> names = Arrays.asList(artifactName.split("_"));
        if (!(names.size() == 3 || names.size() == 4)) {
            //logger.error("Artifact Name : {" + artifactName + "} is not passing the validations");
            System.out.println("Artifact Name : {" + artifactName + "} is not passing the validations");

        } else {
            if (names.size() == 4) {
                String code = names.get(0);
                // may change for glp only spoke with ilp
                if (!(code.substring(0, 3).equals("SGC") || code.substring(0, 2).equals("IF"))) {
                    //TODO for ILP and GLP Code Validation
                    //code doesnot contain SGC or IF
                }
                boolean langcode = false;
                boolean langps = false;
                for (String lang : languageCode) {
                    if (code.contains(lang))
                        langcode = true;
                    if (code.indexOf(lang) == 3 || code.indexOf(lang) == 2)
                        langps = true;

                }
                if (!langcode) {
                    //logger.error("Language Code is incorrect in the Code : " + code + " of ArtifactName : " + artifactName) ;
                    System.out.println("Language Code is incorrect in the Code : " + code + " of ArtifactName : " + artifactName);
                }
                if (!langps) {
                    //logger.error("Language code is not at correct position in the Code : " + code + " of ArtifactName : " + artifactName);
                    System.out.println("Language code is not at correct position in the Code : " + code + " of ArtifactName : " + artifactName);
                }
                //have to add validation for remaining yt code need to get more info


                basicFileNameValidation(names.get(1));
                fileUppercaseValidation(names.get(1));
                datePatternValidation(names.get(3).substring(0, names.get(3).lastIndexOf(".")));
                languageValidator(names.get(2));

            } else {
                basicFileNameValidation(names.get(0));
                fileUppercaseValidation(names.get(0));
                datePatternValidation(names.get(2).substring(0, names.get(2).lastIndexOf(".")));
                languageValidator(names.get(1));
            }
        }
    }


    public void artifactNYTValidation(String artifactName) {
        List<String> names = Arrays.asList(artifactName.split("_"));
        if (!(names.size() == 4 || names.size() == 5)) {
            //logger.error("Artifact Name : {" + artifactName + "} is not passing the validations");
            System.out.println("Artifact Name : {" + artifactName + "} is not passing the validations");

            names.size();
            basicFileNameValidation(names.get(0));
            fileUppercaseValidation(names.get(1));
            datePatternValidation(names.get(3).substring(0, names.get(3).lastIndexOf(".")));
            languageValidator(names.get(1));

        }
    }

}
      