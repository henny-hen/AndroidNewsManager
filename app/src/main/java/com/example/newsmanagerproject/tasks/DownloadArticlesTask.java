package com.example.newsmanagerproject.tasks;

import android.os.Handler;
import android.os.Looper;

import com.example.newsmanagerproject.MainActivity;
import com.example.newsmanagerproject.models.Article;
import com.example.newsmanagerproject.services.ModelManager;

import java.util.List;

public class DownloadArticlesTask implements Runnable {
    private final MainActivity activity;
    private final ModelManager modelManager;

    public interface DownloadArticlesListener {
        void onDownloadStarted();
        void onDownloadFinished(List<Article> articles);
        void onDownloadError(String error);
    }

    public DownloadArticlesTask(MainActivity activity) {
        this.activity = activity;
        this.modelManager = ModelManager.getInstance();
    }

    @Override
    public void run() {
        // Notify start on UI thread
        activity.runOnUiThread(activity::onDownloadStarted);

        try {
            // Download articles from server
            List<Article> articles = modelManager.getArticles();

            // Notify success on UI thread
            activity.runOnUiThread(() -> activity.onDownloadFinished(articles));

        } catch (Exception e) {
            e.printStackTrace();
            // Notify error on UI thread
            activity.runOnUiThread(() -> activity.onDownloadError(e.getMessage()));
        }
    }
}