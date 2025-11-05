package com.example.newsmanagerproject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private Button getDataButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        getDataButton = findViewById(R.id.getData_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getDataButton.setOnClickListener(view -> getData());
    }

    private void getData() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        RetrofitInstance.getApiInterface().getAllArticles().enqueue(new Callback<List<Article>>() {
            @Override
            public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                progressDialog.dismiss();
                List<Article> articles = response.body();
                adapter = new ArticleAdapter(articles);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onFailure(Call<List<Article>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}