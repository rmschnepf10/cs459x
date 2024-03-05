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
import java.security.SignatureException;
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
	byte[] cipherTextFromFileEncrypted;
	
	byte[] signatureDecrypted;
	
	public DecFile(String privateKeyFilePath, String publicKeyFilePath, String ciphertextFilePath) {
		this.publicKeyFilePath = publicKeyFilePath;
		this.privateKeyFilePath = privateKeyFilePath;
		this.ciphertextFilePath = ciphertextFilePath;
	}
	
	// HELPER METHOD, read the files: Parses the public key from a file into a
		// PublicKey -> Code is given in part 2
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
		// PrivateKey -> Code is given in part 2
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
	
	//grab the code given within class but just do not turn byte array into a key
	public void readTheCipherFile() throws IOException {
		File fileCipher = new File(ciphertextFilePath);
		FileInputStream fis = new FileInputStream(ciphertextFilePath);
		cipherTextFromFileEncrypted = new byte[(int)fileCipher.length()];
		fis.read(cipherTextFromFileEncrypted);
		fis.close();
	}
	
	//getting the decrypted text with the private key
	public SecretKey readAESKeyAndDecrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		//this is creating a new byte array that will store the encrypted text that is 256 byes
		byte[] encryptedText = new byte[256];
		System.arraycopy(cipherTextFromFileEncrypted, 0, encryptedText, 0, 256);
		//This is creating a cipher to decrypt the encryptedText that hosts the AES Key
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKeyFromFile);
		byte[] decryptedText = cipher.doFinal(encryptedText);
		
		
		//defining the AES Secret Key from the encrypted Text 
		byte[] AESKey = new byte[16];
		System.arraycopy(decryptedText, 0, AESKey, 0, 16);
		SecretKey key = new SecretKeySpec(AESKey, "AES");
		//printing out to the consel
		System.out.println("Decrypt encrypted AES key to get " + Integer.toString(AESKey.length ) + " bytes");
		return key;
	}
	
	//placing encrypted info into an IV byte array
	public byte[] readIV() {
		byte[] IV = new byte[16];
		System.arraycopy(cipherTextFromFileEncrypted, 256, IV, 0, 16);
		return IV;
	}
	
	public byte[] readCipherText(int sizeofCypherText) {
		//This is taking in the Cyphhertext and producing the size
		byte[] cypherText = new byte[sizeofCypherText];
		System.arraycopy(cipherTextFromFileEncrypted, 272, cypherText, 0, sizeofCypherText);
		
		return cypherText;
	}
	
	//This is taking in the Cyphhertext and producing the size
	public int getSizeOfCypherTextDecrypted() {
		int lengthOfFile = cipherTextFromFileEncrypted.length;
		//this is subracting the signature, AES, and IV
		int sizeofCypherTextEncrypted = lengthOfFile - 256 - 16 - 256;
		return sizeofCypherTextEncrypted;
	}
	
	//this reads the signature that is decrypted and places it in a bytes array 
	public byte[] readTheSignature() {
		byte[] signatureByte = new byte[256];
		System.arraycopy(cipherTextFromFileEncrypted, (cipherTextFromFileEncrypted.length - 256), signatureByte, 0, 256);
		return signatureByte;
	}
	
	public byte[] decryptCipherText(byte[] encryptedText, SecretKey AESKey, byte[] IV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		//This creates IVParamSpec with existing IV
		IvParameterSpec IVParamSpec = new IvParameterSpec(IV);
		
		//This creates SecretKeySpec with existing AES
		SecretKeySpec AESKeySpec = new SecretKeySpec(AESKey.getEncoded(), "AES");
		
		//decrypting the plaintext
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, AESKeySpec, IVParamSpec);
		
		byte[] decryptedText = cipher.doFinal(encryptedText);		
		
		return decryptedText;
	}
	

	
	public boolean verifyIfSignMatchesPlaintext(byte[] signatureByte, byte[] decryptedText) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		
		//This is creating a signature from the instance given in EncFile
		Signature signature = Signature.getInstance("SHA512withRSA");
		//now we are initializing the signature with the public key
		signature.initVerify(publicKeyFromFile);
		//this is updating the signature with the plaintext file that has been decrypted
		signature.update(decryptedText);
        boolean output = signature.verify(signatureByte);

		return output;
	}
	
	public void decryptTheCiphertext() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SignatureException {
	    //Read Private Key & Restore
	    privateKeyFromFile = readFileAndStorePrivateKey();
		
		//Read Public Key & Restore
	    publicKeyFromFile = readFileAndStorePublicKey();
	    
	    //get the cipher text and store it into bytes 
	    readTheCipherFile();
	    
		//Reads and Decrypts the AES Key
		SecretKey AESKey = readAESKeyAndDecrypt();
		
		//Reads the IV
		byte[] IV = readIV();
		
		//Gets the size of the plaintext and stores it as an int
		int sizeOfCypherTextDecrypted = getSizeOfCypherTextDecrypted();
		
		//this reads the cypher text and places it in cipherTextEncrypted byte array 
		byte[] cipherTextEncrypted = readCipherText(sizeOfCypherTextDecrypted);

		System.out.println("Size of the ciphertext: " + cipherTextEncrypted.length + " bytes");

		//reads the signature and places it in a byte array 
		byte[] signature = readTheSignature();
		
		//This will decrypt the plane text and then store the decrypted text in a byte array
		byte[] cipherTextDecrypted = decryptCipherText(cipherTextEncrypted, AESKey, IV);
		System.out.println("Decrypt ciphertext to get: " + Integer.toString(cipherTextDecrypted.length ) + " bytes");
		System.out.println("Decrypt to obtain plaintext:");
		System.out.println(new String(cipherTextDecrypted));
		
		//This will check if the result to let you know if you encrypted/decrypted the code correctly
		boolean signatureBool = verifyIfSignMatchesPlaintext(signature, cipherTextDecrypted);
		System.out.println("Result of verifying signature: " + Boolean.toString(signatureBool));
	}
	
	//all comments for this part are the same as RSAKeyGen other than we have 3 inputs instead of 2
	public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SignatureException {
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
