package com.example.milestokm

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val title = TextView(this).apply {
            text = "Fermentation Info"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val infoText = TextView(this).apply {
            text = """
                App for calculating alcohol percentage in Kilju using the Logistic Growth Formula:
                
                A(t) = ABVmax / (1 + e^(-k * (t - t0)))
                
                Variables:
                - ABVmax: The maximum potential alcohol.
                - k: The growth rate (speed of fermentation).
                - t: Current time in days.
                - t0: The lag phase (time before fermentation starts).
                
                Standard Constants:
                - 17g of sugar per liter produces approx 1% ABV.
                - Fermentation speed increases by about 7% for every degree above 20°C.
            """.trimIndent()
            textSize = 16f
            setPadding(0, (16 * resources.displayMetrics.density).toInt(), 0, 0)
        }

        rootLayout.addView(title)
        rootLayout.addView(infoText)
        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }
}
