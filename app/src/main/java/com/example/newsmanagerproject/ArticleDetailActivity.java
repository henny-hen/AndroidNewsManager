package com.example.newsmanagerproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newsmanagerproject.models.Article;
import com.example.newsmanagerproject.models.Image;
import com.example.newsmanagerproject.services.ModelManager;
import com.example.newsmanagerproject.tasks.UploadImageTask;
import com.example.newsmanagerproject.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;

public class ArticleDetailActivity extends AppCompatActivity implements UploadImageTask.UploadImageListener {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView titleTextView;
    private TextView subtitleTextView;
    private TextView categoryTextView;
    private TextView abstractTextView;
    private TextView bodyTextView;
    private TextView updateDateTextView;
    private TextView userIdTextView;
    private ImageView articleImageView;
    private Button selectImageButton;
    private Button uploadImageButton;
    private ProgressBar progressBar;

    private int articleId;
    private Article article;
    private Bitmap selectedImage;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        initializeViews();
        setupToolbar();
        loadArticleData();
    }

    private void initializeViews() {
        topAppBar = findViewById(R.id.topAppBar);
        titleTextView = findViewById(R.id.detail_title_textView);
        subtitleTextView = findViewById(R.id.detail_subtitle_textView);
        categoryTextView = findViewById(R.id.detail_category_textView);
        abstractTextView = findViewById(R.id.detail_abstract_textView);
        bodyTextView = findViewById(R.id.detail_body_textView);
        updateDateTextView = findViewById(R.id.detail_updateDate_textView);
        userIdTextView = findViewById(R.id.detail_userId_textView);
        articleImageView = findViewById(R.id.detail_image_imageView);
        selectImageButton = findViewById(R.id.select_image_button);
        uploadImageButton = findViewById(R.id.upload_image_button);
        progressBar = findViewById(R.id.detail_progressBar);

        selectImageButton.setOnClickListener(v -> openImagePicker());
        uploadImageButton.setOnClickListener(v -> uploadImage());
        uploadImageButton.setEnabled(false);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> finish());
    }

    private void loadArticleData() {
        Intent intent = getIntent();
        articleId = intent.getIntExtra("ARTICLE_ID", -1);
        String articleTitle = intent.getStringExtra("ARTICLE_TITLE");

        if (articleId == -1) {
            Toast.makeText(this, "Error: Invalid article", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set title immediately
        if (articleTitle != null) {
            setTitle(articleTitle);
        }

        // Download full article details
        loadArticleDetails();
    }

    private void loadArticleDetails() {
        progressBar.setVisibility(View.VISIBLE);

        Thread thread = new Thread(() -> {
            try {
                ModelManager mm = ModelManager.getInstance();
                article = mm.getArticle(articleId);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    displayArticleDetails();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading article: " + e.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                });
            }
        });
        thread.start();
    }

    private void displayArticleDetails() {
        if (article == null) return;

        titleTextView.setText(article.getTitleText());
        
        String subtitle = article.getSubtitleText();
        if (subtitle != null && !subtitle.isEmpty()) {
            subtitleTextView.setText(parseHtml(subtitle));
            subtitleTextView.setVisibility(View.VISIBLE);
        } else {
            subtitleTextView.setVisibility(View.GONE);
        }

        categoryTextView.setText("Category: " + article.getCategory());
        abstractTextView.setText(article.getAbstractText());
        
        String body = article.getBodyText();
        if (body != null && !body.isEmpty()) {
            bodyTextView.setText(parseHtml(body));
            bodyTextView.setVisibility(View.VISIBLE);
        } else {
            bodyTextView.setVisibility(View.GONE);
        }

        updateDateTextView.setText("Last updated: " + article.getUpdateDate());
        userIdTextView.setText("User ID: " + article.getIdUser());

        // Load image
        Image image = article.getImage();
        if (image != null && image.getImage() != null && !image.getImage().isEmpty()) {
            Bitmap bitmap = Utils.base64StringToImg(image.getImage());
            if (bitmap != null) {
                articleImageView.setImageBitmap(bitmap);
            }
        }
    }

    private Spanned parseHtml(String html) {
        if (html == null || html.isEmpty()) {
            return Html.fromHtml("", Html.FROM_HTML_MODE_COMPACT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(html);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                articleImageView.setImageBitmap(selectedImage);
                uploadImageButton.setEnabled(true);
                Toast.makeText(this, "Image selected. Click Upload to save.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage() {
        if (selectedImage == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (article == null) {
            Toast.makeText(this, "Article not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String base64Image = Utils.encodeImage(selectedImage);
        UploadImageTask task = new UploadImageTask(this, article, base64Image, "Updated from Android");
        Thread thread = new Thread(task);
        thread.start();
    }

    @Override
    public void onUploadStarted() {
        progressBar.setVisibility(View.VISIBLE);
        uploadImageButton.setEnabled(false);
        selectImageButton.setEnabled(false);
    }

    @Override
    public void onUploadFinished(boolean success, String message) {
        progressBar.setVisibility(View.GONE);
        uploadImageButton.setEnabled(false);
        selectImageButton.setEnabled(true);
        
        if (success) {
            Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
            selectedImage = null;
        } else {
            Toast.makeText(this, "Upload failed: " + message, Toast.LENGTH_LONG).show();
            uploadImageButton.setEnabled(true);
        }
    }
}