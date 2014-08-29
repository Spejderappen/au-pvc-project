package vestsoft.com.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by michael on 29/08/14.
 */
public class ServerCommunication {
    private static String serverUrl = "http://au-pvc-project.herokuapp.com/";
    public static boolean login(String phoneNumber, String password) {
        boolean return_result = false;
        JSONArray result = connect("phone_number=" + phoneNumber + "&password=" + password);
        try {
            if(result.getJSONObject(0).getString("status").equals("true")) {
                return_result = true;
            }
        }catch (JSONException e) {
        }
        return return_result;
    }
    private static JSONArray connect(String parameters) {
        String result = null;
        URI myURI = null;

        try {
            myURI = new URI(serverUrl + "?" + parameters);
        } catch (URISyntaxException e) {
            // Deal with it
        }
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet getMethod = new HttpGet(myURI);
        HttpResponse webServerResponse = null;
        try {
            webServerResponse = httpClient.execute(getMethod);
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        HttpEntity httpEntity = webServerResponse.getEntity();

        if (httpEntity != null) {
            InputStream instream;
            try {
                instream = httpEntity.getContent();
                result = convertStreamToString(instream);
                instream.close();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            JSONArray myJSONarray = new JSONArray(result);
            return myJSONarray;
        } catch (JSONException e) {
        }
        return new JSONArray();
    }
    public static String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line = null;

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }
}