package assignment3;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RSAKeyGen {
	String path1;
	String path2;
	
	//constructor to get the file paths
	public RSAKeyGen(String path1, String path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	
	//this will generate random RSAKeyPair
	public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
		kpGen.initialize(2048);
		
		KeyPair RSAkp = kpGen.generateKeyPair();
		
		return RSAkp;
	}
	
	//This stores the public key to a certain file given the file path
	public void storePublicKeyToFile(PublicKey publicKey, String filePath) throws IOException {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(filePath);
		fos.write(x509EncodedKeySpec.getEncoded());
		fos.close();
	}
	
	//same as above but private key
	public void storePrivateKeyToFile(PrivateKey privateKey, String filePath) throws IOException {
		PKCS8EncodedKeySpec pkcs38EncodeKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(filePath);
		fos.write(pkcs38EncodeKeySpec.getEncoded());
		fos.close();
	}
	
	//This will actually execute the code and utalize the helper methods above
	public void storeKeysIntoFilePaths() throws NoSuchAlgorithmException, IOException {
		//generating the keyPair which is going to associate with file path 1 or 2
		KeyPair kp = generateRSAKeyPair();
		
		//Identifying the public and private keys for the KeyPair
		PublicKey publicKey = kp.getPublic();
		PrivateKey privateKey = kp.getPrivate();				
		
		//Store Public and Private keys to the file
		storePublicKeyToFile(publicKey, path1);
		storePrivateKeyToFile(privateKey, path2);
		
		
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		if (args.length != 2) {
			System.out.println("Usage: java RSAKeyGen <filePath1> <filePath2>");
			return;
		}
		
		String path1 = args[0];
		String path2 = args[1];
		
        RSAKeyGen rsakeygen = new RSAKeyGen(path1, path2);
        
        rsakeygen.storeKeysIntoFilePaths();
		
	}
}
