package dav.project.andazioni.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import dav.project.andazioni.NetworkUtils
import dav.project.andazioni.R
import dav.project.andazioni.databinding.ActivityMainBinding
import kotlin.random.Random
import dav.project.andazioni.BuildConfig

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseRef: DatabaseReference
    private val PREFS_NAME = "proverb_prefs"
    private val KEY_CURRENT_PROVERB = "current_proverb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Access the API keys
        val apiKey1 = BuildConfig.API_KEY1
        val apiKey2 = BuildConfig.API_KEY2



        // Use the API keys
        println("API Key 1: $apiKey1")
        println("API Key 2: $apiKey2")

        // Status Bar Color
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Initialize Firebase Database reference
        databaseRef = FirebaseDatabase.getInstance().reference.child("Proverbs")

        // Restore the saved proverb state or fetch a new one if none is saved
        val savedProverb = getSavedProverb()
        if (savedProverb != null) {
            binding.tvPorverb.text = savedProverb
            binding.progressBar.visibility = View.GONE // Hide progress bar if we have a saved proverb
        } else {
            fetchRandomProverb()
        }

        binding.tvPorverb.setOnClickListener {
            copyProverbToClipboard(binding.tvPorverb.text.toString())
        }


        binding.generateBtn.setOnClickListener {
            fetchRandomProverb()
        }

        binding.btnShare.setOnClickListener {
            shareProverb(binding.tvPorverb.text.toString())
        }


    }

    override fun onResume() {
        super.onResume()
        // Restore the saved proverb state when the activity resumes
        val savedProverb = getSavedProverb()
        if (savedProverb != null) {
            binding.tvPorverb.text = savedProverb
            binding.progressBar.visibility = View.GONE // Ensure progress bar is hidden
        }
    }

    private fun fetchRandomProverb() {
        if (NetworkUtils.isInternetAvailable(this)) {
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val proverbsList = mutableListOf<String>()

                    // Iterate through the list of proverbs
                    for (proverbSnapshot in dataSnapshot.children) {
                        val proverb = proverbSnapshot.getValue(String::class.java)
                        if (proverb != null) {
                            proverbsList.add(proverb)
                        }
                    }

                    if (proverbsList.isNotEmpty()) {
                        val randomIndex = Random.nextInt(proverbsList.size)
                        val randomProverb = proverbsList[randomIndex]
                        binding.tvPorverb.text = ",,$randomProverb''"
                        saveProverb(binding.tvPorverb.text.toString())
                    } else {
                        binding.tvPorverb.text = "ანდაზა არ მოიძებნა"
                    }

                    // Hide ProgressBar after data is fetched
                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    binding.tvPorverb.text = "მოხდა შეცდომა"
                    // Hide ProgressBar if there is an error
                    binding.progressBar.visibility = View.GONE
                }
            })
        } else {
            Toast.makeText(this, "გთხოვთ, შეამოწმეთ ინტერნეტ კავშირი", Toast.LENGTH_LONG).show()
            binding.progressBar.visibility = View.GONE // Hide progress bar if no internet
        }
    }

    private fun shareProverb(proverb: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, proverb)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share proverb via"))
    }

    private fun copyProverbToClipboard(proverb: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Proverb", proverb)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "ანდაზა კოპირებულია", Toast.LENGTH_SHORT).show()
    }

    private fun saveProverb(proverb: String) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_CURRENT_PROVERB, proverb)
        editor.apply()
    }

    private fun getSavedProverb(): String? {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_CURRENT_PROVERB, null)
    }

    private fun addProverbToFavorites(proverb: String) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val favoriteProverbs = sharedPreferences.getStringSet("favorite_proverbs", mutableSetOf()) ?: mutableSetOf()
        favoriteProverbs.add(proverb)

        editor.putStringSet("favorite_proverbs", favoriteProverbs)
        editor.apply()

        Toast.makeText(this, "ანდაზა დამატებულია ფავორიტებში", Toast.LENGTH_SHORT).show()
    }
}
