package org.ishafoundation.dwaraapi.test_impl.entrypoint.resource.mapper;

import org.ishafoundation.dwaraapi.tape.drive.status.MtStatus;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.test_impl.db.model.Test_DataTransferElement;
import org.ishafoundation.dwaraapi.test_impl.db.model.Test_MtStatus;
import org.ishafoundation.dwaraapi.test_impl.db.model.Test_StorageElement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Test_TapeObjectsMapper {

	DataTransferElement getDataTransferElement(Test_DataTransferElement test_DataTransferElement);
	
	StorageElement getStorageElement(Test_StorageElement test_StorageElement);
	
	MtStatus getMtStatus(Test_MtStatus test_MtStatus);

}

