package tops.encryption;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import tops.encryption.*;
import tops.main.*;
import tops.struct.*;

public class RSAcrypto {
	
	public RSAcrypto() throws GeneralSecurityException {
		  
	}
//	public static void saveToFile(String fileName,
//			  BigInteger mod, BigInteger exp) throws IOException {
//			  ObjectOutputStream oout = new ObjectOutputStream(
//			    new BufferedOutputStream(new FileOutputStream(fileName)));
//			  try {
//			    oout.writeObject(mod);
//			    oout.writeObject(exp);
//			  } catch (Exception e) {
//			    throw new IOException("Unexpected error", e);
//			  } finally {
//			    oout.close();
//			  }
//	}

	
	public static String encrypt(String freindID, String plainStr)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, InvalidKeySpecException,
			NoSuchProviderException, IOException {

		BigInteger mod = null, exp = null;
	
			FreindNode fnode = FreindList.freindList.get(freindID);
			mod = (BigInteger) fnode.pubMod;
			exp = (BigInteger) fnode.pubExp;
		
		RSAPublicKeySpec pub = new RSAPublicKeySpec(mod, exp);
		KeyFactory keyFact = KeyFactory.getInstance("RSA");
		PublicKey pubk = keyFact.generatePublic(pub);

		Cipher clsCipher = Cipher.getInstance("RSA");
		clsCipher.init(Cipher.ENCRYPT_MODE, pubk);
		byte[] data = plainStr.getBytes();
		byte[] arrCipherData = clsCipher.doFinal(data);
		String strCipher = new String(Base64Coder.encode(arrCipherData));

		return strCipher;

	}
	
	public static String decrypt(String encryptedStr)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, InvalidKeySpecException,
			NoSuchProviderException, IOException {

		BigInteger mod = null, exp = null;
		
			mod = (BigInteger) TOPS_Daemon.privMod;
			exp = (BigInteger) TOPS_Daemon.privExp;
		
		RSAPrivateKeySpec pub = new RSAPrivateKeySpec(mod, exp);
		KeyFactory keyFact = KeyFactory.getInstance("RSA");
		PrivateKey prk = keyFact.generatePrivate(pub);

		Cipher clsCipher = Cipher.getInstance("RSA");
		clsCipher.init(Cipher.DECRYPT_MODE, prk);

		byte[] encData = Base64Coder.decode(encryptedStr);
		byte[] arrData = clsCipher.doFinal(encData);
		String strResult = new String(arrData);

		// System.out.println("result(" + strResult + ")");

		return strResult;

	}

}
