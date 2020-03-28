package org.ishafoundation.dwaraapi.storage.constants;

public enum StorageOperation {
	// TODO Avoid this....
	WRITE(10000), 
	READ(2);
	
	private int storageOperationId;
	
	StorageOperation(int storageOperationId) {
        this.storageOperationId = storageOperationId;
    }

	public int getStorageOperationId() {
		return storageOperationId;
	}  
}