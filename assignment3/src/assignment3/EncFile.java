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
	
	//constructor to get the file paths/files 
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
		
		//this info is given 
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
		
		//this info is given 
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
		//this creates a key of type AES
		KeyGenerator kGen = KeyGenerator.getInstance("AES");
		//This is basically a random number generator but makes the values more secure 
		SecureRandom random = new SecureRandom();
		kGen.init(random);
		//this is assigning the AESKey byte array values from the initialized key generator
		AESKey = kGen.generateKey();
		//This is creating a Initialization vector from the AESKey
		IV = AESKey.getEncoded();
	}
	
	public byte[] encryptIntoCiphertext() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		//This creates IVParamSpec with existing IV
		IvParameterSpec IVParamSpec = new IvParameterSpec(IV);
		
		//This creates SecretKeySpec with existing AES
		SecretKeySpec AESKeySpec = new SecretKeySpec(AESKey.getEncoded(), "AES");
		
		//create a cipher with the instance specifications given
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		//making it encrypt and utilizing the spec IV and AES
		cipher.init(Cipher.ENCRYPT_MODE, AESKeySpec, IVParamSpec);
		
		//this is encrypting the plain text file and then storing it into an array of bytes
		byte[] cipherOfPlainText = cipher.doFinal(plainTextStringFromFile);
		
		return cipherOfPlainText;
		
	}
	
	public byte[] encryptAESwithRSApubKey() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		//creating a cipher with the instance of RSA
		Cipher cipher = Cipher.getInstance("RSA");
		//making it encrypt mode and setting it to encrypt utilizing the receivers public key 
		cipher.init(Cipher.ENCRYPT_MODE, publicKeyFromFile);
		//this is actually encrypting the AES key created earlier
		byte[] encryptedAESKey = cipher.doFinal(AESKey.getEncoded());
		
		//This is just bring used to print out the size of all the values requested
		System.out.println("Have encrypted AES key to " + Integer.toString(encryptedAESKey.length ) + " bytes");
		System.out.println("Have picked a random IV with " + Integer.toString(IV.length ) + " bytes");

		return encryptedAESKey;

	}
	
	public byte[] signatureForPlaintext() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		//this is creating a Signature with the instance SHA512withRSA
		Signature signature = Signature.getInstance("SHA512withRSA");
		//creates signature from privateKeyFromFile
		signature.initSign(privateKeyFromFile);
		//this updates the text file
		signature.update(plainTextStringFromFile);
		//this grabs the signature byte of size 256
		byte[] byteSignature = signature.sign();
		
		return byteSignature;
		
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
		
		//this call all of the methods explained earlier and assigns them to byte values 
		byte[] encryptedAES = encryptAESwithRSApubKey();
		byte[] cipherText = encryptIntoCiphertext();
		byte[] signature = signatureForPlaintext();		
		
		//this is opening/creating the cypher.text file and writing the following items 
			//encrypted AES
			//Initialization vector
			//the plain text that has been encrypted which is called cypher text 
			//the signature 
		FileOutputStream fos = new FileOutputStream(newfilePath);
		fos.write(encryptedAES);
		fos.write(IV);
		fos.write(cipherText);
		fos.write(signature);
		fos.close();
		
		//This creates a new byte that combines all of the values written to 
		byte[] newfile_cyphertext = Files.readAllBytes(Paths.get(newfilePath));

		
		//This is just bring used to print out the size of all the values requested
		System.out.println("Have encrypted  " + Integer.toString(plainTextStringFromFile.length ) + " bytes of plaintext to " + Integer.toString(cipherText.length ) + " bytes of ciphertext.");
		System.out.println("Have computed signature with " + Integer.toString(signature.length ) + " bytes");
		System.out.println("Have written " + Integer.toString(newfile_cyphertext.length) + " bytes to file ciphertext.data");
	}

	//all comments for this part are the same as RSAKeyGen other than we have 4 inputs instead of 2
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
