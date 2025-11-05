package com.example.newsmanagerproject;

import com.google.gson.annotations.SerializedName;

public class Article {
    @SerializedName("title")
    private String article_title;

    @SerializedName("abstract")
    private String article_abstract;

    @SerializedName("thumbnail")
    private String article_thumbnail;

    @SerializedName("category")
    private String article_category;

    public Article(String article_title, String article_abstract, String article_thumbnail, String article_category) {
        this.article_title = article_title;
        this.article_abstract = article_abstract;
        this.article_thumbnail = article_thumbnail;
        this.article_category = article_category;
    }

    public String getArticle_title() {
        return article_title;
    }

    public String getArticle_abstract() {
        return article_abstract;
    }

    public String getArticle_thumbnail() {
        return article_thumbnail;
        // utils. bse64 to bitmap
    }

    public String getArticle_category() {
        return article_category;
    }

    public void setArticle_title(String article_title) {
        this.article_title = article_title;
    }

    public void setArticle_abstract(String article_abstract) {
        this.article_abstract = article_abstract;
    }

    public void setArticle_thumbnail(String article_thumbnail) {
        this.article_thumbnail = article_thumbnail;
    }

    public void setArticle_category(String article_category) {
        this.article_category = article_category;
    }
}
