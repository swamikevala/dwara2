package org.ishafoundation.dwaraapi.storage.storagetype.tape.mapper;

import org.ishafoundation.dwaraapi.db.model.mocks.MockDataTransferElement;
import org.ishafoundation.dwaraapi.db.model.mocks.MockMtStatus;
import org.ishafoundation.dwaraapi.db.model.mocks.MockStorageElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatus;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MockZmockEntityObjectsMapper {

	DataTransferElement getDataTransferElement(MockDataTransferElement test_DataTransferElement);
	
	StorageElement getStorageElement(MockStorageElement test_StorageElement);
	
	MtStatus getMtStatus(MockMtStatus test_MtStatus);

}

