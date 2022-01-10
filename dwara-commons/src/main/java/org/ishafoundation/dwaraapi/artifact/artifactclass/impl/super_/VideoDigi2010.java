package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class VideoDigi2010 implements Artifactclass{

	private static final String NUMBER_REGEX = "[\\d]+";  
	private static final String DV_CODE_REGEX = "[A-Z]{1,2}[\\d]{1,5}";
	private static final String DV_CAPTURED_REGEX = "_D[Vv]-[Cc]aptured_";
	private static final String SHIFTED_FROM_REGEX = "_Shifted-From-";  
    private String SHIFTED_DV_REGEX = "^(" + DV_CODE_REGEX + ")_Shifted-to-(" + DV_CODE_REGEX + ")";
    private Pattern SHIFTED_DV_REGEX_PATTERN = Pattern.compile(SHIFTED_DV_REGEX);

	private static final Map<String, String> SHIFTED_TO_MAP = new HashMap<String, String>();
	
	@PostConstruct
	public void setUp() throws Exception {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("ArtifactnameTo_ShiftedTo-ContextArtifactName_Mapping.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        String line = null;
        
        while ((line = br.readLine()) != null) {
//        	SHIFTED_TO_SET.add(line);
        	String[] parts = line.split("~!~");
        	String prevSeqCode = parts[0];
        	String artifactName = parts[1];
        	SHIFTED_TO_MAP.put(prevSeqCode, artifactName);
        }
	}
	
	
	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		
		if(proposedName.matches("^" + DV_CODE_REGEX + "$")) {
			if(SHIFTED_TO_MAP.containsKey(proposedName))
				artifactAttributes.setPreviousCode(getPreviousCodeFromShiftedToName(SHIFTED_TO_MAP.get(proposedName)));
			
			artifactAttributes.setPreviousCode(proposedName);
			
		} else if(proposedName.matches("^" + NUMBER_REGEX + DV_CAPTURED_REGEX  + DV_CODE_REGEX)) {  //5902_DV-Captured_A1929_Inner-Engineering_Tampa_Day3_Cam2_Tape2_10-Nov-06
			int idx = ordinalIndexOf(proposedName, "_", 3);
			String prefix = proposedName.substring(0, idx); //5902_DV-Captured_A1929
			int oldSeq = Integer.parseInt(prefix.substring(0, prefix.indexOf("_"))); //5902
			artifactAttributes.setPreviousCode(prefix.replace(DV_CAPTURED_REGEX, ""));  //5902_A1929
			
			//6719_DV-Captured_P204_Shifted-From-M970_Residents-Meet_IIIS_8-Apr-2009_Tape2_Cam1
			if(proposedName.matches("^" + NUMBER_REGEX + DV_CAPTURED_REGEX  + DV_CODE_REGEX + SHIFTED_FROM_REGEX + DV_CODE_REGEX)) {
				idx = ordinalIndexOf(proposedName, "_", 4);
				prefix = proposedName.substring(0, idx);   //6719_DV-Captured_P204_Shifted-From-M970
				String prevCodes = prefix.replace(DV_CAPTURED_REGEX, "_").replace(SHIFTED_FROM_REGEX, "_");   //6719_P204_M970
				artifactAttributes.setPreviousCode(prevCodes);
			}
			artifactAttributes.setSequenceNumber(oldSeq);
		}
		return artifactAttributes;
	}
	
	private String getPreviousCodeFromShiftedToName(String shiftedToArtifactName) {
		Matcher m1 = SHIFTED_DV_REGEX_PATTERN.matcher(shiftedToArtifactName);
		if(m1.find()) {
			return m1.group(2) + "_" + m1.group(1);
		}
		return null;
	}


	public static int ordinalIndexOf(String str, String substr, int n) {
	    int pos = str.indexOf(substr);
	    while (--n > 0 && pos != -1)
	        pos = str.indexOf(substr, pos + 1);
	    return pos;
	}
	
	@Override
	public boolean validateImport(Artifact nthArtifact) throws Exception {
		String artifactNameAsInCatalog = nthArtifact.getName();
		
	    List<org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File> artifactFileList = nthArtifact.getFile();
	    long artifactSize = 0L;
	    
	    if(artifactFileList != null) {
			for (org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File nthFile : artifactFileList) {
				if(!Boolean.TRUE.equals(nthFile.getDirectory())){
					artifactSize += nthFile.getSize();
				}
			}
	    }
		
	    if((SHIFTED_DV_REGEX_PATTERN.matcher(artifactNameAsInCatalog).find() && artifactSize < 1024) // matches() or find() - regex seems to be made for find()
	    		|| artifactNameAsInCatalog.equals("Z150_Shifted-to-Br-Category_Sw-Nirvichara-has-catalog-numbers_19-Dec-2010")
	    		|| artifactNameAsInCatalog.equals("Z151_Shifted-to-Br-Category_Sw-Nirvichara-has-catalog-numbers_19-Dec-2010")
	    		|| artifactNameAsInCatalog.equals("Z1179_Shifted-to-Brahmachari-Material_No-new-category-code-given-by-Swami-Nirvichara_15-Jan-2011"))
	    	throw new Exception ("Shifted-to placeholder folder");
	    
//	    if(SHIFTED_TO_SET.contains(artifactNameAsInCatalog))
//	    	throw new Exception ("Dummy Shifted-to folder");
		return true;
	}
}		

