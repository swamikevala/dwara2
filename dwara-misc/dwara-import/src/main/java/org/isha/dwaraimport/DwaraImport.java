package org.isha.dwaraimport;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.staged.scan.Error;
import org.ishafoundation.dwaraapi.staged.scan.Validation;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Imported;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeindex;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeinfo;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class DwaraImport extends Validation{
	private static final Logger LOG = LoggerFactory.getLogger(DwaraImport.class);

	private List<BruData> listBruData = new ArrayList<>();
	private List<BruData> artifactsList = new ArrayList<>();
	private List<BruData> fileList = new ArrayList<>();
	String regexAllowedChrsInFileName = "[\\w-.]*";
	Pattern allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);
	
	static Map getJSONFromFile(String folderPath) throws IOException, ParseException {

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(folderPath));

		Map<String, Object> map = new Gson()
				.fromJson(obj.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());

		return map;

	}


	public void apacheGetXMLData(String bruFile, String artifactToArtifactClassMappingJsonFolderPath, String destinationXmlPath) throws IOException, ParseException {
		System.out.println("Loading artifact to artifactclass mapping " + artifactToArtifactClassMappingJsonFolderPath);
		Map<String, Object> artifactToArtifactClassMapping = getJSONFromFile(artifactToArtifactClassMappingJsonFolderPath);

		java.io.File file = new java.io.File(bruFile);
		for (java.io.File textFile : file.listFiles()) {
			if (!textFile.isDirectory()) {
				System.out.println("Parsing catalog " + textFile.getName());
				try {
					java.io.File completedFile = Paths.get(bruFile,"completed",textFile.getName()).toFile();
					if(completedFile.exists()) {
						System.err.println(completedFile.getAbsolutePath() + " already exists. Figure out why we are already running a complete catalog. Skipping it");
						throw new Exception(completedFile.getAbsolutePath() + " already exists. Figure out why we are already running a complete catalog. Skipping it");
					}
					String ltoTape = textFile.getName().split("_")[0];
					String dateStr = textFile.getName().split("_")[1].split("\\.")[0];

            		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy HH"); 
            		LocalDateTime dateTime = LocalDateTime.parse(dateStr + " 00", formatter);
            		DateTimeFormatter formatterISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            		String writtenAt = dateTime.format(formatterISO);

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
									// extra 4096 check to address entire like 
									// VL:c|385671168|1|4096|376631|Z7424_Class_IEO_Tamil-Day2-Desire_FCP7-And-FCPX/XMLs/FCP X/._IEO Tamil Day 5 - Acceptance II.fcpxml 
									// which is a folder. 
									// Check still doest not guarantee the classification of a file vs folder as there are folders like these too
									// VL:c|385671168|1|93265|376631|Z7424_Class_IEO_Tamil-Day2-Desire_FCP7-And-FCPX/XMLs/FCP X/Misc Videos - Yatra + Mystic.fcpxml
									if (!temp.substring(temp.lastIndexOf("/") + 1).contains(".") || b.size == 4096) { 
										b.isDirectory = true;
									}

									fileList.add(b);
								}

								b.category = (String) artifactToArtifactClassMapping.get(temp);
								b.archiveBlock = arrValues[1];
								b.archiveId = "";

								listBruData.add(b);
							}
						}
					}
					LineIterator.closeQuietly(it);
					artifactsList = listBruData.stream().filter(b -> b.isArtifact).collect(Collectors.toList());
					//calculateArtifactSize();
					calculateEndBlock();

					createVolumeindex(ltoTape, writtenAt, destinationXmlPath);
					
					FileUtils.moveFile(textFile, Paths.get(bruFile,"completed",textFile.getName()).toFile());
				} catch (Exception e) {
					e.printStackTrace();
					java.io.File failedFile = Paths.get(bruFile,"failed",textFile.getName()).toFile();
					if(failedFile.exists())
						failedFile.delete();
					FileUtils.moveFile(textFile, failedFile);
				}

			}
		}
	}


	private void calculateArtifactSize() {
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


	public String createVolumeindex(String ltoTape, String writtenAt, String destinationFile) throws Exception {
		boolean hasErrors = false;
		System.out.println("Framing VolumeIndex Object from parsed data");
		Volumeinfo volumeinfo = new Volumeinfo();
		volumeinfo.setVolumeuid(StringUtils.isEmpty(ltoTape) ? "No_LTO" : ltoTape);
		volumeinfo.setVolumeblocksize(1048576);
		volumeinfo.setArchiveformat("bru");
		volumeinfo.setArchiveblocksize(1024);
		//volumeinfo.setEncryptionalgorithm("AES-128");
		Imported imported = new Imported();
		imported.setImported(true);
		imported.setWrittenAt(writtenAt);
		volumeinfo.setImported(imported);
		//volumeinfo.setFinalizedAt(finalizedDate);
		//volumeinfo.setChecksumalgorithm("md5");

		List<Artifact> artifactXMLList = new ArrayList<>();
		for (BruData artifactList: artifactsList) {
			try {
				List<Error> errorList = new ArrayList<Error>();
				errorList.addAll(validateName(artifactList.name, allowedChrsInFileNamePattern));
				
				// System.out.println("Framing object for " + artifactList.name);
				Artifact artifact = new Artifact();
				artifact.setName(artifactList.name);
				artifact.setStartblock(artifactList.startVolumeBlock.intValue());
				artifact.setEndblock(artifactList.endVolumeBlock.intValue());
				if(StringUtils.isBlank(artifactList.category)) { // check if its to be ignored...
//					List<String> abc = (List<String>) ignoreImportArtifacts.get(ltoTape);
//					if(abc != null && abc.contains(artifactList.name)){
//						System.out.println("Skipped " + artifactList.name + " its flagged to be ignored");
//						continue;
//					}
//					else
					System.err.println(ltoTape + ":" + artifactList.name + " misses artifactclass");
					hasErrors=true;
				}	
				artifact.setArtifactclassuid(artifactList.category);

				List<File> fileList = new ArrayList<>();
				for (BruData bruData : listBruData) {
					File file = new File();
					file.setName(bruData.name);
					file.setSize(bruData.size);
					if (bruData.name.equals(artifactList.name)) {
						file.setVolumeStartBlock(artifactList.startVolumeBlock.intValue());
						//file.setVolumeEndBlock(String.valueOf(artifactList.endVolumeBlock));
						file.setArchiveblock(Long.parseLong(bruData.archiveBlock));
						file.setDirectory(artifactList.isDirectory);
						fileList.add(file);
					} else if(bruData.name.startsWith(artifactList.name + "/")){
						file.setVolumeStartBlock(bruData.startVolumeBlock.intValue());
						//file.setVolumeEndBlock(String.valueOf(bruData.endVolumeBlock));
						file.setArchiveblock(Long.parseLong(bruData.archiveBlock));
						if(bruData.isDirectory)
							file.setDirectory(bruData.isDirectory);
						fileList.add(file);
					}
					
				}
				// now lets calculate and collect subfolders size
				Map<String,Long> filePathnameVsSize_Map = new HashMap<String, Long>();
				for (File nthFile : fileList) {
					String nthFilepathname = nthFile.getName();
					if(Boolean.TRUE.equals(nthFile.getDirectory())){
						filePathnameVsSize_Map.put(nthFilepathname,0L);
					}else {
						String nthFileDirectoryName = FilenameUtils.getFullPathNoEndSeparator(nthFilepathname);

						for (String nthArtifactSubDirectory : filePathnameVsSize_Map.keySet()) {
							if(nthFileDirectoryName.contains(nthArtifactSubDirectory)) {
								Long size = filePathnameVsSize_Map.get(nthArtifactSubDirectory);
								size += nthFile.getSize();
								filePathnameVsSize_Map.put(nthArtifactSubDirectory,size);
							}
						}
					}
				}                 

				long artifactSize = 0L;
				// now lets make use of the collected subfolder size 
				for (File nthFile : fileList) {
					if(Boolean.TRUE.equals(nthFile.getDirectory())){
						Long nthDirectorySize= filePathnameVsSize_Map.get(nthFile.getName());
						nthFile.setSize(nthDirectorySize);

						if(nthFile.getName().equals(artifactList.name)){
							artifactSize = nthDirectorySize;
							//artifact.setTotalSize(nthDirectorySize);
						}

					}
				}		
				artifact.setFile(fileList);
				
				errorList.addAll(validateFileCount(fileList.size()));
				errorList.addAll(validateFileSize(artifactSize));

				if(errorList.size() > 0) {
					System.err.println(ltoTape + ":" + artifactList.name + " has validation failures - " + errorList.toString());
					hasErrors=true;
				}

				artifactXMLList.add(artifact);

			}catch (Exception e) {
				System.err.println(ltoTape + ":" + artifactList.name + " has errors " + e.getMessage());
				hasErrors=true;
				e.printStackTrace();
			}
			// System.out.println("Framed object for " + artifactList.name);
		}
		
		if(hasErrors) {
			destinationFile = destinationFile + "-failed";
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
			Files.write(Paths.get(destinationFile + java.io.File.separator + ltoTape + ".xml"), xmlFromJava.getBytes());

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Xml Generated");
		if(hasErrors) {
			throw new Exception(ltoTape + " has errors");
		}
		return xmlFromJava;

	}

}
