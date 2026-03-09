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

class MainActivity : AppCompatActivity() {

    private var currentTolerance: Double = 20.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root UI Layouts
        val rootFrame = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

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
            layoutParams = FrameLayout.LayoutParams(iconSize, iconSize).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dpToPx(8)
                rightMargin = dpToPx(8)
            }
            setOnClickListener {
                startActivity(Intent(this@MainActivity, InfoActivity::class.java))
            }
        }

        // Result and Warning Views
        val textViewResult = TextView(this).apply {
            textSize = 24f
            text = "A(t) = 0.00%"
            setTextColor(Color.BLACK)
        }

        val textViewWarning = TextView(this).apply {
            textSize = 14f
            text = "Warning: Excess sugar will remain unfermented."
            setTextColor(Color.parseColor("#FFA500"))
            visibility = android.view.View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(16) }
        }

        // Input Containers
        val archContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val editVolume = createLabeledInput("Total Volume (V) in Liters", "1.0")
        val editSugar = createLabeledInput("Sugar Mass (G) in Grams", "200")
        val editTemp = createLabeledInput("Temperature (T) in Celsius", "20")
        archContainer.addView(editVolume.first); archContainer.addView(editVolume.second)
        archContainer.addView(editSugar.first); archContainer.addView(editSugar.second)
        archContainer.addView(editTemp.first); archContainer.addView(editTemp.second)

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

        val editTime = createLabeledInput("Time (t) in Days", "5")
        
        // Default Values
        editVolume.second.setText("1.0")
        editSugar.second.setText("200")
        editTemp.second.setText("20")
        editTime.second.setText("5")

        // Functions
        fun performCalculation() {
            val volume = editVolume.second.text.toString().toDoubleOrNull() ?: 1.0
            val sugar = editSugar.second.text.toString().toDoubleOrNull() ?: 0.0
            val time = editTime.second.text.toString().toDoubleOrNull() ?: 0.0
            
            val abvMaxInput = editAbvMax.second.text.toString().toDoubleOrNull() ?: 0.0
            val kInput = editGrowthK.second.text.toString().toDoubleOrNull() ?: 0.0
            val t0Input = editLagT0.second.text.toString().toDoubleOrNull() ?: 0.0

            val abvRaw = FermentationCalculator.calculatePotentialAbv(volume, sugar)
            val result = FermentationCalculator.calculateAt(abvMaxInput, kInput, time, t0Input)
            
            textViewResult.text = "A(t) = %.2f%%".format(result)

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

            val abvRaw = FermentationCalculator.calculatePotentialAbv(volume, sugar)
            val abvMax = if (abvRaw > profile.maxTolerance) profile.maxTolerance else abvRaw
            val kAdj = FermentationCalculator.adjustGrowthRate(profile.kBase, temp)

            editAbvMax.second.setText("%.2f".format(abvMax))
            editGrowthK.second.setText("%.4f".format(kAdj))
            editLagT0.second.setText("${profile.t0}")

            performCalculation()
        }

        // Buttons
        val btnCalculate = Button(this).apply {
            text = "Calculate"
            setOnClickListener { performCalculation() }
        }

        val presetRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val buttonParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
            addView(Button(this@MainActivity).apply {
                text = "Pikahiiva"
                layoutParams = buttonParams
                setOnClickListener { updateFromYeast(YeastProfiles.pikahiiva) }
            })
            addView(Button(this@MainActivity).apply {
                text = "Bread Yeast"
                layoutParams = buttonParams
                setOnClickListener { updateFromYeast(YeastProfiles.breadYeast) }
            })
            addView(Button(this@MainActivity).apply {
                text = "Wine Yeast"
                layoutParams = buttonParams
                setOnClickListener { updateFromYeast(YeastProfiles.wineYeast) }
            })
        }

        val btnToggleAdvanced = Button(this).apply {
            text = "I Understand What I'm Doing"
            setOnClickListener {
                if (logicContainer.visibility == android.view.View.GONE) {
                    logicContainer.visibility = android.view.View.VISIBLE
                    archContainer.visibility = android.view.View.GONE
                    text = "Nevermind"
                } else {
                    logicContainer.visibility = android.view.View.GONE
                    archContainer.visibility = android.view.View.VISIBLE
                    text = "I Understand What I'm Doing"
                }
            }
        }

        // Gemini Section
        val editLocation = createLabeledInput("Enter your location", "City / Area name")
        val btnEstimateCost = Button(this).apply { text = "Estimate Cost" }
        val textViewCostResult = TextView(this).apply { textSize = 16f }

        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
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
                Toast.makeText(this@MainActivity, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }

        // Build View Hierarchy
        mainContentLayout.addView(textViewResult)
        mainContentLayout.addView(textViewWarning)
        mainContentLayout.addView(archContainer)
        mainContentLayout.addView(logicContainer)
        mainContentLayout.addView(editTime.first); mainContentLayout.addView(editTime.second)
        mainContentLayout.addView(btnCalculate)
        mainContentLayout.addView(presetRow)
        mainContentLayout.addView(btnToggleAdvanced)
        mainContentLayout.addView(android.view.View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dpToPx(24)) })
        mainContentLayout.addView(editLocation.first); mainContentLayout.addView(editLocation.second)
        mainContentLayout.addView(btnEstimateCost)
        mainContentLayout.addView(textViewCostResult)

        rootFrame.addView(scrollView)
        rootFrame.addView(infoButton)
        setContentView(rootFrame)
    }

    private fun createLabeledInput(labelText: String, hintText: String): Pair<TextView, EditText> {
        val label = TextView(this).apply {
            text = labelText
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) }
        }
        val input = EditText(this).apply {
            hint = hintText
            inputType = InputType.TYPE_CLASS_TEXT
        }
        return Pair(label, input)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
