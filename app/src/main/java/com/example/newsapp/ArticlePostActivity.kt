package com.example.newsapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.text.Editable
import android.text.TextWatcher

@Suppress("DEPRECATION")
class ArticlePostActivity : AppCompatActivity() {
    private lateinit var articleTitle: TextInputEditText
    private lateinit var articleDescription: TextInputEditText
    private lateinit var articleAuthor: TextInputEditText
    private lateinit var articleDate: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var uploadImageButton: Button
    private lateinit var articleImage: ImageView
    private lateinit var postArticleButton: Button
    private lateinit var clearButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_article)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        // Initialize UI components
        articleTitle = findViewById(R.id.article_title)
        articleDescription = findViewById(R.id.article_description)
        articleAuthor = findViewById(R.id.article_author)
        articleDate = findViewById(R.id.article_date)
        categorySpinner = findViewById(R.id.category_spinner)
        uploadImageButton = findViewById(R.id.upload_image_button)
        articleImage = findViewById(R.id.article_image)

        postArticleButton = findViewById(R.id.post_article_button)
        clearButton = findViewById(R.id.clear_button)

        progressBar = findViewById(R.id.progress_bar)

        // Set up listeners
        uploadImageButton.setOnClickListener { openImagePicker() }
        postArticleButton.setOnClickListener { postArticle() }
        clearButton.setOnClickListener { clearFields() }

        // Load categories into spinner
        val categories = arrayOf("News", "Sports", "Entertainment", "Technology")
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)

        // Character count for title
        articleTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                findViewById<TextView>(R.id.title_char_count).text = "${s?.length ?: 0}/100"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Character count for description
        articleDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                findViewById<TextView>(R.id.description_char_count).text = "${s?.length ?: 0}/1000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                articleImage.setImageURI(selectedImageUri)
                articleImage.visibility = View.VISIBLE
                Log.d("ArticlePostActivity", "Selected Image URI: $selectedImageUri")
            } else {
                Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun postArticle() {
        val title = articleTitle.text.toString().trim()
        val description = articleDescription.text.toString().trim()
        val author = articleAuthor.text.toString().trim()
        val date = articleDate.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()

        if (title.isEmpty() || description.isEmpty() || author.isEmpty() || date.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        // Upload image to Firebase Storage
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
        Log.d("ArticlePostActivity", "Uploading image to: ${imageRef.path}")

        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                Log.d("ArticlePostActivity", "Image upload successful")
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveArticleToDatabase(title, description, author, date, category, uri.toString())
                }
            }.addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Log.e("ArticlePostActivity", "Image upload failed: ${exception.message}")
                Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveArticleToDatabase(title: String, description: String, author: String, date: String, category: String, imageUrl: String) {
        val articleId = database.child("articles").push().key // Generate unique key for the article
        if (articleId != null) {
            val articleData = hashMapOf(
                "title" to title,
                "description" to description,
                "author" to author,
                "date" to date,
                "category" to category,
                "imageUrl" to imageUrl,
                "userId" to auth.currentUser?.uid
            )

            database.child("articles").child(articleId).setValue(articleData)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Article posted successfully", Toast.LENGTH_SHORT).show()
                    clearFields()
                }.addOnFailureListener { exception ->
                    progressBar.visibility = View.GONE
                    Log.e("ArticlePostActivity", "Error posting article: ${exception.message}")
                    Toast.makeText(this, "Error posting article: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun clearFields() {
        articleTitle.text?.clear()
        articleDescription.text?.clear()
        articleAuthor.text?.clear()
        articleDate.text?.clear()
        articleImage.setImageURI(null)
        articleImage.visibility = View.GONE
        selectedImageUri = null
    }

}
