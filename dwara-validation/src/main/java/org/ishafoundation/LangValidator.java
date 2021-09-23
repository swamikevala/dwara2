package org.ishafoundation.validation;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;




public class LangValidator implements LangValidatorInterface {
		
		public Set<String> languages ;
		public Set<String> languageCode ;
		private Pattern allowedChrsInFileNamePattern = null;
	   // private Pattern photoSeriesArtifactclassArifactNamePattern = null;
		private Pattern datePattern = null;
	    private static SimpleDateFormat photoSeriesArtifactNameDateFormat = new SimpleDateFormat("yyyyMMdd");
	    public List<Error> errorList = new ArrayList<>();
	
	    {
	    	
	    	 String[] languages = Locale.getISOLanguages();
	    	 for(String language: languages) {
	    		 Locale locale = new Locale(language);
	    		 this.languages.add(locale.getDisplayLanguage());
	    		 this.languageCode.add(locale.getISO3Language());
	    	 }
	    	
	    }
	    
	    
public void langArtifactValidator(String pathName ) {
		File root = new File(pathName);
		for (File file : root.listFiles()) {
			String fileName= file.getName();
		String type=	videoDifferentiator(fileName);
		if(type.equals("yt")) {
			langYTArtifactValidator(fileName );
		}
		else {
			langNYTArtifactValidator(fileName );
		}
		
		 sizeCountValidator(file.getPath());
		 folderValidator(file.getPath());
		 
		
		}
}

public void backupValidator(String pathName) {
	File root = new File(pathName);
	for (File file : root.listFiles()) {
		String fileName= file.getName();
		basicFileNameValidation(fileName );
		sizeCountValidator(file.getPath());
		
	}
	
}

		//String fileName = file.getName();
	
	public void basicFileNameValidation(String fileName ) {
		/*File folder = new File(pathName);
		File[] listOfFiles = folder.listFiles();*/
		
		
		
		
		String regexAllowedChrsInFileName = "[\\w-.]*";
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);
		//photoSeriesArtifactclassArifactNamePattern = Pattern
			//	.compile("([0-9]{8})_[A-Z]{3}_" + regexAllowedChrsInFileName);
		if (fileName.length() > 245) { // 245 because we need to add sequence number
			
			//"Artifact Name gt 245 characters";
			
		}

