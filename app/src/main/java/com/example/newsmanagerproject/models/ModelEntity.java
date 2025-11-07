package com.example.newsmanagerproject.models;

import com.example.newsmanagerproject.services.ModelManager;

import org.json.simple.JSONObject;

import java.util.Hashtable;

public abstract class ModelEntity {
    protected int id;
    protected ModelManager mm;

    public ModelEntity(ModelManager mm) {
        this.mm = mm;
        this.id = -1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void save() throws Exception {
        int returnedId = mm.save(this);
        if (this.id < 0 && returnedId > 0) {
            this.id = returnedId;
        }
    }

    public void delete() throws Exception {
        mm.delete(this);
    }

    protected abstract Hashtable<String, String> getAttributes();

    public org.json.simple.JSONObject toJSON() {
        JSONObject json = new JSONObject();
        Hashtable<String, String> attributes = getAttributes();
        for (String key : attributes.keySet()) {
            json.put(key, attributes.get(key));
        }
        if (id > 0) {
            json.put("id", id);
        }
        return json;
    }
}