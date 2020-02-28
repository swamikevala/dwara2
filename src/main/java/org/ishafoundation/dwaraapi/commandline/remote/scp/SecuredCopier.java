package org.ishafoundation.dwaraapi.commandline.remote.scp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

/**
 * This class copies transcoding files using JSCH from ingest server to catdv server
 *
 */
@Component
public class SecuredCopier {
	
	static Logger logger = LoggerFactory.getLogger(SecuredCopier.class);
	
	public void copyTo(Session session, String localFilePath, String remoteFilePath) throws Exception {
		FileInputStream fis = null;
		try {
			// exec 'scp -t rfile' remotely
			String command = "scp " + "-prt \"" + remoteFilePath + "\"";
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
	
			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();
	
			channel.connect();
			logger.debug("Getting I/O streams for the scp command " + command);
			if (checkAck(in) != 0) {
				throw new Exception("Failed ack for getting I/O streams for the scp command " + command);
			}
			
			File _lfile = new File(localFilePath);
			logger.debug("Sending \"C0644 filesize filename\", where filename should not include '/'");
			long filesize = _lfile.length();
			command = "C0644 " + filesize + " ";
			if (localFilePath.lastIndexOf('/') > 0) {
				command += localFilePath.substring(localFilePath.lastIndexOf('/') + 1);
			} else {
				command += localFilePath;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				throw new Exception("\"C0644 filesize filename\" ack failed");
			}
	
			logger.debug("Sending the content of localFilePath");
			fis = new FileInputStream(localFilePath);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len);
			}
			fis.close();
			fis = null;
			
			logger.debug("Sending '\0'");
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				throw new Exception("scp Failed");
			}
			out.close();
	
			channel.disconnect();

	
			logger.info("scp completed copying " + localFilePath + " to " + remoteFilePath);
		}
		finally {
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
		}
	}

	private int checkAck(InputStream in) throws Exception {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');

			throw new Exception(sb.toString());
		}
		return b;
	}
}