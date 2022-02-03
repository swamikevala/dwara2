package org.ishafoundation.dwaraapi.commandline.remote.scp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

/**
 * This class copies transcoding files using JSCH from ingest server to catdv server
 *
 */
@Component
public class SecuredCopier {
	
	static Logger logger = LoggerFactory.getLogger(SecuredCopier.class);
	
	public void copyFrom(Session jschSession, String remoteFilePath, String localFilePath) throws Exception{
		 ChannelSftp channelSftp =  null;
        try {
        	Channel sftp = jschSession.openChannel("sftp");

	        // 5 seconds timeout
	        sftp.connect(5000);
	
	        channelSftp = (ChannelSftp) sftp;
	
	        // download file from remote server to local
	        channelSftp.get(remoteFilePath, localFilePath);
        }
        finally {
        	if(channelSftp != null)
        		channelSftp.exit();	
		}
        
	}
	
	public void copyFrom(Session session, String from, String to, String fileName) throws Exception {
        from = from + File.separator + fileName;
        String prefix = null;

        if (new File(to).isDirectory()) {
            prefix = to + File.separator;
        }

        // exec 'scp -f rfile' remotely
        String command = "scp -f " + from;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] buf = new byte[1024];

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        while (true) {
            int c = checkAck(in);
            if (c != 'C') {
                break;
            }

            // read '0644 '
            in.read(buf, 0, 5);

            long filesize = 0L;
            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    // error
                    break;
                }
                if (buf[0] == ' ') break;
                filesize = filesize * 10L + (long) (buf[0] - '0');
            }

            String file = null;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }

            logger.debug("file-size=" + filesize + ", file=" + file);

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // read a content of lfile
            FileOutputStream fos = new FileOutputStream(prefix == null ? to : prefix + file);
            int foo;
            while (true) {
                if (buf.length < filesize) foo = buf.length;
                else foo = (int) filesize;
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    // error
                    break;
                }
                fos.write(buf, 0, foo);
                filesize -= foo;
                if (filesize == 0L) break;
            }

            if (checkAck(in) != 0) {
            	throw new Exception("\"C0644 filesize filename\" ack failed");
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            try {
                if (fos != null) fos.close();
            } catch (Exception ex) {
                
            }
        }

        channel.disconnect();
        session.disconnect();
    }

	
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