package dav.project.andazioni.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dav.project.andazioni.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content view first
        setContentView(R.layout.activity_intro)

        // Then find your views
        val textView = findViewById<TextView>(R.id.tvTerms)
        val content = SpannableString("წესებსა და პირობებს")
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        textView.text = content

        // Check if the app is launched for the first time
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        if (!isFirstLaunch) {
            // If it's not the first launch, go directly to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // If it's the first launch, show IntroActivity
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Status Bar color
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val tac: TextView = findViewById(R.id.tvTerms)
        val start: Button = findViewById(R.id.startBtn)

        tac.setOnClickListener {
            startActivity(Intent(this@IntroActivity, TacActivity::class.java))
        }

        start.setOnClickListener {
            // Mark that the app has been launched before
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()

            // Go to MainActivity
            startActivity(Intent(this@IntroActivity, MainActivity::class.java))
            finish()
        }
    }
}
