package utils;

import play.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.apache.commons.codec.binary.Hex;

public class Pusher {
  
  public static String PusherKey() {
  	return System.getenv().get("PUSHER_KEY");
  }
  
  private static String PusherSecret() {
  	return System.getenv().get("PUSHER_SECRET");
  }
  
  private static String PusherAppId() {
  	return System.getenv().get("PUSHER_APP_ID");
  }
  
  public static void send(String data, String event) {
    // Compose the pusher request...

    // Pusher path...
    String path = "/apps/" + PusherAppId() + "/channels/football_tournament/events";

    Logger.info(path);

    // Query part of the URL, with authentication keys etc...
		String query = "auth_key=" + PusherKey() +
			"&auth_timestamp=" + (System.currentTimeMillis() / 1000) +
			"&auth_version=1.0" +
			"&body_md5=" + md5(data) +
			"&name=" + event;

    Logger.info(query);

		String key = "POST\n" + path + "\n" + query;

    Logger.info(key);

		String signature = sha256(PusherSecret(), key);

    Logger.info(signature);

		String uri = "http://api.pusherapp.com" + path + "?" + query + "&auth_signature=" + signature;
   
    Logger.info(uri);
   
    try {
      sendPost(uri,data);
    }
    catch (Exception e) {
      Logger.error("A problem occured trying to send the data to pusher.",e);
    }
  }
  
  private static String md5(String data) {
    try {
      Logger.info(data);
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] data_bytes = data.getBytes("UTF-8");
      byte[] hash = md.digest(data_bytes);
      StringBuffer hexString = new StringBuffer();

      for (int i = 0; i < hash.length; i++) {
        if ((0xff & hash[i]) < 0x10) {
          hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
        } else {
          hexString.append(Integer.toHexString(0xFF & hash[i]));
        }
      }

      Logger.info("Digest = "+hexString);
      return new String(hexString);
    }
    catch (Exception e)
    {
      return "";
    }
  }
  
  private static String sha256(String key, String data) {
    try {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
      sha256_HMAC.init(secret_key);
      return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes()));
    }
    catch (Exception e)
    {
      return "";
    }
  }
  
  private static void sendPost(String url, String data) throws Exception {
 
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(data);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}
  
}