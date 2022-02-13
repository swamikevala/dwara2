package org.ishafoundation.dwaraapi.context;

public class CryptoContext {
	
	private String password;
	
	private String iv;
	
	private String salt;
	
	private String secretKeyFactoryAlgorithm; // PBKDF2WithHmacSHA256
	
	private String secretKeyGeneratorAlgorithm; //AES
	
	private String inputFilepathname;
	
	private String outputFilepathname;
	
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

	public String getInputFilepathname() {
		return inputFilepathname;
	}

	public void setInputFilepathname(String inputFilepathname) {
		this.inputFilepathname = inputFilepathname;
	}

	public String getOutputFilepathname() {
		return outputFilepathname;
	}

	public void setOutputFilepathname(String outputFilepathname) {
		this.outputFilepathname = outputFilepathname;
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
