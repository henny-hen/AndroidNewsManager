package com.example.newsmanagerproject.services;

import android.util.Log;

import com.example.newsmanagerproject.models.Article;
import com.example.newsmanagerproject.models.Image;
import com.example.newsmanagerproject.models.ModelEntity;


import org.json.simple.*;
import org.json.simple.parser.ParseException;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class ModelManager {
    private static final String TAG = "ModelManager";
    
    private String idUser;

    private String usernameS;
    private String authType;
    private String apikey;
    private String serviceUrl;
    private boolean requireSelfSigned = false;
    
    public static final String ATTR_LOGIN_USER = "username";
    public static final String ATTR_LOGIN_PASS = "password";
    public static final String ATTR_SERVICE_URL = "service_url";
    public static final String ATTR_REQUIRE_SELF_CERT = "require_self_signed_cert";
    
    private static ModelManager instance;
    
    public static ModelManager getInstance() {
        if (instance == null) {
            instance = new ModelManager();
        }
        return instance;
    }
    
    private ModelManager() {
        this.serviceUrl = "https://sanger.dia.fi.upm.es/pui-rest-news/";
        this.usernameS = getUsernameS();
        this.apikey = "ANON07";
        this.authType = "Anonymous";
        this.requireSelfSigned = true;
    }
    
    public void initialize(Properties properties) throws Exception {
        if (properties.containsKey(ATTR_SERVICE_URL)) {
            this.serviceUrl = properties.getProperty(ATTR_SERVICE_URL);
        }
        
        requireSelfSigned = properties.containsKey(ATTR_REQUIRE_SELF_CERT) 
            && properties.getProperty(ATTR_REQUIRE_SELF_CERT).equalsIgnoreCase("TRUE");
        
        if (properties.containsKey(ATTR_LOGIN_USER) && properties.containsKey(ATTR_LOGIN_PASS)) {
            login(properties.getProperty(ATTR_LOGIN_USER), 
                  properties.getProperty(ATTR_LOGIN_PASS));
        }
    }
    
    public void login(String username, String password) throws Exception {
        String request = serviceUrl + "login";
        
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        if (requireSelfSigned) {
            TrustModifier.relaxHostChecking(connection);
        }
        
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);
        
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("username", username);
        jsonParam.put("passwd", password);
        
        writeJSONParams(connection, jsonParam);
        
        int httpResult = connection.getResponseCode();
        if (httpResult == HttpURLConnection.HTTP_OK) {
            String res = parseHttpStreamResult(connection);
            JSONObject userJsonObject = readRestResultFromSingle(res);
            
            idUser = userJsonObject.get("user").toString();
            authType = userJsonObject.get("Authorization").toString();
            apikey = userJsonObject.get("apikey").toString();
            
            Log.d(TAG, "Login successful for user: " + username);
            usernameS = username;
        } else {
            throw new Exception("Login failed: " + connection.getResponseMessage());
        }
    }
    
    public void stayLoggedIn(String userId, String authorizationType, String apiKey, String username) {
        this.idUser = userId;
        this.authType = authorizationType;
        this.apikey = apiKey;
        this.usernameS = username;
        Log.d(TAG, "Session restored for user: " + username);
    }
    
    public void logout() {
        // Reset to anonymous mode
        this.usernameS = "";
        this.idUser = null;
        this.authType = "Anonymous";
        this.apikey = "ANON07";
        Log.d(TAG, "User logged out, reset to anonymous");
    }
    
    public List<Article> getArticles() throws Exception {
        return getArticles(-1, -1);
    }
    
    public List<Article> getArticles(int buffer, int offset) throws Exception {
        String limits = "";
        if (buffer > 0 && offset >= 0) {
            limits = "/" + buffer + "/" + offset;
        }
        
        List<Article> result = new ArrayList<>();
        String request = serviceUrl + "articles" + limits;
        
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        if (requireSelfSigned) {
            TrustModifier.relaxHostChecking(connection);
        }
        
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Authorization", getAuthTokenHeader());
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);
        
        int httpResult = connection.getResponseCode();
        if (httpResult == HttpURLConnection.HTTP_OK) {
            String res = parseHttpStreamResult(connection);
            List<JSONObject> objects = readRestResultFromList(res);
            
            for (JSONObject jsonObject : objects) {
                result.add(new Article(this, jsonObject));
            }
            
            Log.d(TAG, objects.size() + " articles retrieved");
        } else {
            throw new Exception("Failed to get articles: " + connection.getResponseMessage());
        }
        
        return result;
    }
    
    public Article getArticle(int idArticle) throws Exception {
        String request = serviceUrl + "article/" + idArticle;
        
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        if (requireSelfSigned) {
            TrustModifier.relaxHostChecking(connection);
        }
        
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Authorization", getAuthTokenHeader());
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);
        
        int httpResult = connection.getResponseCode();
        if (httpResult == HttpURLConnection.HTTP_OK) {
            String res = parseHttpStreamResult(connection);
            JSONObject object = readRestResultFromGetObject(res);
            return new Article(this, object);
        } else {
            throw new Exception("Failed to get article: " + connection.getResponseMessage());
        }
    }
    
    protected int saveImage(Image image) throws Exception {
        String request = serviceUrl + "article/image";
        
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        if (requireSelfSigned) {
            TrustModifier.relaxHostChecking(connection);
        }
        
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Authorization", getAuthTokenHeader());
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);
        
        writeJSONParams(connection, image.toJSON());
        
        int httpResult = connection.getResponseCode();
        if (httpResult == HttpURLConnection.HTTP_OK) {
            String res = parseHttpStreamResult(connection);
            int id = readRestResultFromInsert(res);
            Log.d(TAG, "Image saved with id: " + id);
            return id;
        } else {
            throw new Exception("Failed to save image: " + connection.getResponseMessage());
        }
    }
    
    // Helper methods
    private String parseHttpStreamResult(HttpURLConnection connection) throws IOException {
        StringBuilder res = new StringBuilder();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), "utf-8"));
        String line;
        while ((line = br.readLine()) != null) {
            res.append(line).append("\n");
        }
        br.close();
        return res.toString();
    }
    
    private void writeJSONParams(HttpURLConnection connection, JSONObject json) throws IOException {
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(json.toJSONString());
        wr.flush();
        wr.close();
    }
    
    private int readRestResultFromInsert(String res) throws Exception {
        Object o = JSONValue.parseWithException(res);
        if (o instanceof JSONObject) {
            JSONObject jsonResult = (JSONObject) o;
            if (jsonResult.containsKey("id")) {
                return Integer.parseInt(jsonResult.get("id").toString());
            } else {
                throw new Exception("No id in json returned");
            }
        } else {
            throw new Exception("No json returned");
        }
    }
    
    private JSONObject readRestResultFromGetObject(String res) throws Exception {
        Object o = JSONValue.parseWithException(res);
        if (o instanceof JSONObject) {
            return (JSONObject) o;
        } else {
            throw new Exception("No json object returned");
        }
    }
    
    private List<JSONObject> readRestResultFromList(String res) throws Exception {
        List<JSONObject> result = new ArrayList<>();
        Object o = JSONValue.parseWithException(res);
        
        if (o instanceof JSONObject) {
            JSONObject jsonResult = (JSONObject) o;
            Set<Object> keys = jsonResult.keySet();
            for (Object keyRow : keys) {
                JSONObject jsonObj = (JSONObject) jsonResult.get(keyRow);
                result.add(jsonObj);
            }
        } else if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (Object row : jsonArray) {
                JSONObject jsonObj = (JSONObject) row;
                result.add(jsonObj);
            }
        } else {
            throw new Exception("Result is not a JSON Array nor Object");
        }
        
        return result;
    }
    
    private JSONObject readRestResultFromSingle(String res) throws ParseException {
        return (JSONObject) JSONValue.parseWithException(res);
    }
    
    private String getAuthTokenHeader() {
        return authType + " apikey=" + apikey;
    }
    
    public String getIdUser() {
        return idUser;
    }

    public String getUsernameS() {
        return usernameS;
    }
    
    public String getApikey() {
        return apikey;
    }
    
    public String getAuthType() {
        return authType;
    }
    
    public boolean isLoggedIn() {
        return idUser != null && !idUser.isEmpty() && !authType.equals("Anonymous");
    }


    public int save(ModelEntity o) throws Exception {
        int returnedId = -1;
        if (o instanceof Image) {
            returnedId = saveImage((Image) o);
        }
        // Add article save if needed
        return returnedId;
    }

    public void delete(ModelEntity o) throws Exception {
        // Implement if needed
    }
}