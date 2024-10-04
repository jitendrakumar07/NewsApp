@file:Suppress("DEPRECATION")

package com.example.newsapp


import Article
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var articlesAdapter: ArticlesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupFirebase()
        setupBottomNavigation()
        fetchArticles()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.rec)
        recyclerView.layoutManager = LinearLayoutManager(this)
        articlesAdapter = ArticlesAdapter(mutableListOf(), ::openArticleDetails)
        recyclerView.adapter = articlesAdapter // Set adapter directly
    }

    private fun setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().reference.child("articles")
    }

    private fun setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_admin -> {
                    openLoginActivity()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchArticles() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val articlesList = mutableListOf<Article>()
                for (snapshot in dataSnapshot.children) {
                    val article = snapshot.getValue(Article::class.java)
                    article?.let { articlesList.add(it) }
                }
                articlesAdapter.updateData(articlesList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "Error fetching articles", databaseError.toException())
            }
        })
    }


    private fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun openArticleDetails(article: Article) { // Changed type to Article
        val intent = Intent(this, ArticleDetailsActivity::class.java).apply {
            putExtra("article", article) // Pass the article object
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up listener to prevent memory leaks if needed
        // databaseReference.removeEventListener(listener)
    }
}
