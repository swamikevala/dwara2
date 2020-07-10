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
	
	// TODO change this to private
	// calculates a file's Archive block END based on its size
	public static int getFileArchiveBlockEnd(int fileHeaderBlocks, int fileArchiveBlockOffset, Long fileSize, int archiveformatBlocksize) {
		int fileArchiveBlocksCount = (int) Math.ceil(fileSize/archiveformatBlocksize);
		return fileArchiveBlockOffset + fileHeaderBlocks + fileArchiveBlocksCount - 1; // - 1 because starts with 0 //(int) (fileSize / archiveformatBlocksize) - 1;//
	}
	
	// calculates a file's Volume block END - using running archiveblockoffset...
	public static int getFileVolumeBlockEnd(String fileName, int fileArchiveBlockOffset, Long fileSize, int archiveformatBlocksize, int blockingFactor){
		int fileHeaderBlocks = getFileHeaderBlocks(fileName);
			
		int lastFileEndArchiveBlock = getFileArchiveBlockEnd(fileHeaderBlocks, fileArchiveBlockOffset, fileSize, archiveformatBlocksize);
		
		int fileVolumeBlocksCount = (int) Math.ceil(lastFileEndArchiveBlock/blockingFactor);
		int artifactTotalVolumeBlocks = fileVolumeBlocksCount;
		
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

}
