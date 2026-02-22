package com.example.milestokm

import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root layout
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val padding = dpToPx(16)
            setPadding(padding, padding, padding, padding)
        }

        // TextView for Result
        val textViewResult = TextView(this).apply {
            id = android.view.View.generateViewId()
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
        }

        // EditText for Input
        val editTextInput = EditText(this).apply {
            id = android.view.View.generateViewId()
            hint = "Enter your distance here:"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            val padding = dpToPx(10)
            setPadding(padding, padding, padding, padding)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Button: Miles to KM
        val buttonMTK = Button(this).apply {
            id = android.view.View.generateViewId()
            text = "Miles to KM"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
        }

        // Button: KM to Miles
        val buttonKTM = Button(this).apply {
            id = android.view.View.generateViewId()
            text = "KM to Miles"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
        }

        // Button: Poronkusema to KM
        val buttonPKTK = Button(this).apply {
            id = android.view.View.generateViewId()
            text = "Poronkusema to KM"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
        }

        // Add views to layout
        rootLayout.addView(textViewResult)
        rootLayout.addView(editTextInput)
        rootLayout.addView(buttonMTK)
        rootLayout.addView(buttonKTM)
        rootLayout.addView(buttonPKTK)

        setContentView(rootLayout)

        // Conversion Logic
        buttonMTK.setOnClickListener {
            val input = editTextInput.text.toString().toDoubleOrNull()
            if (input != null) {
                // "convert to km multiplies by 1.6"
                val result = input * 1.6
                textViewResult.text = "$result"
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

        buttonKTM.setOnClickListener {
            val input = editTextInput.text.toString().toDoubleOrNull()
            if (input != null) {
                // "convert to KM multiplies the number by 0.6" (assuming KM to Miles)
                val result = input * 0.6
                textViewResult.text = "$result"
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

        buttonPKTK.setOnClickListener {
            val input = editTextInput.text.toString().toDoubleOrNull()
            if (input != null) {
                // "porokusema divides by 8.5"
                val result = input / 8.5
                textViewResult.text = "$result"
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
