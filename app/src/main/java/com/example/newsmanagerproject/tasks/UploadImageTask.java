package com.example.newsmanagerproject.tasks;

import android.app.Activity;

import com.example.newsmanagerproject.models.Article;
import com.example.newsmanagerproject.models.Image;

public class UploadImageTask implements Runnable {
    private final Activity activity;
    private final Article article;
    private final String base64Image;
    private final String description;

    public interface UploadImageListener {
        void onUploadStarted();
        void onUploadFinished(boolean success, String message);
    }

    public UploadImageTask(Activity activity, Article article, String base64Image, String description) {
        this.activity = activity;
        this.article = article;
        this.base64Image = base64Image;
        this.description = description;
    }

    @Override
    public void run() {
        // Notify start on UI thread
        activity.runOnUiThread(() -> {
            if (activity instanceof UploadImageListener) {
                ((UploadImageListener) activity).onUploadStarted();
            }
        });

        try {
            // Add image to article
            Image image = article.addImage(base64Image, description);
            
            // Save image to server
            image.save();

            // Notify success on UI thread
            activity.runOnUiThread(() -> {
                if (activity instanceof UploadImageListener) {
                    ((UploadImageListener) activity).onUploadFinished(true, "Image uploaded successfully");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            // Notify error on UI thread
            activity.runOnUiThread(() -> {
                if (activity instanceof UploadImageListener) {
                    ((UploadImageListener) activity).onUploadFinished(false, e.getMessage());
                }
            });
        }
    }
}