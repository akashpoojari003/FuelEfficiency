package com.example.fuelefficiency

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class MainActivity : AppCompatActivity() {

    var mean = floatArrayOf(
        5.47771f,
        195.318f,
        104.869f,
        2990.25f,
        15.5592f,
        75.8981f,
        0.624204f,
        0.178344f,
        0.197452f
    )
    var std = floatArrayOf(
        1.69979f,
        104.332f,
        38.0962f,
        843.899f,
        2.78923f,
        3.67564f,
        0.485101f,
        0.383413f,
        0.398712f
    )
    var interpreter: Interpreter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            interpreter = Interpreter(loadModelFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val cylinders = findViewById<EditText>(R.id.cylinders)
        val displacement = findViewById<EditText>(R.id.displacement)
        val horsePower = findViewById<EditText>(R.id.horsePower)
        val weight = findViewById<EditText>(R.id.weight)
        val acceleration = findViewById<EditText>(R.id.acceleration)
        val modelYear = findViewById<EditText>(R.id.modelYear)
        val origin = findViewById<Spinner>(R.id.spinner)
        val arrayAdapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("USA", "Europe", "Japan")
        )
        origin.adapter = arrayAdapter
        val result = findViewById<TextView>(R.id.textView)
        val btn = findViewById<Button>(R.id.button)
        btn.setOnClickListener(View.OnClickListener {
            val floats = Array(1) { FloatArray(9) }
            floats[0][0] = (cylinders.text.toString().toFloat() - mean[0]) / std[0]
            floats[0][1] = (displacement.text.toString().toFloat() - mean[1]) / std[1]
            floats[0][2] = (horsePower.text.toString().toFloat() - mean[2]) / std[2]
            floats[0][3] = (weight.text.toString().toFloat() - mean[3]) / std[3]
            floats[0][4] = (acceleration.text.toString().toFloat() - mean[4]) / std[4]
            floats[0][5] = (modelYear.text.toString().toFloat() - mean[5]) / std[5]
            when (origin.selectedItemPosition) {
                0 -> {
                    floats[0][6] = (1 - mean[6] / std[6])
                    floats[0][7] = (0 - mean[7] / std[7])
                    floats[0][8] = (0 - mean[8] / std[8])
                }
                1 -> {
                    floats[0][6] = (0 - mean[6] / std[6])
                    floats[0][7] = (1 - mean[7] / std[7])
                    floats[0][8] = (0 - mean[8] / std[8])
                }
                2 -> {
                    floats[0][6] = (0 - mean[6] / std[6])
                    floats[0][7] = (0 - mean[7] / std[7])
                    floats[0][8] = (1 - mean[8] / std[8])
                }
            }
            val res = doInference(floats)
            result.text = "Result :$res"
        })
    }

    fun doInference(input: Array<FloatArray>?): Float {
        val output = Array(1) { FloatArray(1) }
        interpreter!!.run(input, output)
        return output[0][0]
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = this.assets.openFd("automobile.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val length = assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }
}