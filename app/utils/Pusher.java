package util;

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
  
  private static String PusherKey() {
    return "";
  }
  
  private static String PusherSecret() {
    return "";
  }
  
  private static String PusherAppId() {
    return "";
  }
  
  public static void send(String data, String event) {
    // Compose the pusher request...

    // Pusher path...
    String path = "/apps/" + PusherAppId() + "/channels/football_tournament/events";

    // Query part of the URL, with authentication keys etc...
		String query = "auth_key=" + PusherKey() +
			"&auth_timestamp=" + (System.currentTimeMillis() / 1000) +
			"&auth_version=1.0" +
			"&body_md5=" + md5(data) +
			"&name=" + event;

		String key = "POST\n" + path + "\n" + query;
		String signature = sha256(PusherSecret(), key);

		String uri = "http://api.pusherapp.com" + path + "?" + query + "&auth_signature=" + signature;
   
    try {
      sendPost(uri,data);
    }
    catch (Exception e) {
      Logger.error("A problem occured trying to send the data to pusher.");
    }
  }
  
  private static String md5(String data) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] thedigest = md.digest(data.getBytes("UTF-8"));
      return new String(thedigest);
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
  
  private static void sendPost(String uri, String data) throws Exception {
 
		String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
 
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