package org.ishafoundation.dwaraapi.storage.constants;

public enum StorageOperation {
	WRITE(1), 
	READ(2);
	
	private int storageOperationId;
	
	StorageOperation(int storageOperationId) {
        this.storageOperationId = storageOperationId;
    }

	public int getStorageOperationId() {
		return storageOperationId;
	}  
}