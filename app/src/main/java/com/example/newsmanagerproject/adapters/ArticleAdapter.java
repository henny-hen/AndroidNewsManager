package com.example.newsmanagerproject.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsmanagerproject.R;
import com.example.newsmanagerproject.models.Article;
import com.example.newsmanagerproject.utils.Utils;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private List<Article> data;
    private OnArticleClickListener clickListener;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }

    public ArticleAdapter(List<Article> data, OnArticleClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = data.get(position);
        
        holder.titleTextView.setText(article.getTitleText());
        holder.abstractTextView.setText(article.getAbstractText());
        holder.categoryTextView.setText(article.getCategory());
        
        // Load thumbnail if available - now always visible as background
        String thumbnail = article.getThumbnail();
        if (thumbnail != null && !thumbnail.isEmpty()) {
            Bitmap bitmap = Utils.base64StringToImg(thumbnail);
            if (bitmap != null) {
                holder.thumbnailImageView.setImageBitmap(bitmap);
            } else {
                // Set a default placeholder or color
                holder.thumbnailImageView.setImageResource(android.R.color.darker_gray);
            }
        } else {
            // Set a default placeholder or color
            holder.thumbnailImageView.setImageResource(android.R.color.darker_gray);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onArticleClick(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView abstractTextView;
        TextView categoryTextView;
        ImageView thumbnailImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_textView);
            abstractTextView = itemView.findViewById(R.id.abstract_textView);
            categoryTextView = itemView.findViewById(R.id.category_textView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_imageView);
        }
    }
}