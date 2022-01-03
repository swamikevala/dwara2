import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class CatalogConvert {

	static final String csv = "C:\\Users\\Lenovo\\Documents\\dwara-import-testing\\import.csv";
	static final String catalogs = "C:\\Users\\Lenovo\\Documents\\dwara-import-testing\\brucats";
	static final String converted = "C:\\Users\\Lenovo\\Documents\\dwara-import-testing\\converted";
	
//	public static void main(String args[]) throws IOException {
	public static void mainn(String args[]) throws IOException {	
		InputStream in = new FileInputStream(csv);
		InputStreamReader inr = new InputStreamReader(in, Charset.forName("UTF-8"));
		BufferedReader binr = new BufferedReader(inr);
		
		int lineCount = 0;
		int successful = 0;
		
		String barcode;
		String brucode;
	
		while(binr.ready()) {
			String line = binr.readLine();
			if (line.length() != 21 || !line.substring(8,9).equals(",")) {
				throw new IllegalArgumentException("Line " + lineCount + " does not have correct format");
			}
			barcode = line.substring(0,8);
			brucode = line.substring(9,21);
			lineCount++;
			
			Path source = Paths.get(catalogs + "\\BRU-" + brucode + ".gz");
			Path target = Paths.get(converted + "\\" + barcode);
			
			if (Files.notExists(source)) {
				System.err.printf("The file %s doesn't exist", source);
				return;
			}
			
			try {
				decompressGzip(source, target);
				ReversedLinesFileReader rlfr = new ReversedLinesFileReader(target.toFile(), Charset.forName("UTF-8"));
				
				String startedStr = null;
				String completedStr = null;
				
				for (int i = 0; i < 12; i++) {
					completedStr = rlfr.readLine();
				}
				
				startedStr = rlfr.readLine();
				
				System.out.println(startedStr);
				
				String formattedStartedStr = formatDateString(startedStr, 10);
				String formattedCompletedStr = formatDateString(completedStr, 12);

				Files.move(target, target.resolveSibling(target.getFileName() + "_" + formattedStartedStr + "_" + formattedCompletedStr));
				successful++;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		binr.close();
		System.out.println("Total listed catalogs: " + lineCount);
		System.out.println("Total successfully converted: " + successful);
	}
	
	
	public static void decompressGzip(Path source, Path target) throws IOException {

        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(source.toFile()));
             FileOutputStream fos = new FileOutputStream(target.toFile())) {

            // copy GZIPInputStream to FileOutputStream
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }
	
	private static String formatDateString(String dateStr, int offset) {
		
		String newDateStr = dateStr.substring(offset).replace("  ", " ");
		System.out.println(newDateStr);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH);
		LocalDateTime dateTime = LocalDateTime.parse(newDateStr, formatter);
		
		return dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy-HH-mm-ss"));
	}
}
