package com.example.milestokm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextInput: EditText = findViewById(R.id.editTextInput)
        val buttonMTK: Button = findViewById(R.id.buttonMTK)
        val buttonKTM: Button = findViewById(R.id.buttonKTM)
        val buttonPKTK: Button = findViewById(R.id.buttonPKTK)
        val textViewResult: TextView = findViewById(R.id.textViewResult)

        buttonMTK.setOnClickListener {
            val inputText = editTextInput.text.toString()
            if (inputText.isNotEmpty()) {
                val miles = inputText.toDoubleOrNull() // casting input to double or nullifying if empty
                if (miles != null) {
                    val km = miles * 1.6
                    textViewResult.text = "$miles Miles is $km KM"
                } else { // error handling for null
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else { // error handling for empty input
                Toast.makeText(this, "Please enter a distance", Toast.LENGTH_SHORT).show()
            }
        }

        buttonKTM.setOnClickListener {
            val inputText = editTextInput.text.toString()
            if (inputText.isNotEmpty()) {
                val km = inputText.toDoubleOrNull() // line 25 for reference
                if (km != null) {
                    val miles = km * 0.6
                    textViewResult.text = "$km KM is $miles Miles"
                } else {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a distance", Toast.LENGTH_SHORT).show()
            }
        }

        buttonPKTK.setOnClickListener {
            val inputText = editTextInput.text.toString()
            if (inputText.isNotEmpty()) {
                val pk = inputText.toDoubleOrNull() // line 25 for reference
                if (pk != null) {
                    val km = pk * 8.5 // Reindeer can travel about 7-10km before poronkusema
                    textViewResult.text = "$pk Poronkusema is $km KM"
                } else {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a distance", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
