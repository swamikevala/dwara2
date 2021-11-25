package org.isha.dwaraimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
		System.out.println("Loading artifactclass mapping into memory from " + folderPath);
		Map<String, Object> folders = getJSONFromFile(folderPath);
		java.io.File file = new java.io.File(bruFile);
		for (java.io.File textFile : file.listFiles()) {
			if (!textFile.isDirectory()) {
				System.out.println("Parsing catalog " + textFile.getName());
				try {
					failedTextFile = textFile;
					ltoTape = textFile.getName().split("_")[0];
					String dateStr = textFile.getName().split("_")[1].split("\\.")[0];
					finalizedAt = dateStr + ", 00:00:00.000";

					//            		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy HH"); 
					//            		LocalDateTime dateTime = LocalDateTime.parse(dateStr + " 00", formatter);
					//            		DateTimeFormatter formatterISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
					//            		finalizedAt = dateTime.format(formatterISO);

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

								listBruData.add(b);
							}
						}
					}
					LineIterator.closeQuietly(it);
					//calculateArtifactSize();
					calculateEndBlock();

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
				if(bruData.name.startsWith(artifactsList.get(i).name) && bruData.isDirectory) {
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


	public static final XMLOutputProcessor XMLOUTPUT = new AbstractXMLOutputProcessor() {
		@Override
		protected void printDeclaration(final Writer out, final FormatStack fstack) throws IOException {
			write(out, "<?xml version=\"1.1\" encoding=\"utf-8\" ?> ");
			write(out, fstack.getLineSeparator());
		}
	};


	public String createVolumeindex(String finalizedDate, String destinationFile) {
		System.out.println("Framing objects");
		Volumeinfo volumeinfo = new Volumeinfo();
		volumeinfo.setVolumeuid(StringUtils.isEmpty(ltoTape) ? "No_LTO" : ltoTape);
		volumeinfo.setVolumeblocksize("1048576");
		volumeinfo.setArchiveformat("bru");
		volumeinfo.setArchiveblocksize("1024");
		volumeinfo.setEncryptionalgorithm("AES-128");
		volumeinfo.setFinalizedAt(finalizedDate);
		volumeinfo.setChecksumalgorithm("md5");

		List<Artifact> artifactXMLList = new ArrayList<>();
		for (BruData artifactList: artifactsList) {
			try {
				System.out.println("Framing object for " + artifactList.name);
				Artifact artifact = new Artifact();
				artifact.setName(artifactList.name);
				artifact.setStartblock(String.valueOf(artifactList.startVolumeBlock));
				artifact.setEndblock(String.valueOf(artifactList.endVolumeBlock));
				artifact.setArtifactclassuid(artifactList.category);

				List<File> fileList = new ArrayList<>();
				for (BruData bruData : listBruData) {
					File file = new File();
					file.setName(bruData.name);
					if (bruData.name.equals(artifactList.name)) {
						file.setVolumeStartBlock(String.valueOf(artifactList.startVolumeBlock));
						file.setVolumeEndBlock(String.valueOf(artifactList.endVolumeBlock));
						file.setArchiveblock(String.valueOf(bruData.archiveBlock));
						file.setDirectory(String.valueOf(artifactList.isDirectory));
					} else if(bruData.name.startsWith(artifactList.name + "/")){
						file.setVolumeStartBlock(String.valueOf(bruData.startVolumeBlock));
						file.setVolumeEndBlock(String.valueOf(bruData.endVolumeBlock));
						file.setArchiveblock(String.valueOf(bruData.archiveBlock));
						if(bruData.isDirectory)
							file.setDirectory(String.valueOf(bruData.isDirectory));
					}
					fileList.add(file);
				}
				// now lets calculate and collect subfolders size
				Map<String,Long> filePathnameVsSize_Map = new HashMap<String, Long>();
				for (File nthFile : fileList) {
					String nthFilepathname = nthFile.getName();
					if(Boolean.parseBoolean(nthFile.getDirectory())){
						filePathnameVsSize_Map.put(nthFilepathname,0L);
					}else {
						String nthFileDirectoryName = FilenameUtils.getFullPathNoEndSeparator(nthFilepathname);

						for (String nthArtifactSubDirectory : filePathnameVsSize_Map.keySet()) {
							if(nthFileDirectoryName.contains(nthArtifactSubDirectory)) {
								Long size = filePathnameVsSize_Map.get(nthArtifactSubDirectory);
								size += Long.parseLong(nthFile.getSize());
								filePathnameVsSize_Map.put(nthArtifactSubDirectory,size);
							}
						}
					}
				}                 

				// now lets make use of the collected subfolder size 
				for (File nthFile : fileList) {
					if(Boolean.parseBoolean(nthFile.getDirectory())){
						String nthDirectorySize= filePathnameVsSize_Map.get(nthFile.getName())+"";
						nthFile.setSize(nthDirectorySize);
						/*
						if(nthFile.getName().equals(artifactList.name))
							artifact.setTotalSize(nthDirectorySize);
						*/
					}
				}		
				artifact.setFile(fileList);
				artifactXMLList.add(artifact);
			}catch (Exception e) {
				System.err.println(artifactList.name + " has errors " + e.getMessage());
				e.printStackTrace();
			}
			System.out.println("Framed object for " + artifactList.name);
		}

		System.out.println("Generating xml");
		Volumeindex volumeindex = new Volumeindex();
		volumeindex.setVolumeinfo(volumeinfo);
		volumeindex.setArtifact(artifactXMLList);

		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
		xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
		String xmlFromJava = null;
		try {

			xmlFromJava = xmlMapper.writeValueAsString(volumeindex);
			Files.write(Paths.get(destinationFile + java.io.File.separator + ltoTape + ".xml"), xmlFromJava.getBytes(), StandardOpenOption.CREATE);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Xml Generated");
		return xmlFromJava;

	}

}
