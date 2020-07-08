package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

public class TarBlockCalculatorUtil {
	
	private static int FILENAME_CHARACTER_THRESHOLD_PER_TAR_BLOCK = 100;
	
	public static int getBlockingFactor(int archiveBlock, int volumeBlock) {
		return volumeBlock/archiveBlock;
	}
	
	// calculates the no. of Archive blocks a file consumes based on its size - excludes the file name header
	public static int getFileArchiveBlocksCount(Long fileSize, int archiveformatBlocksize) {
		int archiveBlocksUsedByThisFile = (int) (fileSize / archiveformatBlocksize);
		if(fileSize % archiveformatBlocksize > 0) // If there is remainder bits means it still occupies that block...
			archiveBlocksUsedByThisFile = archiveBlocksUsedByThisFile + 1; // +1 because - we need to round it "UP"... Eg, 99.09 = 100 or 77.75 = 78.

		return archiveBlocksUsedByThisFile;
	}

	// calculates the no. of Volume block's a file consumes based on archiveBlock
	public static int getFileVolumeBlocksCount(int fileArchiveBlock, int blockingFactor) {
		int fileVolumeBlock = (int) (fileArchiveBlock / blockingFactor);
		if(fileArchiveBlock % blockingFactor > 0) // If there is remainder bits means it still occupies that block...
			fileVolumeBlock = fileVolumeBlock + 1; // +1 because - we need to round it "UP"... Eg, 99.09 = 100 or 77.75 = 78.
		
		return fileVolumeBlock;
	}

	// TODO : better this logic... 1 or 3 blocks based on 100 character limit or something better identification?
	public static int getFileHeaderBlocks(String fileName){
		int fileHeaderBlocks = 1;
		if(fileName.length() > FILENAME_CHARACTER_THRESHOLD_PER_TAR_BLOCK) {
			fileHeaderBlocks = 3;
		}
		return fileHeaderBlocks;	
	}
	
	// TODO change this to private
	// calculates a file's Archive block END based on its size
	public static int getFileArchiveBlockEnd(int fileHeaderBlocks, int fileArchiveBlockOffset, Long fileSize, int archiveformatBlocksize) {
		return fileArchiveBlockOffset + fileHeaderBlocks + getFileArchiveBlocksCount(fileSize, archiveformatBlocksize) - 1; // - 1 because starts with 0 //(int) (fileSize / archiveformatBlocksize) - 1;//
	}
	
	// calculates a file's Volume block END - using running archiveblockoffset...
	public static int getFileVolumeBlockEnd(String fileName, int fileArchiveBlockOffset, Long fileSize, int archiveformatBlocksize, int blockingFactor){
		int fileHeaderBlocks = getFileHeaderBlocks(fileName);
			
		int lastFileEndArchiveBlock = getFileArchiveBlockEnd(fileHeaderBlocks, fileArchiveBlockOffset, fileSize, archiveformatBlocksize);
		
		int artifactTotalVolumeBlocks = getFileVolumeBlocksCount(lastFileEndArchiveBlock, blockingFactor);
		
		if(artifactTotalVolumeBlocks == 0)
			return 0;
		return artifactTotalVolumeBlocks -1;//TODO : starting 0 entries show as - 1; // -1 because starts with 0
	}
	
	// useful when executing the restore
	//block 136638: -rwxrwxrwx aravindhpr/aravindhpr 1638625295 2020-04-16 12:08 Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6/DCIM/104GOPRO/GOPR6925.MP4
	// 136638 - (133 * 1024) + 3
	public static int getSkipByteCount(String fileName, int fileArchiveBlock, int archiveformatBlockize, int seekedVolumeBlock, int blockingFactor) {
		return (fileArchiveBlock + TarBlockCalculatorUtil.getFileHeaderBlocks(fileName)  - (seekedVolumeBlock * blockingFactor)) * archiveformatBlockize;
	}
	
	
	
	// TODO : Remove these methods - Start
	
	public static int getFileVolumeBlockEnd(String fileName, int archiveformatBlocksize, Long fileSize, int volumeBlocksize){
		int fileHeaderBlocks = getFileHeaderBlocks(fileName);

		int artifactTotalVolumeBlocks = getFileVolumeBlocksCount(fileSize + (fileHeaderBlocks * archiveformatBlocksize), volumeBlocksize);
		
		return artifactTotalVolumeBlocks - 1; // -1 because starts with 0
	}
	

	// calculates the no. of Volume block's a file consumes based on filesize - excludes the file name header
	public static int getFileVolumeBlocksCount(Long fileSize, int volumeBlocksize) {
		int fileVolumeBlock = (int) (fileSize / volumeBlocksize);
		if(fileSize % volumeBlocksize > 0) // If there is remainder bits means it still occupies that block...
			fileVolumeBlock = fileVolumeBlock + 1; // +1 because - we need to round it "UP"... Eg, 99.09 = 100 or 77.75 = 78.
		
		return fileVolumeBlock;
	}

	// TODO : Remove these methods - End
	
	public static void main(String[] args) {
		
		
		
		Long fileSize = 12878883L;
//		Long fileSize = 24063587L;
		
		//bru = 2048, tar = 512
		int archiveformatBlocksize = 512;
		int volumeBlocksize = 262144;
		
		int blockingFactor = getBlockingFactor(archiveformatBlocksize, volumeBlocksize);
		System.out.println(blockingFactor); // expected 512
				
		int fileArchiveBlock = getFileArchiveBlocksCount(fileSize, archiveformatBlocksize);
		System.out.println(fileArchiveBlock); // expected 25155
		
		int fileVolumeBlock = getFileVolumeBlocksCount(fileArchiveBlock, blockingFactor);
		System.out.println(fileVolumeBlock + " - " + (fileSize/volumeBlocksize)); // 50 - 49
		
		
		String fileName_3Blocks = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9_1593709409451\\2 CD\\Dummy\\20190701_074746.mp4";
		int fhb = getFileHeaderBlocks(fileName_3Blocks);
		System.out.println(fhb); // 3
		
		String fileName_1Block = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9_1593709409451";
		fhb = getFileHeaderBlocks(fileName_1Block);
		System.out.println(fhb); // 1
		
		fileSize = 7353665L;
		
		int eab = getFileArchiveBlockEnd(fhb, 72162, fileSize, archiveformatBlocksize);
		System.out.println(eab); // 86526

		int evb = getFileVolumeBlockEnd(fileName_3Blocks, 72162, fileSize, archiveformatBlocksize, blockingFactor);
		System.out.println(evb); // 169
		
		fileSize = 1638625295L;
		evb = getFileVolumeBlockEnd(fileName_3Blocks, 136638, fileSize, archiveformatBlocksize, blockingFactor);
		System.out.println(evb); // 6518
		
		int sbc = getSkipByteCount(fileName_3Blocks, 512, 136638, 133, 1024);
		System.out.println(sbc); //449
	}
}
