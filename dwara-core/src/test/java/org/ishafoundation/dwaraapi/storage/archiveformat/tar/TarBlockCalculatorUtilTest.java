package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import org.junit.Test;

public class TarBlockCalculatorUtilTest {
	
	int volumeBlocksize = 524288;//524288; //262144; //1048576;
	int tarBlocksize = 512;
	int blockingFactor = volumeBlocksize/tarBlocksize; // 1024

	/*
		[root@test-ingest 1G-5G]# tar cvvv -R -b 512 -f /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9
		block 0: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/
		block 1: -rwxrwxrwx root/root         0 2019-08-13 15:33 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/._.DS_Store
		block 2: -rwxrwxrwx root/root         0 2019-08-13 15:33 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/.DS_Store
		block 3: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/
		block 4: -rwxrwxrwx root/root  12878883 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074746.mp4
		block 25160: -rwxrwxrwx root/root  24063587 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4
		block 72161: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/
		block 72162: -rwxrwxrwx root/root   7353665 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/20190701_071239.mp4
	*/
	//@Test	
//	public void test1() throws Exception{
//		//block 0: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/
//		String fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/";
//		int fhb = TarBlockCalculatorUtil.getFileHeaderBlocks(fileName);
//		System.out.println(fhb); // 1
//		//assertEquals(1, fhb);
//		
//		
//		Long fileSize = 0L;
//		int fileArchiveStartBlock = 0;
//		
//		int eab = TarBlockCalculatorUtil.getFileArchiveBlockEnd(fhb, fileArchiveStartBlock, fileSize, tarBlocksize);
//		System.out.println(eab); // 0
//		//assertEquals(0, eab);
//		
//		// block 4: -rwxrwxrwx root/root  12878883 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074746.mp4	
//		fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074746.mp4";
//		fhb = TarBlockCalculatorUtil.getFileHeaderBlocks(fileName);
//		System.out.println(fhb); // 1
//		//assertEquals(1, fhb);
//		
//		
//		fileSize = 12878883L;
//		fileArchiveStartBlock = 4;
//		
//		eab = TarBlockCalculatorUtil.getFileArchiveBlockEnd(fhb, fileArchiveStartBlock, fileSize, tarBlocksize);
//		System.out.println(eab); // 25159
//		//assertEquals(25159, eab);
//		
//		//block 25160: -rwxrwxrwx root/root  24063587 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4
//		fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4";
//		fhb = TarBlockCalculatorUtil.getFileHeaderBlocks(fileName);
//		System.out.println(fhb); // 1
//		//assertEquals(1, fhb);
//		
//		
//		fileSize = 24063587L;
//		fileArchiveStartBlock = 25160;
//		
//		eab = TarBlockCalculatorUtil.getFileArchiveBlockEnd(fhb, fileArchiveStartBlock, fileSize, tarBlocksize);
//		System.out.println(eab); // 72160
//		//assertEquals(72160, eab);
//		
//
//		//block 72161: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/
//		fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/";
//		fhb = TarBlockCalculatorUtil.getFileHeaderBlocks(fileName);
//		System.out.println(fhb); // 1
//		//assertEquals(1, fhb);
//		
//		
//		fileSize = 0L;
//		fileArchiveStartBlock = 72161;
//		
//		eab = TarBlockCalculatorUtil.getFileArchiveBlockEnd(fhb, fileArchiveStartBlock, fileSize, tarBlocksize);
//		System.out.println(eab); // 72161
//		//assertEquals(72161, eab);
//	}
	
	/*
		block 0: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/
		block 1: -rwxrwxrwx root/root         0 2019-08-13 15:33 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/._.DS_Store
		block 2: -rwxrwxrwx root/root         0 2019-08-13 15:33 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/.DS_Store
		block 3: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/
		block 4: -rwxrwxrwx root/root  12878883 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074746.mp4
		block 25160: -rwxrwxrwx root/root  24063587 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4
		block 72161: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/
		block 72162: -rwxrwxrwx root/root   7353665 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/20190701_071239.mp4
	*/

