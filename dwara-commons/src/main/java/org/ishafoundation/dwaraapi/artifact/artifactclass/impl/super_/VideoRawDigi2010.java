package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import java.util.List;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;

public class VideoRawDigi2010 implements Artifactclass{

	private static final String DV_CODE_REGEX = "^[A-Z]{1,2}[\\d]{1,5}$";
	private static final String DVCAPTURED_CODE_REGEX = "^[\\d]+_D[Vv]-[Cc]aptured";
	

    private String DV_CODE_REGEX2 = "[A-Z]{1,2}[\\d]{1,5}";
    private String SHIFTED_DV_REGEX = "^" + DV_CODE_REGEX2 + "_Shifted-to-" + DV_CODE_REGEX2;
    private Pattern SHIFTED_DV_REGEX_PATTERN = Pattern.compile(SHIFTED_DV_REGEX);

	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		
		if(proposedName.matches(DV_CODE_REGEX)) {
			artifactAttributes.setPreviousCode(proposedName);
			
		} else if(proposedName.matches(DVCAPTURED_CODE_REGEX + "_" + DV_CODE_REGEX) {  //5902_DV-Captured_A1929_Inner-Engineering_Tampa_Day3_Cam2_Tape2_10-Nov-06
			int idx = ordinalIndexOf(proposedName, "_", 3);
			String prefix = proposedName.substring(0,idx); //5902_DV-Captured_A1929
			int oldSeq = Integer.parseInt(prefix.substring(0, prefix.indexOf("_"))); //5902
			
			artifactAttributes.setPreviousCode(prefix.replace("_D[Vv]-[Cc]aptured", ""));  //5902_A1929
			artifactAttributes.setSequenceNumber(oldSeq);
		}
		return artifactAttributes;
	}

	@Override
	public boolean validateImport(Artifact nthArtifact) throws Exception {
		String artifactNameAsInCatalog = nthArtifact.getName();
		
	    List<org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File> artifactFileList = nthArtifact.getFile();
	    long artifactSize = 0L;
	    
	    if(artifactFileList != null) {
		    fileCount = artifactFileList.size();
			for (org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File nthFile : artifactFileList) {
				if(!Boolean.TRUE.equals(nthFile.getDirectory())){
					artifactSize += nthFile.getSize();
				}
			}
	    }
		
	    if((SHIFTED_DV_REGEX_PATTERN.matcher(artifactNameAsInCatalog).matches() && artifactSize < 1024)
	    		|| artifactNameAsInCatalog.equals("Z150_Shifted-to-Br-Category_Sw-Nirvichara-has-catalog-numbers_19-Dec-2010")
	    		|| artifactNameAsInCatalog.equals("Z151_Shifted-to-Br-Category_Sw-Nirvichara-has-catalog-numbers_19-Dec-2010")
	    		|| artifactNameAsInCatalog.equals("Z1179_Shifted-to-Brahmachari-Material_No-new-category-code-given-by-Swami-Nirvichara_15-Jan-2011"))
	    	throw new Exception ("Shifted-to folder");
	    
		return true;
	}
}		