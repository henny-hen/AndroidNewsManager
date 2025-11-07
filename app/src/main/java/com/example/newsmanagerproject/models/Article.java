package com.example.newsmanagerproject.models;

import com.example.newsmanagerproject.services.ModelManager;
import com.example.newsmanagerproject.utils.Utils;

import org.json.simple.JSONObject;

import java.util.Hashtable;

public class Article extends ModelEntity {
    private String titleText;
    private String subtitleText;
    private String category;
    private String abstractText;
    private String bodyText;
    private String idUser;
    private Image mainImage;
    private String imageDescription;
    private String thumbnail;
    private String updateDate;

    public Article(ModelManager mm, JSONObject jsonArticle) {
        super(mm);
        parseFromJSON(jsonArticle);
    }

    public Article(ModelManager mm, String category, String titleText, String abstractText, String body, String subtitle) {
        super(mm);
        this.id = -1;
        this.category = category;
        this.titleText = titleText;
        this.abstractText = abstractText;
        this.bodyText = body;
        this.subtitleText = subtitle;
    }

    private String parseStringFromJson(JSONObject jsonArticle, String key, String def) {
        Object in = jsonArticle.getOrDefault(key, def);
        return (in == null ? def : in).toString();
    }

    private void parseFromJSON(JSONObject jsonArticle) {
        try {
            id = Integer.parseInt(jsonArticle.get("id").toString());
            idUser = parseStringFromJson(jsonArticle, "username", "").replaceAll("\\\\", "");
            titleText = parseStringFromJson(jsonArticle, "title", "").replaceAll("\\\\", "");
            subtitleText = parseStringFromJson(jsonArticle, "subtitle", "").replaceAll("\\\\", "");
            category = parseStringFromJson(jsonArticle, "category", "").replaceAll("\\\\", "");
            abstractText = parseStringFromJson(jsonArticle, "abstract", "").replaceAll("\\\\", "");
            bodyText = parseStringFromJson(jsonArticle, "body", "").replaceAll("\\\\", "");
            updateDate = parseStringFromJson(jsonArticle, "update_date", "");

            imageDescription = parseStringFromJson(jsonArticle, "image_description", "").replaceAll("\\\\", "");
            thumbnail = parseStringFromJson(jsonArticle, "thumbnail_image", "").replaceAll("\\\\", "");

            String imageData = parseStringFromJson(jsonArticle, "image_data", "").replaceAll("\\\\", "");

            if (imageData != null && !imageData.isEmpty()) {
                mainImage = new Image(mm, 1, imageDescription, id, imageData);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing Article from JSON: " + e.getMessage());
        }
    }

    // Getters
    public String getTitleText() { return titleText; }
    public String getSubtitleText() { return subtitleText; }
    public String getCategory() { return category; }
    public String getAbstractText() { return abstractText; }
    public String getBodyText() { return bodyText; }
    public String getIdUser() { return idUser; }
    public String getUpdateDate() { return updateDate; }
    public String getThumbnail() { return thumbnail; }
    public String getImageDescription() { return imageDescription; }

    // Setters
    public void setTitleText(String titleText) { this.titleText = titleText; }
    public void setSubtitleText(String subtitleText) { this.subtitleText = subtitleText; }
    public void setCategory(String category) { this.category = category; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

    public Image getImage() {
        if (mainImage == null && thumbnail != null && !thumbnail.isEmpty()) {
            return new Image(mm, 1, "", getId(), thumbnail);
        }
        return mainImage;
    }

    public void setImage(Image image) {
        this.mainImage = image;
    }

    public Image addImage(String b64Image, String description) {
        Image img = new Image(mm, 1, description, getId(), b64Image);
        mainImage = img;
        return img;
    }

    @Override
    public String toString() {
        return "Article [id=" + getId() +
                ", title=" + titleText +
                ", category=" + category +
                ", abstract=" + abstractText +
                ", updateDate=" + updateDate + "]";
    }

    public Hashtable<String, String> getAttributes() {
        Hashtable<String, String> res = new Hashtable<>();
        res.put("category", category);
        res.put("abstract", abstractText);
        res.put("title", titleText);
        res.put("body", bodyText);
        res.put("subtitle", subtitleText);

        if (mainImage != null) {
            res.put("image_data", mainImage.getImage());
            res.put("image_media_type", "image/png");
        }

        if (mainImage != null && mainImage.getDescription() != null && !mainImage.getDescription().isEmpty()) {
            res.put("image_description", mainImage.getDescription());
        } else if (imageDescription != null && !imageDescription.isEmpty()) {
            res.put("image_description", imageDescription);
        }

        return res;
    }
}