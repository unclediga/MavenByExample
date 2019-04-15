import org.apache.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.Base64.Encoder;


public class YahooRetriever {

	private static Logger log = Logger.getLogger(YahooRetriever.class);


	public InputStream retrieve(String zipcode) throws Exception {
		log.info( "Retrieving Weather Data" );


        ////////////////////////////////////////////////////
        String configFileName = System.getProperty("user.home")+"\\.config\\mavenbyexample\\yahooweather.properties";
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(configFileName));
        } catch (FileNotFoundException e) {
            throw new Exception("Can't find " + configFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String appId = properties.getProperty("appId");
        final String consumerKey = properties.getProperty("consumerKey");
        final String consumerSecret = properties.getProperty("consumerSecret");
        final String proxyHost = properties.getProperty("proxyHost");
        final int proxyPort = Integer.parseInt(properties.getProperty("proxyPort"));

        final String url = "https://weather-ydn-yql.media.yahoo.com/forecastrss";
        //String url = "http://weather.yahooapis.com/forecastrss?p=" + zipcode;

        long timestamp = new Date().getTime() / 1000;
        byte[] nonce = new byte[32];
        Random rand = new Random();
        rand.nextBytes(nonce);
        String oauthNonce = new String(nonce).replaceAll("\\W", "");

        List<String> parameters = new ArrayList<String>();
        parameters.add("oauth_consumer_key=" + consumerKey);
        parameters.add("oauth_nonce=" + oauthNonce);
        parameters.add("oauth_signature_method=HMAC-SHA1");
        parameters.add("oauth_timestamp=" + timestamp);
        parameters.add("oauth_version=1.0");
        // Make sure value is encoded
        parameters.add("location=" + URLEncoder.encode("sunnyvale,ca", "UTF-8"));
        //parameters.add("format=json");
        Collections.sort(parameters);

        StringBuffer parametersList = new StringBuffer();
        for (int i = 0; i < parameters.size(); i++) {
            parametersList.append(((i > 0) ? "&" : "") + parameters.get(i));
        }

        String signatureString = "GET&" +
            URLEncoder.encode(url, "UTF-8") + "&" +
            URLEncoder.encode(parametersList.toString(), "UTF-8");

        String signature = null;
        try {
            SecretKeySpec signingKey = new SecretKeySpec((consumerSecret + "&").getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHMAC = mac.doFinal(signatureString.getBytes());
            Encoder encoder = Base64.getEncoder();
            signature = encoder.encodeToString(rawHMAC);
        } catch (Exception e) {
            System.err.println("Unable to append signature");
            System.exit(0);
        }

        String authorizationLine = "OAuth " +
            "oauth_consumer_key=\"" + consumerKey + "\", " +
            "oauth_nonce=\"" + oauthNonce + "\", " +
            "oauth_timestamp=\"" + timestamp + "\", " +
            "oauth_signature_method=\"HMAC-SHA1\", " +
            "oauth_signature=\"" + signature + "\", " +
            "oauth_version=\"1.0\"";


//        URLConnection conn = new URL(url + "?location=sunnyvale,ca&format=json").openConnection();    


        SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy httpProxy = new Proxy(Proxy.Type.HTTP, addr);
        URLConnection conn = new URL(url + "?location=sunnyvale,ca").openConnection(httpProxy);

        //URLConnection conn = new URL(url + "?location=sunnyvale,ca").openConnection();
        conn.addRequestProperty("Authorization", authorizationLine);
        conn.addRequestProperty("X-Yahoo-App-Id", appId);
        conn.addRequestProperty("Content-Type", "application/json");
            
		return conn.getInputStream();
	}

}
