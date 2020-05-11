package z_deletethis;

import java.io.File;

import org.ishafoundation.dwaraapi.utils.CRCUtil;

public class CRCTests {
	public static void main(String[] args) {
		String filePathname = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_91589021652143\\1 CD\\20190701_071239.mp4";
		
		File archivedFile = new File("C:\\data\\tmp\\restore" + File.separator + filePathname);
		Long archivedFileCrc = CRCUtil.getCrc(archivedFile);
		System.out.println(archivedFileCrc);
		
		File srcFile = new File("C:\\data\\ingested" + File.separator + filePathname);
		Long srcFileCrc = CRCUtil.getCrc(srcFile);
		System.out.println(srcFileCrc);
		
	}
}
