package com.example.newsmanagerproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "NewsManagerPrefs";
    private static final String PREF_USER_ID = "userId";
    private static final String PREF_AUTH_TYPE = "authType";
    private static final String PREF_API_KEY = "apiKey";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_REMEMBER_ME = "rememberMe";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String userId, String authType, String apiKey, String username) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_USER_ID, userId);
        editor.putString(PREF_AUTH_TYPE, authType);
        editor.putString(PREF_API_KEY, apiKey);
        editor.putString(PREF_USERNAME, username);
        editor.putBoolean(PREF_REMEMBER_ME, true);
        editor.apply();
    }

    public boolean hasStoredSession() {
        return prefs.getBoolean(PREF_REMEMBER_ME, false);
    }

    public String getUserId() {
        return prefs.getString(PREF_USER_ID, null);
    }

    public String getAuthType() {
        return prefs.getString(PREF_AUTH_TYPE, null);
    }

    public String getApiKey() {
        return prefs.getString(PREF_API_KEY, null);
    }

    public String getUsername() {
        return prefs.getString(PREF_USERNAME, null);
    }

    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREF_USER_ID);
        editor.remove(PREF_AUTH_TYPE);
        editor.remove(PREF_API_KEY);
        editor.remove(PREF_USERNAME);
        editor.putBoolean(PREF_REMEMBER_ME, false);
        editor.apply();
    }
}