	@Test	
	public void test2() throws Exception{
		
		//block 0: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/
		String fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/";
		Long fileSize = 0L;
		int fileArchiveStartBlock = 0;
						
		int evb = TarBlockCalculatorUtil.getFileVolumeEndBlock(fileName, fileArchiveStartBlock, fileSize, tarBlocksize, blockingFactor);
		System.out.println("vb : " + evb);
		//assertEquals(0, evb);
		
		// block 4: -rwxrwxrwx root/root  12878883 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074746.mp4	
		fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074746.mp4";
		fileSize = 12878883L;
		fileArchiveStartBlock = 4;
		
		evb = TarBlockCalculatorUtil.getFileVolumeEndBlock(fileName, fileArchiveStartBlock, fileSize, tarBlocksize, blockingFactor);
		System.out.println("vb : " + evb);
		//assertEquals(24, evb); // 25 but starts with 0;
		
		//block 25160: -rwxrwxrwx root/root  24063587 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4
		fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4";
		fileSize = 24063587L;
		fileArchiveStartBlock = 25160;
		
		evb = TarBlockCalculatorUtil.getFileVolumeEndBlock(fileName, fileArchiveStartBlock, fileSize, tarBlocksize, blockingFactor);
		System.out.println("vb : " + evb);
		//assertEquals(70, evb); // 46 + 25
		

		//block 72161: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/
		fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/";
		fileSize = 0L;
		fileArchiveStartBlock = 72161;
		
		evb = TarBlockCalculatorUtil.getFileVolumeEndBlock(fileName, fileArchiveStartBlock, fileSize, tarBlocksize, blockingFactor);
		System.out.println("vb : " + evb);
		
		fileName = "Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6/DCIM/104GOPRO/GOPR6924.MP4";
		fileSize = 69953193L;
		fileArchiveStartBlock = 7;
		
		evb = TarBlockCalculatorUtil.getFileVolumeEndBlock(fileName, fileArchiveStartBlock, fileSize, tarBlocksize, blockingFactor);
		System.out.println("vb : " + evb); 
		
		fileName = "Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6/DCIM/104GOPRO/GOPR6925.MP4";
		fileSize = 1638625295L;
		fileArchiveStartBlock = 136638;
		
		evb = TarBlockCalculatorUtil.getFileVolumeEndBlock(fileName, fileArchiveStartBlock, fileSize, tarBlocksize, blockingFactor);
		System.out.println("vb : " + evb);
		//assertEquals(71, evb);
	}

	private Long getUsedFileSize(Long fileSize) {
		return fileSize;
		//return (long) (fileSize + (fileSize * 0.125));
	}
	
	@Test	
	public void test4() throws Exception{
		System.out.println("***");
		//block 136638: -rwxrwxrwx aravindhpr/aravindhpr 1638625295 2020-04-16 12:08 Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6/DCIM/104GOPRO/GOPR6925.MP4
		// (136638 + 3 - (136638/1024 * 1024)) * 512
		int fileArchiveBlock = 136638;
		String fileName = "Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6/DCIM/104GOPRO/GOPR6925.MP4";

		int sbc = TarBlockCalculatorUtil.getSkipByteCount(fileName, fileArchiveBlock,  tarBlocksize, blockingFactor);
		System.out.println(sbc);
		//assertEquals(449, sbc);
		
		//block 25160: -rwxrwxrwx root/root  24063587 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4
		// (25160 + 1 - (25160/1024 * 1024)) * 512
		fileArchiveBlock = 25160;
		fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4";

		sbc = TarBlockCalculatorUtil.getSkipByteCount(fileName, fileArchiveBlock, tarBlocksize, blockingFactor);
		System.out.println(sbc);
		//assertEquals(585, sbc);
		
		fileArchiveBlock = 3;
		fileName = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9";

		sbc = TarBlockCalculatorUtil.getSkipByteCount(fileName, fileArchiveBlock, tarBlocksize, blockingFactor);
		System.out.println(sbc);
	}
	 
}
