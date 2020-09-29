package org.ishafoundation.dwaraapi.staged.scan;

import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.enumreferences.Domain;

public interface IStagedFileValidator {

	public List<Error> validate(StagedFileDetails stagedFileDetails, Domain domain);

}
