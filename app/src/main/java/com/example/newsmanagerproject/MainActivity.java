package com.example.newsmanagerproject;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsmanagerproject.adapters.ArticleAdapter;
import com.example.newsmanagerproject.models.Article;
import com.example.newsmanagerproject.services.ModelManager;
import com.example.newsmanagerproject.tasks.DownloadArticlesTask;
import com.example.newsmanagerproject.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DownloadArticlesTask.DownloadArticlesListener {

    private static final int LOGIN_REQUEST_CODE = 1001;

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private ExtendedFloatingActionButton downloadButton;
    private Button loginButton;
    private ProgressBar progressBar;
    private ChipGroup categoryChipGroup;
    private View loginStatusIndicator;
    private TextView loginStatusText;
    private List<Article> articleList;
    private List<Article> allArticles;
    private String selectedCategory = "All";
    
    private ActivityResultLauncher<Intent> loginLauncher;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        
        initializeViews();
        restoreSessionIfAvailable();
        setupRecyclerView();
        setupListeners();
        setupLoginLauncher();
        updateLoginStatus();
        
        // Auto-download articles on start
        startDownload();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        downloadButton = findViewById(R.id.getData_button);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progressBar);
        categoryChipGroup = findViewById(R.id.category_chip_group);
        loginStatusIndicator = findViewById(R.id.login_status_indicator);
        loginStatusText = findViewById(R.id.login_status_text);
        articleList = new ArrayList<>();
        allArticles = new ArrayList<>();
        

    }

    private void initializeModelManager() {
        // ModelManager is already initialized as singleton with ANON07
        // No need for explicit initialization unless you want to login
    }
    
    private void restoreSessionIfAvailable() {
        if (sessionManager.hasStoredSession()) {
            String userId = sessionManager.getUserId();
            String authType = sessionManager.getAuthType();
            String apiKey = sessionManager.getApiKey();
            String username = sessionManager.getUsername();
            
            if (userId != null && authType != null && apiKey != null && username != null) {
                ModelManager mm = ModelManager.getInstance();
                mm.stayLoggedIn(userId, authType, apiKey, username);
                Toast.makeText(this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArticleAdapter(articleList, this::onArticleClick);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        downloadButton.setOnClickListener(v -> startDownload());
        
        loginButton.setOnClickListener(v -> {
            if (isLoggedIn()) {
                // Logout
                logout();
            } else {
                // Login
                Intent intent = new Intent(this, LoginActivity.class);
                loginLauncher.launch(intent);
            }
        });
        
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    selectedCategory = chip.getText().toString();
                    filterArticlesByCategory();
                }
            }
        });
    }
    
    private void setupLoginLauncher() {
        loginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    updateLoginStatus();
                    Toast.makeText(this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                    // Refresh articles after successful login
                    startDownload();
                }
            }
        );
    }
    
    private void updateLoginStatus() {
        boolean loggedIn = isLoggedIn();
        
        if (loggedIn) {
            loginStatusIndicator.setBackgroundResource(R.drawable.status_indicator_green);
            loginStatusText.setText("Logged in as: " + ModelManager.getInstance().getUsernameS());
            loginButton.setText("Sign Out");
        } else {
            loginStatusIndicator.setBackgroundResource(R.drawable.status_indicator);
            loginStatusText.setText("Not logged in");
            loginButton.setText("Login");
        }
    }
    
    private boolean isLoggedIn() {
        return ModelManager.getInstance().isLoggedIn();
    }
    
    private void logout() {
        // Clear session from SharedPreferences
        sessionManager.clearSession();
        
        // Reset ModelManager to anonymous mode
        ModelManager.getInstance().logout();
        
        // Update UI
        updateLoginStatus();
        startDownload();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void startDownload() {
        DownloadArticlesTask task = new DownloadArticlesTask(this);
        Thread thread = new Thread(task);
        thread.start();
    }

    private void onArticleClick(Article article) {
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("ARTICLE_ID", article.getId());
        intent.putExtra("ARTICLE_TITLE", article.getTitleText());
        startActivity(intent);
    }

    @Override
    public void onDownloadStarted() {
        progressBar.setVisibility(View.VISIBLE);
        downloadButton.setEnabled(false);
    }

    @Override
    public void onDownloadFinished(List<Article> articles) {
        progressBar.setVisibility(View.GONE);
        downloadButton.setEnabled(true);
        
        allArticles.clear();
        allArticles.addAll(articles);
        
        updateCategorySpinner();
        filterArticlesByCategory();
        
        Toast.makeText(this, "Loaded " + articles.size() + " articles", Toast.LENGTH_SHORT).show();
    }
    
    private void updateCategorySpinner() {
        // Extract unique categories from articles
        Set<String> categorySet = new HashSet<>();
        categorySet.add("All");
        
        for (Article article : allArticles) {
            String category = article.getCategory();
            if (category != null && !category.trim().isEmpty()) {
                categorySet.add(category);
            }
        }
        
        // Convert to sorted list
        List<String> categories = new ArrayList<>(categorySet);
        categories.sort((a, b) -> {
            if (a.equals("All")) return -1;
            if (b.equals("All")) return 1;
            return a.compareTo(b);
        });
        
        // Clear existing chips and add new ones
        categoryChipGroup.removeAllViews();
        
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipIconVisible(false);
            chip.setCheckedIconVisible(true);
            
            // Style the chip with state-based colors
            chip.setChipBackgroundColorResource(R.color.chip_background_color);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color, null));
            chip.setChipStrokeColorResource(R.color.chip_stroke);
            chip.setChipStrokeWidth(3f);
            chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            // Add padding for better touch target
            chip.setEnsureMinTouchTargetSize(true);
            chip.setMinHeight(48);
            
            // Set "All" as checked by default
            if (category.equals("All")) {
                chip.setChecked(true);
            }
            
            categoryChipGroup.addView(chip);
        }
    }
    
    private void filterArticlesByCategory() {
        articleList.clear();
        
        if (selectedCategory.equals("All")) {
            articleList.addAll(allArticles);
        } else {
            for (Article article : allArticles) {
                if (selectedCategory.equals(article.getCategory())) {
                    articleList.add(article);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Showing " + articleList.size() + " articles", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadError(String error) {
        progressBar.setVisibility(View.GONE);
        downloadButton.setEnabled(true);
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
    }
}