package com.example.newsapp

import Article
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ArticlesAdapter(
    private var articles: MutableList<Article>,
    private val itemClickListener: (Article) -> Unit // Lambda for item click
) : RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.article_title)
        val contentTextView: TextView = itemView.findViewById(R.id.article_content)
        val imageView: ImageView = itemView.findViewById(R.id.article_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.titleTextView.text = article.title
        holder.contentTextView.text = article.content

        Glide.with(holder.itemView.context)
            .load(article.imageUrl)
            .apply(RequestOptions().error(R.drawable.img)) // Error placeholder
            .into(holder.imageView)

        // Handle item click to open details
        holder.itemView.setOnClickListener {
            itemClickListener(article) // Use the provided click listener
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateData(newArticles: List<Article>) {
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }
}
