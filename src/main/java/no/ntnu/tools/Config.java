package no.ntnu.tools;



import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Config{

  public final static int timeout = 50;

  public final static  SecretKey key64 = new SecretKeySpec( new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 }, "Blowfish" );

  public final static Cipher cipher;

  static {
    try {
      cipher = Cipher.getInstance( "Blowfish" );
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  public static BigInteger[] keyGen(){
//    int p = 59;
//    int q = 47;
//    int product = p*q;
//    int totient = (p-1)*(q-1);
//    int pubKey = 61;
//    int privKey = 1837;
    int p = 7;
    int q = 19;
    BigInteger product = BigInteger.valueOf(p*q);
    int totient = (p-1)*(q-1);
    BigInteger pubKey = BigInteger.valueOf(29);
    BigInteger privKey = BigInteger.valueOf(41);
    return new BigInteger[]{privKey,pubKey,product};
  }

  public  static String[] encrypt(String[] payload){
    BigInteger[] keys = keyGen();
    int index = 0;
    String[] encryptedPayload = new String[payload.length];
    for(String partition : payload){
      String encryptedPartition = "";
      for(int i = 0; i < partition.length(); i++) {
       BigInteger charValue = BigInteger.valueOf(partition.charAt(i));
       charValue = charValue.modPow((keys[1]),keys[2]);
       char finalChar = (char) (charValue.intValue());
        encryptedPartition = encryptedPartition.concat(finalChar + "");
      }
      encryptedPayload[index] = encryptedPartition;
      index++;
    }
    return encryptedPayload;
  }

  public static String[] decrypt(String[] payload){
    BigInteger[] keys = keyGen();
    int index = 0;
    String[] decryptedPayload = new String[payload.length];
    for(String partition : payload){
      String decryptedPartition = "";
      for(int i= 0; i<partition.length(); i++) {
        BigInteger charValue = BigInteger.valueOf(partition.charAt(i));
        charValue = charValue.modPow((keys[0]),keys[2]);
        char finalChar = (char) (charValue.intValue());
        decryptedPartition = decryptedPartition.concat(finalChar + "");
      }
      decryptedPayload[index] = decryptedPartition;
      index++;
    }
    return decryptedPayload;
  }
}
