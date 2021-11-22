package org.isha.dwaraimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DwaraImport {
    private static final Logger LOG = LoggerFactory.getLogger(DwaraImport.class);

    private List<BruData> listBruData = new ArrayList<>();
    private List<BruData> artifactsList = new ArrayList<>();
    private List<BruData> fileList = new ArrayList<>();
    String finalizedAt = null;
    String ltoTape;
    java.io.File failedTextFile;

    static Map getJSONFromFile(String folderPath) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(folderPath));

        Map<String, Object> map = new Gson()
                .fromJson(obj.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());

            return map;

    }


    public void apacheGetXMLData(String bruFile, String folderPath, String destinationXmlPath) throws IOException, ParseException {

        Map<String, Object> folders = getJSONFromFile(folderPath);
        java.io.File file = new java.io.File(bruFile);
        for (java.io.File textFile : file.listFiles()) {
            if (!textFile.isDirectory()) {
                try {
                    failedTextFile = textFile;
                    LineIterator it = FileUtils.lineIterator(textFile, "UTF-8");
                    listBruData.clear();
                    while (it.hasNext()) {
                        String line = it.nextLine();
                        if (line.contains("VL:c")) {

                            String[] arrValues = line.split("\\|");
                            BruData b = new BruData();
                            if (arrValues[0].equals("VL:c")) {
                                String temp = arrValues[5].replaceAll("\\P{Print}", "");
                                if (arrValues[5].endsWith("/")) {
                                    temp = StringUtils.substring(arrValues[5], 0, -1);
                                }
                                if (temp.startsWith("./")) {
                                    temp = temp.replace("./", "");
                                }

                                b.startVolumeBlock = Long.parseLong(arrValues[4]) + 1;
                                b.size = Long.parseLong(arrValues[3]);

                                if (!temp.contains("/")) {
                                    b.name = temp;
                                    b.isArtifact = true;
                                    b.isDirectory = true;
                                } else {
                                    b.name = temp;
                                    b.isArtifact = false;
                                    temp.substring(temp.lastIndexOf("/") + 1);
                                    if (!temp.substring(temp.lastIndexOf("/") + 1).contains(".")) {
                                        b.isDirectory = true;
                                    }

                                    fileList.add(b);
                                }

                                b.category = (String) folders.get(temp);
                                b.archiveBlock = arrValues[1];
                                b.archiveId = "";
                                //b.ltoTape = (String) barCodeTapes.get(textFile.getName().split("-")[1]);
                                b.ltoTape = textFile.getName().split("_")[0];
                                ltoTape = textFile.getName().split("_")[0];
                                finalizedAt = textFile.getName().split("_")[1].split("\\.")[0] + ", 00:00:00.000";


                                listBruData.add(b);


                            }
                        }
                    }
                    LineIterator.closeQuietly(it);
                    calculateArtifactSize();
                    calculateEndBlock();
                    //volumeJDOMDocument(destinationXmlPath, finalizedAt);
                    createVolumeindex(finalizedAt, destinationXmlPath);

                } catch (Exception e) {
                    e.getStackTrace();

                }

            }
        }
    }


   private void calculateArtifactSize() {
       artifactsList = listBruData.stream().filter(b -> b.isArtifact).collect(Collectors.toList());
       for (int i=0; i< artifactsList.size(); i++) {
           long totalSize = 0;
            for (BruData bruData : listBruData) {
               if(bruData.name.startsWith(artifactsList.get(i).name)) {
                   totalSize += bruData.size;
               }
           }
            BruData tempArtifact = artifactsList.get(i);
            tempArtifact.totalSize = totalSize;
            artifactsList.set(i, tempArtifact);
       }
   }

    private void calculateEndBlock() {

        for(int i=0; i< artifactsList.size(); i++) {
            boolean lastArtifactFile = false;
            Long tempArtifactEndBlock = null;
            for(int j=0; j< listBruData.size(); j++) {
                if(listBruData.get(j).name.startsWith(artifactsList.get(i).name + "/")) {
                    BruData tempFile = listBruData.get(j);
                    if(j == listBruData.size()-1) {
                        tempFile.endVolumeBlock = (long) (listBruData.get(j).startVolumeBlock + (Math.ceil(((double)listBruData.get(j).size) / 1024) - 1));
                    } else {
                        if(!listBruData.get(j).startVolumeBlock.equals(listBruData.get(j+1).startVolumeBlock)) {
                            tempFile.endVolumeBlock = listBruData.get(j+1).startVolumeBlock - 1;
                        } else {
                            tempFile.endVolumeBlock = listBruData.get(j+1).startVolumeBlock;
                        }

                    }
                    listBruData.set(j, tempFile);
                    lastArtifactFile = true;
                    tempArtifactEndBlock = tempFile.endVolumeBlock;
                    
                }
                
            }
            if(lastArtifactFile) {
                BruData artifactTempFile = artifactsList.get(i);
                artifactTempFile.endVolumeBlock = tempArtifactEndBlock;
                artifactsList.set(i, artifactTempFile);
            }

        }

    }


    private void volumeJDOMDocument(String destinationXmlPath, String finalizedDate) {
        String ltoTape, destinationFileName;
        Document doc = new Document();
        Element volumeIndex = new Element("VolumeIndex");

        Element volumeInfo = new Element("VolumeInfo");
        Element volumeUid = new Element("VolumeUid");
        //Element volumeBlocksize = new Element("VolumeBlockSize");
        if(StringUtils.isEmpty(artifactsList.get(0).ltoTape)) {
            ltoTape = "No_LTO";
            volumeUid.addContent("No_LTO");
        } else {
            ltoTape = artifactsList.get(0).ltoTape;
            volumeUid.addContent(artifactsList.get(0).ltoTape);
        }


            Element volumeBlocksize = new Element("VolumeBlocksize").addContent("1048576");
            Element archiveFormat = new Element("ArchiveFormat").addContent("bru");
            Element archiveBlocksize = new Element("ArchiveBlocksize").addContent("1024");
            Element checksumAlgorithm = new Element("ChecksumAlgorithm").addContent("md5");
            Element encryptionAlgorithm = new Element("EncryptionAlgorithm").addContent("AES-128");
            Element finalizedAt = new Element("FinalizedAt").addContent(finalizedDate);


        volumeInfo.addContent(volumeUid);
        volumeInfo.addContent(volumeBlocksize);
        volumeInfo.addContent(archiveFormat);
        volumeInfo.addContent(archiveBlocksize);

        if(!StringUtils.isEmpty(checksumAlgorithm.getContent().toString())) {
            volumeInfo.addContent(checksumAlgorithm);
        }
        if(!StringUtils.isEmpty(encryptionAlgorithm.getContent().toString())) {
            volumeInfo.addContent(encryptionAlgorithm);
        }
        if(!StringUtils.isEmpty(finalizedAt.getContent().toString())) {
            volumeInfo.addContent(finalizedAt);
        }

        volumeIndex.addContent(volumeInfo);


        for (BruData artifactList: artifactsList) {
            Element artifact = new Element("Artifact");
            Attribute minOccursAttribute = new Attribute("minOccurs", "0");
            artifact.setAttribute("name", String.valueOf(artifactList.name))
                    .setAttribute("startBlock", String.valueOf(artifactList.startVolumeBlock))
                    .setAttribute("endBlock", String.valueOf(artifactList.endVolumeBlock))
                    .setAttribute("artifactclassUid", artifactList.category)
                    .setAttribute("sequenceCode", " ");

            for (BruData bruData : listBruData) {
                Element file = new Element("File");
                if(bruData.name.equals(artifactList.name)) {
                    file.setAttribute("volumeStartBlock", String.valueOf(artifactList.startVolumeBlock))
                            .setAttribute("volumeEndBlock", String.valueOf(artifactList.endVolumeBlock))
                            .setAttribute("archiveBlock", String.valueOf(bruData.archiveBlock))
                            .setAttribute("size", String.valueOf(artifactList.totalSize))
                            .setAttribute("directory", String.valueOf(artifactList.isDirectory))
                            .setAttribute("checksum", "");
                    file.addContent(bruData.name);
                    artifact.addContent(file);
                } else {

                    if(bruData.isDirectory && bruData.name.startsWith(artifactList.name + "/")) {
                        file.setAttribute("volumeStartBlock", String.valueOf(bruData.startVolumeBlock))
                                .setAttribute("volumeEndBlock", String.valueOf(bruData.endVolumeBlock))
                                .setAttribute("archiveBlock", String.valueOf(bruData.archiveBlock))
                                .setAttribute("size", String.valueOf(bruData.size))
                                .setAttribute("directory", String.valueOf(bruData.isDirectory))
                                .setAttribute("checksum", "");
                        file.addContent(bruData.name);
                        artifact.addContent(file);
                    } else if(bruData.name.startsWith(artifactList.name + "/") ){
                        file.setAttribute("volumeStartBlock", String.valueOf(bruData.startVolumeBlock))
                                .setAttribute("volumeEndBlock", String.valueOf(bruData.endVolumeBlock))
                                .setAttribute("archiveBlock", String.valueOf(bruData.archiveBlock))
                                .setAttribute("size", String.valueOf(bruData.size))
                                .setAttribute("checksum", "");
                        file.addContent(StringEscapeUtils.unescapeXml(bruData.name));
                        artifact.addContent(file);
                    }
                }

            }
            volumeIndex.addContent(artifact);
        }

        doc.addContent(volumeIndex);

        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat(), XMLOUTPUT);

        if(destinationXmlPath.endsWith("\\") || destinationXmlPath.endsWith("/")) {
            destinationFileName = destinationXmlPath.concat(ltoTape).concat(".xml");
        } else {
            destinationFileName = destinationXmlPath.concat("/").concat(ltoTape).concat(".xml");
        }

        try {
            FileOutputStream fileOutputStream =
                    new FileOutputStream(destinationFileName);
            xmlOutputter.output(doc, fileOutputStream);

           /* SchemaFactory schemafac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL xsdResource = DwaraImport.class.getClassLoader().getResource("validation.xsd");
            Schema schema = schemafac.newSchema(new File(xsdResource.getPath()));
            XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
            SAXBuilder sb = new SAXBuilder(factory);
            Document document = sb.build(new File(destinationFileName));*/
            //System.out.println("Document Created Successfully at : " + destinationFileName);
        } catch (Exception e) {
            e.getStackTrace();
        }


    }


    public static final XMLOutputProcessor XMLOUTPUT = new AbstractXMLOutputProcessor() {
        @Override
        protected void printDeclaration(final Writer out, final FormatStack fstack) throws IOException {
            write(out, "<?xml version=\"1.1\" encoding=\"utf-8\" ?> ");
            write(out, fstack.getLineSeparator());
        }
    };


    public String createVolumeindex(String finalizedDate, String destinationFile) {
        Volumeinfo volumeinfo = new Volumeinfo();
        volumeinfo.setVolumeuid(StringUtils.isEmpty(artifactsList.get(0).ltoTape) ? "No_LTO" : artifactsList.get(0).ltoTape);
        volumeinfo.setVolumeblocksize("1048576");
        volumeinfo.setArchiveformat("bru");
        volumeinfo.setArchiveblocksize("1024");
        volumeinfo.setEncryptionalgorithm("AES-128");
        volumeinfo.setFinalizedAt(finalizedDate);
        volumeinfo.setChecksumalgorithm("md5");

        List<Artifact> artifactXMLList = new ArrayList<>();
        for (BruData artifactList: artifactsList) {
                Artifact artifact = new Artifact();
                artifact.setName(artifactList.name);
                artifact.setStartblock(String.valueOf(artifactList.startVolumeBlock));
                artifact.setEndblock(String.valueOf(artifactList.endVolumeBlock));
                artifact.setArtifactclassuid(artifactList.category);

                List<File> fileList = new ArrayList<>();
                    for (BruData bruData : listBruData) {
                        File file = new File();
                        if (bruData.name.equals(artifactList.name)) {
                            file.setName(bruData.name);
                            file.setSize(String.valueOf(artifactList.totalSize));
                            file.setVolumeStartBlock(String.valueOf(artifactList.startVolumeBlock));
                            file.setVolumeEndBlock(String.valueOf(artifactList.endVolumeBlock));
                            file.setArchiveblock(String.valueOf(bruData.archiveBlock));
                            file.setDirectory(String.valueOf(artifactList.isDirectory));
                            fileList.add(file);

                        } else {
                            if(bruData.isDirectory && bruData.name.startsWith(artifactList.name + "/")) {
                                file.setName(bruData.name);
                                file.setSize(String.valueOf(bruData.size));
                                file.setVolumeStartBlock(String.valueOf(bruData.startVolumeBlock));
                                file.setVolumeEndBlock(String.valueOf(bruData.endVolumeBlock));
                                file.setArchiveblock(String.valueOf(bruData.archiveBlock));
                                file.setDirectory(String.valueOf(bruData.isDirectory));
                                fileList.add(file);

                            } else if(bruData.name.startsWith(artifactList.name + "/") ){
                                file.setName(bruData.name);
                                file.setSize(String.valueOf(bruData.size));
                                file.setVolumeStartBlock(String.valueOf(bruData.startVolumeBlock));
                                file.setVolumeEndBlock(String.valueOf(bruData.endVolumeBlock));
                                file.setArchiveblock(String.valueOf(bruData.archiveBlock));
                                fileList.add(file);

                            }
                    }
                }

                artifact.setFile(fileList);
                artifactXMLList.add(artifact);
        }

        Volumeindex volumeindex = new Volumeindex();
        volumeindex.setVolumeinfo(volumeinfo);
        volumeindex.setArtifact(artifactXMLList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String xmlFromJava = null;
        try {

            xmlFromJava = xmlMapper.writeValueAsString(volumeindex);
            Files.write(Paths.get(destinationFile + "\\" + ltoTape + ".xml"), xmlFromJava.getBytes(), StandardOpenOption.CREATE);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return xmlFromJava;

    }

}
