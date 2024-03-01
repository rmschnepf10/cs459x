package assignment3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;

public class EncFile {
	String publicKeyFilePath;
	String privateKeyFilePath;
	String txtfilePath;
	String newfilePath;
	
	//Initialized within readTheFiles
	PublicKey publicKeyFromFile;
	PrivateKey privateKeyFromFile;
	byte[] plainTextStringFromFile;
	
	//Initialized within AESKey and IV Creation
	SecretKey AESKey;
	byte[] IV;
	
	public EncFile(String publicKeyFilePath, String privateKeyFilePath, String txtfilePath, String newfilePath) {
		this.publicKeyFilePath = publicKeyFilePath;
		this.privateKeyFilePath = privateKeyFilePath;
		this.txtfilePath = txtfilePath;
		this.newfilePath = newfilePath;
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

	//This initializes the  AESKey and IV which are declared above as variables 
	public void generateAESKeyandIV() throws NoSuchAlgorithmException {
		KeyGenerator kGen = KeyGenerator.getInstance("AES");
		SecureRandom random = new SecureRandom();
		kGen.init(random);
		AESKey = kGen.generateKey();
		IV = AESKey.getEncoded();
	}
	
	public byte[] encryptIntoCiphertext() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		//This creates IVParamSpec with existing IV
		IvParameterSpec IVParamSpec = new IvParameterSpec(IV);
		
		//This creates SecretKeySpec with existing AES
		SecretKeySpec AESKeySpec = new SecretKeySpec(AESKey.getEncoded(), "AES");
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, AESKeySpec, IVParamSpec);
		
		byte[] cipherOfPlainText = cipher.doFinal(plainTextStringFromFile);
		
		return cipherOfPlainText;
		
	}
	
	public byte[] encryptAESwithRSApubKey() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKeyFromFile);
		byte[] encryptedAESKey = cipher.doFinal(AESKey.getEncoded());
		
		//This is just bring used to print out the size of all the values requested
		System.out.println("Have encrypted AES key to " + Integer.toString(encryptedAESKey.length ) + " bytes");
		System.out.println("Have picked a random IV with " + Integer.toString(IV.length ) + " bytes");

		return encryptedAESKey;

	}
	
	public byte[] signatureForPlaintext() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		Signature sig = Signature.getInstance("SHA512withRSA");
		sig.initSign(privateKeyFromFile);
		sig.update(plainTextStringFromFile);
		byte[] signature = sig.sign();
		
		return signature;
		
	}
	
	public void writeToNewFile() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SignatureException, IOException, InvalidKeySpecException {
		//Read Public Key & Restore
	    publicKeyFromFile = readFileAndStorePublicKey();
	    
	    //Read Private Key & Restore
	    privateKeyFromFile = readFileAndStorePrivateKey();
	    
	    plainTextStringFromFile = Files.readAllBytes(Paths.get(txtfilePath));
	    
	    //printing to the command line
	    System.out.println("Plaintext: ");
	    System.out.println(new String(plainTextStringFromFile));
	    
		//this creates the two values and stores the info as local variables
		generateAESKeyandIV();
		
		byte[] encryptedAES = encryptAESwithRSApubKey();
		byte[] cipherText = encryptIntoCiphertext();
		byte[] signature = signatureForPlaintext();		
		
		FileOutputStream fos = new FileOutputStream(newfilePath);
		fos.write(encryptedAES);
		fos.write(IV);
		fos.write(cipherText);
		fos.write(signature);
		fos.close();
		
		byte[] newfile_cyphertext = Files.readAllBytes(Paths.get(newfilePath));

		
		//This is just bring used to print out the size of all the values requested
		System.out.println("Have encrypted  " + Integer.toString(plainTextStringFromFile.length ) + " bytes of plaintext to " + Integer.toString(cipherText.length ) + " bytes of ciphertext.");
		System.out.println("Have computed signature with " + Integer.toString(signature.length ) + " bytes");
		System.out.println("Have written " + Integer.toString(newfile_cyphertext.length) + " bytes to file ciphertext.data");
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SignatureException {
		if (args.length != 4) {
			System.out.println(
					"Usage: java EncFile <receiver's public key> <sender's private key> <plaintext file> <new file>");
			return;
		}

		String publicKeyFilePath = args[0];
		String privateKeyFilePath = args[1];
		String txtfilePath = args[2];
		String newfilePath = args[3];

		EncFile encFile = new EncFile(publicKeyFilePath, privateKeyFilePath, txtfilePath, newfilePath);
		
		//This will execute all the code that will encrypted the plainText and write out the information into the new empty file
		encFile.writeToNewFile();
		
		
		
		

	}
}
