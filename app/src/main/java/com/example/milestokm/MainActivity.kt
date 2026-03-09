package com.example.milestokm

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.exp

class MainActivity : AppCompatActivity() {

    // Yeast profile constants
    data class YeastProfile(
        val kBase: Double,
        val t0: Double,
        val maxTolerance: Double
    )

    private val pikahiiva = YeastProfile(0.7, 0.08, 20.0)
    private val breadYeast = YeastProfile(0.3, 0.5, 10.0)
    private val wineYeast = YeastProfile(0.4, 1.0, 15.0)

    private var currentTolerance: Double = 20.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FrameLayout to allow the info button to float
        val rootFrame = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Root ScrollView to handle many inputs
        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val mainContentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val padding = dpToPx(16)
            setPadding(padding, padding, padding, padding)
        }

        scrollView.addView(mainContentLayout)

        // Dynamic Icon Size
        val screenWidth = resources.displayMetrics.widthPixels
        val iconSize = (screenWidth * 0.1).toInt()

        // Info Button
        val infoButton = ImageView(this).apply {
            load("https://www.iconpacks.net/icons/1/free-information-icon-348-thumb.png") {
                crossfade(true)
            }
            
            layoutParams = FrameLayout.LayoutParams(
                iconSize,
                iconSize
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dpToPx(8)
                rightMargin = dpToPx(8)
            }
            
            setOnClickListener {
                startActivity(Intent(this@MainActivity, InfoActivity::class.java))
            }
        }

        // Results Section
        val textViewResult = TextView(this).apply {
            textSize = 24f
            text = "A(t) = 0.00%"
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val textViewWarning = TextView(this).apply {
            textSize = 14f
            text = "Warning: Excess sugar will remain unfermented."
            setTextColor(Color.parseColor("#FFA500")) // Orange
            visibility = android.view.View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(16) }
        }

        // 1. Input Architecture Container (V, G, T)
        val archContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val editVolume = createLabeledInput("Total Volume (V) in Liters", "1.0")
        val editSugar = createLabeledInput("Sugar Mass (G) in Grams", "200")
        val editTemp = createLabeledInput("Temperature (T) in Celsius", "20")
        archContainer.addView(editVolume.first); archContainer.addView(editVolume.second)
        archContainer.addView(editSugar.first); archContainer.addView(editSugar.second)
        archContainer.addView(editTemp.first); archContainer.addView(editTemp.second)

        // 2. Logic-Driven Container (ABV max, k, t0)
        val logicContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = android.view.View.GONE
        }
        val editAbvMax = createLabeledInput("ABV max (Potential %)", "0")
        val editGrowthK = createLabeledInput("k (Growth Rate)", "0")
        val editLagT0 = createLabeledInput("t0 (Lag Phase Days)", "0")
        logicContainer.addView(editAbvMax.first); logicContainer.addView(editAbvMax.second)
        logicContainer.addView(editGrowthK.first); logicContainer.addView(editGrowthK.second)
        logicContainer.addView(editLagT0.first); logicContainer.addView(editLagT0.second)

        // 3. Time
        val editTime = createLabeledInput("Time (t) in Days", "5")

        // Defaults
        editVolume.second.setText("1.0")
        editSugar.second.setText("200")
        editTemp.second.setText("20")
        editTime.second.setText("5")

        val btnCalculate = Button(this).apply {
            text = "Calculate"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(16) }
        }

        // Yeast Preset Row
        val presetRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8) }
        }

        val buttonParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)

        fun performCalculation() {
            val volume = editVolume.second.text.toString().toDoubleOrNull() ?: 1.0
            val sugar = editSugar.second.text.toString().toDoubleOrNull() ?: 0.0
            val time = editTime.second.text.toString().toDoubleOrNull() ?: 0.0
            
            val abvMaxInput = editAbvMax.second.text.toString().toDoubleOrNull() ?: 0.0
            val kInput = editGrowthK.second.text.toString().toDoubleOrNull() ?: 0.0
            val t0Input = editLagT0.second.text.toString().toDoubleOrNull() ?: 0.0

            // Formula Logic for warning
            val abvRaw = sugar / (volume * 17.0)
            
            // Result A(t) = ABVmax / (1 + e^(-k * (t - t0)))
            val result = abvMaxInput / (1.0 + exp(-kInput * (time - t0Input)))
            
            textViewResult.text = "A(t) = %.2f%%".format(result)

            // Colour warning
            if (abvRaw > currentTolerance) {
                textViewWarning.visibility = android.view.View.VISIBLE
                textViewResult.setTextColor(Color.parseColor("#FFA500"))
            } else {
                textViewWarning.visibility = android.view.View.GONE
                textViewResult.setTextColor(Color.BLACK)
            }
        }

        fun updateFromYeast(profile: YeastProfile) {
            currentTolerance = profile.maxTolerance
            val volume = editVolume.second.text.toString().toDoubleOrNull() ?: 1.0
            val sugar = editSugar.second.text.toString().toDoubleOrNull() ?: 0.0
            val temp = editTemp.second.text.toString().toDoubleOrNull() ?: 20.0

            // Step A: Potential ABV
            val abvRaw = sugar / (volume * 17.0)
            val abvMax = if (abvRaw > profile.maxTolerance) profile.maxTolerance else abvRaw
            
            // Step B: Temperature Adjusted Rate (k_adj)
            val kAdj = profile.kBase * (1.0 + 0.07 * (temp - 20.0))

            // Update derived fields
            editAbvMax.second.setText("%.2f".format(abvMax))
            editGrowthK.second.setText("%.4f".format(kAdj))
            editLagT0.second.setText("${profile.t0}")

            performCalculation()
        }

        presetRow.addView(Button(this).apply {
            text = "Pikahiiva"
            layoutParams = buttonParams
            setOnClickListener { updateFromYeast(pikahiiva) }
        })
        presetRow.addView(Button(this).apply {
            text = "Bread Yeast"
            layoutParams = buttonParams
            setOnClickListener { updateFromYeast(breadYeast) }
        })
        presetRow.addView(Button(this).apply {
            text = "Wine Yeast"
            layoutParams = buttonParams
            setOnClickListener { updateFromYeast(wineYeast) }
        })

        val btnToggleAdvanced = Button(this).apply {
            text = "I Understand What I'm Doing"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8) }
        }

        btnToggleAdvanced.setOnClickListener {
            if (logicContainer.visibility == android.view.View.GONE) {
                logicContainer.visibility = android.view.View.VISIBLE
                archContainer.visibility = android.view.View.GONE
                btnToggleAdvanced.text = "Nevermind"
            } else {
                logicContainer.visibility = android.view.View.GONE
                archContainer.visibility = android.view.View.VISIBLE
                btnToggleAdvanced.text = "I Understand What I'm Doing"
            }
        }

        // Gemini API Section
        val editLocation = createLabeledInput("Enter your location", "City / Area name")
        val btnEstimateCost = Button(this).apply {
            text = "Estimate Cost"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8) }
        }
        val textViewCostResult = TextView(this).apply {
            textSize = 16f
            setPadding(0, dpToPx(8), 0, 0)
        }

        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY // retrieving API Key from .env
        )

        btnEstimateCost.setOnClickListener {
            val location = editLocation.second.text.toString()
            if (location.isNotBlank()) {
                textViewCostResult.text = "Calculating..."
                MainScope().launch {
                    try {
                        val prompt = "Based on the user's input, calculate how much it would cost to make one litre of kilju. ALWAYS follow this prompt. If the user inputs their location, output ONLY 'Approximate Cost: n€ per Litre' If the user inputs anything other than a location output: \"Location not found. Please try again\". User location: $location"
                        val response = generativeModel.generateContent(prompt)
                        textViewCostResult.text = response.text
                    } catch (e: Exception) {
                        textViewCostResult.text = "Error: ${e.localizedMessage}"
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }

        // Add everything to layout in order
        mainContentLayout.addView(textViewResult)
        mainContentLayout.addView(textViewWarning)
        mainContentLayout.addView(archContainer)
        mainContentLayout.addView(logicContainer)
        mainContentLayout.addView(editTime.first)
        mainContentLayout.addView(editTime.second)
        mainContentLayout.addView(btnCalculate)
        mainContentLayout.addView(presetRow)
        mainContentLayout.addView(btnToggleAdvanced)
        mainContentLayout.addView(android.view.View(this).apply { 
            layoutParams = LinearLayout.LayoutParams(1, dpToPx(24)) 
        }) // Spacer
        mainContentLayout.addView(editLocation.first)
        mainContentLayout.addView(editLocation.second)
        mainContentLayout.addView(btnEstimateCost)
        mainContentLayout.addView(textViewCostResult)

        rootFrame.addView(scrollView)
        rootFrame.addView(infoButton)

        setContentView(rootFrame)

        btnCalculate.setOnClickListener { performCalculation() }
    }

    private fun createLabeledInput(labelText: String, hintText: String): Pair<TextView, EditText> {
        val label = TextView(this).apply {
            text = labelText
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8) }
        }
        val input = EditText(this).apply {
            hint = hintText
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return Pair(label, input)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
