package com.example.newsmanagerproject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("articles")
    Call<List<Article>> getAllArticles();
}