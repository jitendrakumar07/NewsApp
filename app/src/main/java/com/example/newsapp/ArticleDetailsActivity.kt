package com.example.newsapp

import Article
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@Suppress("DEPRECATION")
class ArticleDetailsActivity : AppCompatActivity() {
    private lateinit var articleTitle: TextView
    private lateinit var articleImage: ImageView
    private lateinit var articleContent: TextView
    private lateinit var articleDescription: TextView
    private lateinit var articleAuthor: TextView
    private lateinit var articleDate: TextView
    private lateinit var shareButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_details)

        // Initialize views
        articleTitle = findViewById(R.id.article_title)
        articleImage = findViewById(R.id.article_image)
        articleContent = findViewById(R.id.article_content)
        articleDescription = findViewById(R.id.article_description)
        articleAuthor = findViewById(R.id.article_author)
        articleDate = findViewById(R.id.article_date)
        shareButton = findViewById(R.id.share_button)

        // Get the article object passed from the previous activity
        val article = intent.getSerializableExtra("article") as? Article
        article?.let {
            // Populate the UI with article data
            articleTitle.text = it.title
            articleContent.text = it.content
            articleDescription.text = it.description
            articleAuthor.text = it.author
            articleDate.text = it.date

            // Load image with error handling
            Glide.with(this)
                .load(it.imageUrl)
                .apply(RequestOptions().error(R.drawable.img)) // Placeholder if image fails to load
                .into(articleImage)
        } ?: run {
            // Handle the case where the article is null
            articleTitle.text = getString(R.string.error_article_title)
            articleContent.text = getString(R.string.error_article_content)
            articleDescription.text = ""
            articleAuthor.text = ""
            articleDate.text = ""
        }

        // Set up the share button
        shareButton.setOnClickListener {
            shareArticle(article)
        }
    }

    private fun shareArticle(article: Article?) {
        article?.let {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${it.title}\n\n${it.content}\n\n${it.description}")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share article via"))
        }
    }
}
