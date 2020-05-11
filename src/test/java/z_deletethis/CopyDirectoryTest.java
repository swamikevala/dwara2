package z_deletethis;

import org.apache.commons.io.FileUtils;

public class CopyDirectoryTest {
	public static void main(String[] args) throws Exception {
		java.io.File srcDir = new java.io.File("C:\\data\\ingested\\Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_91589022184955");
		java.io.File destDir = new java.io.File("C:\\data\\tmp\\restore");
		FileUtils.copyDirectoryToDirectory(srcDir, destDir);
	}
}
