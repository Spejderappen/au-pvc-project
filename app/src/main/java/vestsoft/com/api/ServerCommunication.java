package vestsoft.com.api;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import vestsoft.com.pvc_project.Model.Friend;

/**
 * Created by michael on 29/08/14.
 */
public class ServerCommunication {
    private static String serverUrl = "http://au-pvc-project.herokuapp.com/";

    public static boolean login(String phoneNumber, String password) {
        boolean return_result = false;
        JSONArray result = connectGet("login?phone_number=" + phoneNumber + "&password=" + password);
        try {
            if(result.getJSONObject(0).getString("status").equals("true")) {
                return_result = true;
            }
        }catch (JSONException e) {
        }
        return return_result;
    }

    public static boolean createUser(String first_name, String last_name, String phone_number, String password) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("first_name", first_name));
        params.add(new BasicNameValuePair("last_name", last_name));
        params.add(new BasicNameValuePair("phone_number", phone_number));
        params.add(new BasicNameValuePair("password", password));

        JSONArray result = connectPost(params, "users");
        try {
            if(result.getJSONObject(0).getString("status").equals("true")) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean updatePosition(Friend friend) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("position", "true"));
        params.add(new BasicNameValuePair("phone_number", friend.getPhone()));
        params.add(new BasicNameValuePair("latitude", Double.toString(friend.getLatitide())));
        params.add(new BasicNameValuePair("longitude", Double.toString(friend.getLongitude())));

        JSONArray result = connectPut(params, "users/1");
        try {
            if(result.getJSONObject(0).getString("status").equals("true")) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
        }
        return false;
    }

    public static List<Friend> getExcistingFriends(List<Friend> friends) {
        String friend_numbers = "";
        for(Friend f : friends) {
            friend_numbers += f.getPhone() + ",";
        }
        List<Friend> newFriendList = new ArrayList<Friend>();
        JSONArray result = connectGet("users.json?friend_numbers=" + friend_numbers);
        for(int i = 0 ; result.length() > i ; i++ ) {
            try {
                JSONObject obj = result.getJSONObject(i);
                Friend friend = new Friend();
                friend.setName(obj.getString("first_name") + " " + obj.getString("last_name"));
                friend.setPhone(obj.getString("phone_number"));
                friend.setDateTime(obj.getString("updated_at"));
                friend.setLatitude(obj.getDouble("latitude"));
                friend.setLongitude(obj.getDouble("longitude"));
                newFriendList.add(friend);
            } catch (JSONException e) {
            }
        }
        return newFriendList;
    }

    private static JSONArray connectGet(String parameters) {
        String result = null;
        URI myURI = null;

        try {
            myURI = new URI(serverUrl + parameters);
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
            Log.e("PVC", e.getMessage());
        }
        return new JSONArray();
    }

    private static JSONArray connectPost(List<NameValuePair> parameters, String url) {
        String result = null;

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(serverUrl + url);
        // Request parameters and other properties.

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //Execute and get the response.
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity httpEntity = response.getEntity();

        if (httpEntity != null) {
            InputStream instream;
            try {
                instream = httpEntity.getContent();
                result = convertStreamToString(instream);
                instream.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            JSONArray myJSONarray = new JSONArray(result);
            return myJSONarray;
        } catch (JSONException e) {
            Log.e("PVC", e.getMessage());
        }
        return new JSONArray();
    }

    private static JSONArray connectPut(List<NameValuePair> parameters, String url) {
        String result = null;

        HttpClient httpClient = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(serverUrl + url);
        // Request parameters and other properties.

        try {
            httpPut.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //Execute and get the response.
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity httpEntity = response.getEntity();

        if (httpEntity != null) {
            InputStream instream;
            try {
                instream = httpEntity.getContent();
                result = convertStreamToString(instream);
                instream.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            JSONArray myJSONarray = new JSONArray(result);
            return myJSONarray;
        } catch (JSONException e) {
            Log.e("PVC", e.getMessage());
        }
        return new JSONArray();
    }

    private static String convertStreamToString(InputStream is) throws IOException {
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