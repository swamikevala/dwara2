package deletethis;

import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;

public class EnumConstantComparisonCheck {
	public static void main(String[] args) {
		StorageJob sj = new StorageJob();
		sj.setStorageOperation(StorageOperation.FORMAT);
		
		
		if(StorageOperation.FORMAT == sj.getStorageOperation())
			System.out.println("ok good");
		
		if(StorageOperation.FORMAT.name().equals(sj.getStorageOperation().name()))
			System.out.println("ok good too");
	}
}
