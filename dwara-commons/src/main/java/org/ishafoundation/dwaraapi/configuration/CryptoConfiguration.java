package org.ishafoundation.dwaraapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="crypto")
public class CryptoConfiguration {
	
	private String password;
	
	private String iv;
	
	private String salt;
	
	private String secretKeyFactoryAlgorithm; // PBKDF2WithHmacSHA256
	
	private String secretKeyGeneratorAlgorithm; //AES

	private String transformation; //name of the transformation to be used in Cipher instance e.g., AES/CBC/PKCS5Padding
	
	private int bufferSize;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getSecretKeyFactoryAlgorithm() {
		return secretKeyFactoryAlgorithm;
	}

	public void setSecretKeyFactoryAlgorithm(String secretKeyFactoryAlgorithm) {
		this.secretKeyFactoryAlgorithm = secretKeyFactoryAlgorithm;
	}

	public String getSecretKeyGeneratorAlgorithm() {
		return secretKeyGeneratorAlgorithm;
	}

	public void setSecretKeyGeneratorAlgorithm(String secretKeyGeneratorAlgorithm) {
		this.secretKeyGeneratorAlgorithm = secretKeyGeneratorAlgorithm;
	}

	public String getTransformation() {
		return transformation;
	}

	public void setTransformation(String transformation) {
		this.transformation = transformation;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
