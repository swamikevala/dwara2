package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

public class TarBlockCalculatorUtil {
	
	private static int FILENAME_CHARACTER_THRESHOLD_PER_TAR_BLOCK = 100;
	
	public static int getBlockingFactor(int archiveBlock, int volumeBlock) {
		return volumeBlock/archiveBlock;
	}

	// TODO : better this logic... 1 or 3 blocks based on 100 character limit or something better identification?
	public static int getFileHeaderBlocks(String fileName){
		int fileHeaderBlocks = 1;
		if(fileName.length() > FILENAME_CHARACTER_THRESHOLD_PER_TAR_BLOCK) {
			fileHeaderBlocks = 3;
		}
		return fileHeaderBlocks;	
	}
	
	public static int getFileVolumeBlock(int artifactStartVolumeBlock, int archiveBlock, int blockingFactor) {
		return artifactStartVolumeBlock + archiveBlock/blockingFactor; // Not ceiling the value as we need to -1 anyway because of 0 start
	}
	
	// calculates a file's Volume END  block - using the running archiveblock of each file... where archiveBlock is the starting block of the archive...
	public static int getFileVolumeEndBlock(String fileName, int fileArchiveBlock, Long fileSize, double archiveformatBlocksize, double blockingFactor){
		int fileHeaderBlocks = getFileHeaderBlocks(fileName);
		
		// Total no. of archive blocks used by the file...
		int fileArchiveBlocksCount = (int) Math.ceil(fileSize/archiveformatBlocksize);
		int file_Archive_EndBlock = fileArchiveBlock + fileHeaderBlocks + fileArchiveBlocksCount;
		
		int fileVolumeBlocksCount = (int) Math.ceil(file_Archive_EndBlock/blockingFactor);
		int artifactTotalVolumeBlocks = fileVolumeBlocksCount;
		
		return artifactTotalVolumeBlocks - 1; // -1 because of 0 starts 
	}

	// useful when executing the restore
	//block 136638: -rwxrwxrwx aravindhpr/aravindhpr 1638625295 2020-04-16 12:08 Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6/DCIM/104GOPRO/GOPR6925.MP4
	// 136638 + 3 - (133 * 1024)
	public static int getSkipByteCount(String fileName, int fileArchiveBlock, int archiveformatBlockize, int blockingFactor) {
		return (fileArchiveBlock + TarBlockCalculatorUtil.getFileHeaderBlocks(fileName)  - ((fileArchiveBlock/blockingFactor)  * blockingFactor)) * archiveformatBlockize;
	}

}
