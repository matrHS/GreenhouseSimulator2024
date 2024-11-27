package no.ntnu.tools;
import java.math.BigInteger;


public class RSA {

  public  static String[] encrypt(String[] payload, BigInteger[] keys){
    int index = 2;
    String[] encryptedPayload = new String[payload.length];
    encryptedPayload[0] = payload[0];
    encryptedPayload[1] = payload[1];
    for(int i = 2; i < payload.length; i ++ ){
      String partition = payload[i];
      String encryptedPartition = "";
      for(int j = 0; j < partition.length(); j++) {
       BigInteger charValue = BigInteger.valueOf(partition.charAt(j));
       charValue = charValue.modPow((keys[1]),keys[2]);
       char finalChar = (char) (charValue.intValue());
        encryptedPartition = encryptedPartition.concat(finalChar + "");
      }
      encryptedPayload[index] = encryptedPartition;
      index++;
    }
    return encryptedPayload;
  }

  public static String[] decrypt(String[] payload, BigInteger[] keys){
    int index = 2;
    String[] decryptedPayload = new String[payload.length];
    decryptedPayload[0] = payload[0];
    decryptedPayload[1] = payload[1];
    for(int i = 2; i < payload.length; i ++ ){
      String partition = payload[i];
      String decryptedPartition = "";
      for(int j= 0; j<partition.length(); j++) {
        BigInteger charValue = BigInteger.valueOf(partition.charAt(j));
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
