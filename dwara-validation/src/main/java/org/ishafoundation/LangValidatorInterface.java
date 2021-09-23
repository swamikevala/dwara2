package org.ishafoundation.validation;

import java.util.List;

import org.ishafoundation.dwaraapi.staged.scan.Error;

public interface LangValidatorInterface {
	
	public void langArtifactValidator(String pathName );
	public void langYTArtifactValidator(String ArtifactName  );
	public void langNYTArtifactValidator(String ArtifactName );
	public void basicFileNameValidation(String fileName );
	public void languageValidator(String lang );
	public String videoDifferentiator(String name);
	public void folderValidator(String pathName);
	public void sizeCountValidator(String pathName);

}
