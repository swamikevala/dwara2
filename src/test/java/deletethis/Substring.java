package deletethis;

import org.apache.commons.lang3.StringUtils;

public class Substring {
	public static void main(String[] args) {
		String tapeBarcode = "V5A001L7";
		String volID = StringUtils.substring(tapeBarcode, 0, 6);
		System.out.println(volID);
		String ltoGen = StringUtils.substring(tapeBarcode, 6, 8);
		System.out.println(ltoGen);
	}
}
