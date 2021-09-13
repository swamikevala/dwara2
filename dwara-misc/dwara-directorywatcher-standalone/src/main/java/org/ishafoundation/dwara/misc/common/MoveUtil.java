package org.ishafoundation.dwara.misc.common;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveUtil {

	private static Logger logger = LoggerFactory.getLogger(MoveUtil.class);
	
	public static void move(Path srcPath, final Path destPath) throws Exception{
		Files.createDirectories(destPath);
		Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.move(file, Paths.get(destPath.toString(), file.getFileName().toString()), StandardCopyOption.ATOMIC_MOVE);
				return FileVisitResult.CONTINUE;
			}
		});

		logger.info(srcPath + " moved succesfully to " + destPath.getParent());

		FileUtils.deleteDirectory(srcPath.toFile());
	}
}
