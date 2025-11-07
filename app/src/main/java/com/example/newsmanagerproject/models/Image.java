package com.example.newsmanagerproject.models;

import com.example.newsmanagerproject.services.ModelManager;
import com.example.newsmanagerproject.utils.Utils;

import org.json.simple.JSONObject;

import java.util.Hashtable;

public class Image extends ModelEntity {
    private int order;
    private String description;
    private int idArticle;
    private String image;

    public Image(ModelManager mm, int order, String description, int idArticle, String b64Image) {
        super(mm);
        this.id = -1;
        this.order = order;
        this.description = description;
        this.idArticle = idArticle;
        this.image = Utils.createScaledStrImage(b64Image, 500, 500);
    }

    public Image(ModelManager mm, JSONObject jsonImage) {
        super(mm);
        try {
            id = Integer.parseInt(jsonImage.get("id").toString());
            order = Integer.parseInt(jsonImage.get("order").toString());
            description = jsonImage.getOrDefault("description", "").toString().replaceAll("\\\\", "");
            idArticle = Integer.parseInt(jsonImage.get("id_article").toString().replaceAll("\\\\", ""));
            image = jsonImage.get("data").toString().replaceAll("\\\\", "");
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing Image from JSON: " + e.getMessage());
        }
    }

    // Getters
    public int getOrder() { return order; }
    public String getDescription() { return description; }
    public int getIdArticle() { return idArticle; }
    public String getImage() { return image; }

    // Setters
    public void setOrder(int order) { this.order = order; }
    public void setDescription(String description) { this.description = description; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return "Image [id=" + getId() +
                ", order=" + order +
                ", description=" + description +
                ", id_article=" + idArticle + "]";
    }

    protected Hashtable<String, String> getAttributes() {
        Hashtable<String, String> res = new Hashtable<>();
        res.put("id_article", "" + idArticle);
        res.put("order", "" + order);
        res.put("description", description);
        res.put("data", image);
        res.put("media_type", "image/png");
        return res;
    }
}