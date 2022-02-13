package org.ishafoundation.dwaraapi.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.ishafoundation.dwaraapi.context.CryptoContext;

public class CryptoUtil {

	public static void encrypt(CryptoContext cryptoContext) throws Exception{
		crypt(Cipher.ENCRYPT_MODE, cryptoContext);
	}
	
	public static void decrypt(CryptoContext cryptoContext) throws Exception{
		crypt(Cipher.DECRYPT_MODE, cryptoContext);
	}
	
	private static void crypt(int opMode, CryptoContext cryptoContext) throws Exception{
		InputStream in = null;
		OutputStream out = null;
        try {
            SecretKey key = getAESKeyFromPassword(cryptoContext.getPassword().toCharArray(), cryptoContext.getSalt().getBytes(StandardCharsets.UTF_8));
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(cryptoContext.getIv().getBytes(StandardCharsets.UTF_8));
            in = new BufferedInputStream(new FileInputStream(cryptoContext.getInputFilepathname()));
            out = new BufferedOutputStream(new FileOutputStream(cryptoContext.getOutputFilepathname()));
            Cipher c = Cipher.getInstance(cryptoContext.getTransformation());
            c.init(opMode, key, paramSpec);
            out = new CipherOutputStream(out, c);
            int count = 0;
            byte[] buffer = new byte[cryptoContext.getBufferSize()];
            while (in.available() > 0) {
            		
            	count = in.read(buffer);
				if (count < 0)
					continue;

                out.write(buffer, 0, count);
            }
        } finally {
        	if(in != null)
        		in.close();
        	if(out != null)
        		out.close();
        }

		
	}
	
    // Password derived AES 256 bits secret key
    private static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // iterationCount = 65536
        // keyLength = 256
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;

    }
}
