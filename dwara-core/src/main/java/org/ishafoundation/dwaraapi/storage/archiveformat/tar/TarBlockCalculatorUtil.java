package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

public class TarBlockCalculatorUtil {

	public static final int TAPELABEL_BLOCK = 1;
	
	//https://www.gnu.org/software/tar/manual/html_chapter/tar_9.html#SEC162
	public static final int TAPEMARK_BLOCK = 1;
	public static final int ARTIFACTLABEL_BLOCK = 1;
	public static final int NEXTARCHIVE_FRESH_START_BLOCK = 1;
	
	public static final int FIRSTARCHIVE_START_BLOCK = 0 + TAPELABEL_BLOCK + TAPEMARK_BLOCK;

	public static final int INCLUSIVE_BLOCK_ADJUSTER = 1;// + 1 so the start and end volume block boundaries are inclusive.. for eg., svb = 2, evb = 5, totalblocks = four blocks involved 2,3,4,5(so 5 - 2 + INCLUSIVE_BLOCK_ADJUSTER = 4)
	
	private static int FILENAME_CHARACTER_THRESHOLD_PER_TAR_BLOCK = 100;
	
	public static int getBlockingFactor(int archiveBlock, int volumeBlock) {
		return volumeBlock/archiveBlock;
	}

//	// TODO : better this logic... 1 or 3 blocks based on 100 character limit or something better identification?
//	public static int getFileHeaderBlocks(String fileName){
//		// TODO : we should get this from the file tbale...
////		int fileHeaderBlocks = 1;
////		if(fileName.length() >= FILENAME_CHARACTER_THRESHOLD_PER_TAR_BLOCK) { // Reference TarArchiveOutputStream.handleLongName().if (len >= TarConstants.NAMELEN)
////			fileHeaderBlocks = 3;
////		}
//		int fileHeaderBlocks = 3; // For Posix format its always 3 because of the pax header... 
//		return fileHeaderBlocks;	
//	}
	
	public static int getFileVolumeBlock(int artifactStartVolumeBlock, long archiveBlock, int blockingFactor) {
		return (int) (artifactStartVolumeBlock + archiveBlock/blockingFactor); // Not ceiling the value as we need to -1 anyway because of 0 start
	}
	
	// calculates a file's Volume END  block - using the running archiveblock of each file... where archiveBlock is the starting block of the file archive...
	public static int getFileVolumeEndBlock(String fileName, long fileArchiveBlock, int fileHeaderBlocks, Long fileSize, double archiveformatBlocksize, double blockingFactor){
		//int fileHeaderBlocks = getFileHeaderBlocks(fileName);
		
		// Total no. of archive blocks used by the file...
		int fileArchiveBlocksCount = (int) Math.ceil(fileSize/archiveformatBlocksize);
		long file_Archive_EndBlock = fileArchiveBlock + fileHeaderBlocks + fileArchiveBlocksCount;
		
		int fileVolumeBlocksCount = (int) Math.ceil(file_Archive_EndBlock/blockingFactor);
		return fileVolumeBlocksCount;
	}

	// useful when executing the restore
	//block 136638: -rwxrwxrwx aravindhpr/aravindhpr 1638625295 2020-04-16 12:08 Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6/DCIM/104GOPRO/GOPR6925.MP4
	// 136638 + 3 - (133 * 1024)
	public static int getSkipByteCount(String fileName, long fileArchiveBlock, int archiveformatBlockize, int blockingFactor) {
		//return (fileArchiveBlock + TarBlockCalculatorUtil.getFileHeaderBlocks(fileName)  - ((fileArchiveBlock/blockingFactor)  * blockingFactor)) * archiveformatBlockize;
		return (int) ((fileArchiveBlock - ((fileArchiveBlock/blockingFactor)  * blockingFactor)) * archiveformatBlockize);
	}

}
