package tops.struct;
import java.math.BigInteger;
import java.net.*;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.google.common.hash.*;

import net.rudp.*;


public class FreindNode{
	public String freindID;
	public String privateIP;
	public int privatePN;
	public String publicIP;
	public int publicPN;
	public BigInteger pubMod;
	public BigInteger pubExp;
	public String sessionKey;
	public boolean keySendChk = false;
	public boolean keyRecieveChk = false;
	public ReliableSocket socket = null;
	public BloomFilter bloomFilter;
	
	
	public FreindNode(String id, String pvIP, int pvPN, String pbIP, int pbPN, BigInteger mod, BigInteger exp,  String sk) {
		// TODO Auto-generated constructor stub
		freindID = id;
		privateIP = pvIP;
		privatePN = pvPN;
		publicIP = pbIP;
		publicPN = pbPN;
		pubMod = mod;
		pubExp = exp;
		sessionKey = sk;
	}
	
}