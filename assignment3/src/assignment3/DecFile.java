package assignment3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DecFile {
	String privateKeyFilePath;
	String publicKeyFilePath;
	String ciphertextFilePath;
	
	PublicKey publicKeyFromFile;
	PrivateKey privateKeyFromFile;
	byte[] cipherTextFromFile;
	
	public DecFile(String privateKeyFilePath, String publicKeyFilePath, String ciphertextFilePath) {
		this.publicKeyFilePath = publicKeyFilePath;
		this.privateKeyFilePath = privateKeyFilePath;
		this.ciphertextFilePath = ciphertextFilePath;
	}
	
	// HELPER METHOD, read the files: Parses the public key from a file into a
		// PublicKey
	public PublicKey readFileAndStorePublicKey()
			throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		File filePublicKey = new File(publicKeyFilePath);
		FileInputStream fis = new FileInputStream(publicKeyFilePath);
		byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
		fis.read(encodedPublicKey);
		fis.close();
		
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedPublicKey);
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
		return publicKey;
	}

		// HELPER METHOD, read the files: Parses the private key from a file into a
		// PrivateKey
	public PrivateKey readFileAndStorePrivateKey()
		throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		File filePrivateKey = new File(privateKeyFilePath);
		FileInputStream fis = new FileInputStream(privateKeyFilePath);
		byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
		fis.read(encodedPrivateKey);
		fis.close();
		
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(pkcs8EncodedKeySpec);
		return privateKey;
	}
	
	public byte[] readTheCipherFile() throws IOException {
		File fileCipher = new File(ciphertextFilePath);
		FileInputStream fis = new FileInputStream(ciphertextFilePath);
		cipherTextFromFile = new byte[(int)fileCipher.length()];
		fis.read(cipherTextFromFile);
		fis.close();
		return cipherTextFromFile;
	}
	
	public SecretKey readAESKeyAndDecrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		//getting the decrypted text with the private key
		byte[] encryptedText = new byte[256];
		System.arraycopy(cipherTextFromFile, 0, encryptedText, 0, 256);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKeyFromFile);
		byte[] decryptedText = cipher.doFinal(encryptedText);
		
		
		//defining the AES Key
		byte[] AESKey = new byte[16];
		System.arraycopy(decryptedText, 0, AESKey, 0, 16);
		SecretKey key = new SecretKeySpec(AESKey, "AES");
		System.out.println("Decrypt encrypted AES key to get " + Integer.toString(AESKey.length ) + " bytes");
		return key;
	}
	
	public byte[] readIV() {
		byte[] IV = new byte[16];
		System.arraycopy(cipherTextFromFile, 256, IV, 0, 16);
		return IV;
	}
	
	public byte[] readCipherText(int sizeofCypherText) {
		byte[] cypherText = new byte[sizeofCypherText];
		System.arraycopy(cipherTextFromFile, 272, cypherText, 0, sizeofCypherText);
		
		return cypherText;
	}
	
	public int getSizeOfCypherText() {
		int lengthOfFile = cipherTextFromFile.length;
		int sizeofCypherText = lengthOfFile - 256 - 16 - 256;
		return sizeofCypherText;
	}
	
	public byte[] readTheSignature(int sizeofCypherText) {
		byte[] signature = new byte[256];
		System.arraycopy(cipherTextFromFile, (256 + 16 + sizeofCypherText), signature, 0, 256);
		
		return signature;
	}
	
	public byte[] decryptCipherText(byte[] encryptedText, SecretKey AESKey, byte[] IV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		//This creates IVParamSpec with existing IV
		IvParameterSpec IVParamSpec = new IvParameterSpec(IV);
		
		//This creates SecretKeySpec with existing AES
		SecretKeySpec AESKeySpec = new SecretKeySpec(AESKey.getEncoded(), "AES");
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, AESKeySpec, IVParamSpec);
		
		byte[] decryptedText = cipher.doFinal(encryptedText);
		
		return decryptedText;
	}
	
	public void decryptTheCiphertext() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
	    //Read Private Key & Restore
	    privateKeyFromFile = readFileAndStorePrivateKey();
		
		//Read Public Key & Restore
	    publicKeyFromFile = readFileAndStorePublicKey();
	    
	    //get the cipher text and store it into bytes 
	    cipherTextFromFile = readTheCipherFile();
		System.out.println("Size of the ciphertext: " + Integer.toString(cipherTextFromFile.length ) + " bytes");
		
		SecretKey AESKey = readAESKeyAndDecrypt();
		
		byte[] IV = readIV();
		
		int sizeOfCypherText = getSizeOfCypherText();
		
		byte[] cipherTextEncrypted = readCipherText(sizeOfCypherText);
		//DELETE LATER, JUST FOR TESTING
		System.out.println("DELETE LATER" + cipherTextEncrypted.length);
		
		byte[] signature = readTheSignature(sizeOfCypherText);
		
		byte[] cipherTextDecrypted = decryptCipherText(cipherTextEncrypted, AESKey, IV);
		System.out.println("Decrypt ciphertext to get: " + Integer.toString(cipherTextDecrypted.length ) + " bytes");
		System.out.println("Decrypt to obtain plaintext:");
		System.out.println(new String(cipherTextDecrypted));

	}
	
	public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		if (args.length != 3) {
			System.out.println(
					"Usage: java EncFile <receiver's private key> <sender's public key> <ciphertext file>");
			return;
		}

		String privateKeyFilePath = args[0];
		String publicKeyFilePath = args[1];
		String ciphertextPath = args[2];

		DecFile decFile = new DecFile(privateKeyFilePath, publicKeyFilePath, ciphertextPath);
		
		decFile.decryptTheCiphertext();
	}
}