		Matcher m = allowedChrsInFileNamePattern.matcher(fileName);
		if (!m.matches()) {
			
			//"Artifact Name contains special characters"
			
		}

		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		try {
			decoder.decode(ByteBuffer.wrap(fileName.getBytes()));
		} catch (CharacterCodingException ex) {
			
			//("Artifact Name contains non-unicode characters");
			

		}	
		
		
		
	}

	@Override
	public void languageValidator(String lang) {
		boolean isLanguage = false;
		for (String language : languages) {
			if(language.equals(lang)){
				isLanguage= true;
			}
			
			
			
		}
		if(!isLanguage) {
			for (String language : languageCode) {
				if(language.equalsIgnoreCase(lang)){
					isLanguage= true;
				}
		}
		
	}
		if(!isLanguage) {
			//language name or code is not present after name
		}
		
		
	}

	@Override
	public String videoDifferentiator(String name) {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void langYTArtifactValidator(String ArtifactName) {
		
		List<String> names = Arrays.asList( ArtifactName.split("_"));
		if(!(names.size()==3 || names.size()==4)) {
			//error
		}
		else {
		
		
		int i =0;
		if(names.size() ==4) {
			i+=1;
			 String code = names.get(0);	
			// may change for glp only spoke with ilp
			 if(!(code.substring(0, 3).equals("SGC") || code.substring(0, 2).equals("IF") )) {
				 //code doesnot contain SGC or IF
			 }
			 boolean langcode=false;
			 boolean langps=false;
			 for (String lang : languageCode) {
				 if(code.contains(lang)) {
					 langcode=true;
					 if(code.indexOf(lang)==3 || code.indexOf(lang)==2)
						 langps =true;
				 }
			 }
			 if(!langcode) {
				 // lang not present in code
			 }
			 if(!langps) {
				 // lang not at position in code
			 }
		//have to add validation for remaining yt code need to get more info
		}
			 basicFileNameValidation(names.get(i));
				
				List<String> nameSplited = Arrays.asList(names.get(i).split("-"));
				for(String name : nameSplited  ) {
					if(!StringUtils.isAllUpperCase(name.subSequence(0, 1))) {
						//error the first letters of words in name are not in UPPERCASE
					}
					
					
				}
				
				languageValidator(names.get(i+1));
				
				datePattern =Pattern.compile("^(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}$");
				Matcher m1 = datePattern.matcher(names.get(i+2));
				
				if(!m1.matches()) {
					
					//"Artifact Name date should be in ddMonyyyy pattern"
					
				}
				
				
				
			}
		}
		
		
		
	

	@Override
	public void langNYTArtifactValidator(String ArtifactName) {
		List<String> names = Arrays.asList( ArtifactName.split("_"));
		if(!(names.size()==4 || names.size()==5)) {
			//error
		}
		else {
		
		
		int i =0;
		if(names.size() ==5) {
			i+=1;
		// have to add code validator after we get info about it	 
		
		}
			 basicFileNameValidation(names.get(i));
				
				List<String> nameSplited = Arrays.asList(names.get(i).split("-"));
				for(String name : nameSplited  ) {
					if(!StringUtils.isAllUpperCase(name.subSequence(0, 1))) {
						//error the first letters of words in name are not in UPPERCASE
					}
					
					
				}
				
				languageValidator(names.get(i+1));
				
				
				
				datePattern =Pattern.compile("^(([0-9])|([0-2][0-9])|([3][0-1]))-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-[0-9]{4}$");
				Matcher m1 = datePattern.matcher(names.get(i+3));
				
				if(!m1.matches()) {
					//"Artifact Name date should be in ddMonyyyy pattern"
				}
				
				
				
			}
		}
		
		
	

	@Override
	public void folderValidator(String pathName) {
		File artifact = new File(pathName);
		List<File> files = Arrays.asList(artifact.listFiles());
		if(FileUtils.sizeOfDirectory(artifact)==0) {
			//O GB
		}
		if(files.size()==0) {
			//Artifact is empty
		}
		boolean namepresent = false;
		
		for(File file : files) {
			if(!file.canWrite())
			{
				//Do not have write permission
			}
			if (file.isDirectory()) {
				if(file.getName().equals("Video Output")) {
					for(String videoFile : file.list()) {
						String extenstion =FilenameUtils.getExtension(videoFile);
						if(extenstion.equals("mp4") || extenstion.equals("mov"))
							namepresent=true;
					}
				}
				
			}
			
			else {
				if(file.getName().startsWith("Output_"))
				namepresent=true;
			
		}
		
	}
	if(!namepresent)
	{
		//Artifact need to have Video OUtput folder or file starting with OUTput_
	}

	}

	@Override
	public void sizeCountValidator(String pathName) {
		// TODO Auto-generated method stub
		File file = new File(pathName);
		long size=0;
		long count =0;
		if (file.isDirectory()) {
			size=FileUtils.sizeOfDirectory(file);
			count=file.list().length;
		}
		else {
			size=FileUtils.sizeOf(file);
			count= 1;
		}
	if(size==0){
		//O GB
	}
	else if(count==0) {
		//Artifact is empty
	}
	}
	
	public static void main(String [] args){
		String dept =args[0];
		String type = args[1];
		String pathName = args[2];
		LangValidator validator = new LangValidator();
		if (dept.equals("impressions") || type.equals("backup")) {
			validator.backupValidator( pathName );
			
		}
		else if((dept.equals("glp")|| dept.equals("ilp")) && type.equals("archive"))  {
			validator.langArtifactValidator(pathName);
		}
	}
}
      