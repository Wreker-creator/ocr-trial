package com.wreker.ocr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.SparseArray
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.wreker.ocr.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val cropActivityResultContracts = object :
        ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(this@MainActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {

            return CropImage.getActivityResult(intent)?.uri

        }

    }

    private var uri : Uri ?=null

    private lateinit var cropActivityResultLauncher : ActivityResultLauncher<Any?>

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts){
            it?.let {
                uri = it
            }
        }


        binding.btnCapture.setOnClickListener {

            cropActivityResultLauncher.launch(null)

        }

        binding.btnReadText.setOnClickListener {
            val bitmap = uri?.let { it1 -> getBitmap(it1) }
            binding.image.setImageBitmap(bitmap)
            bitmap?.let {
                getTextFromImage(it)
            }
        }

    }

    private fun getBitmap(uri : Uri) : Bitmap{
        val source = ImageDecoder.createSource(this.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source)
    }

    private fun getTextFromImage(bitmap: Bitmap){
        val textRecognizer = com.google.android.gms.vision.text.TextRecognizer.Builder(this).build()

        if(!textRecognizer.isOperational){
            this.toast("Error")
        }else{
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val textBlockSparseArray : SparseArray<TextBlock> = textRecognizer.detect(frame)
            val stringBuilder = StringBuilder()

            for(i in 0 until textBlockSparseArray.size()){
                val textBlock = textBlockSparseArray.valueAt(i)
                stringBuilder.append(textBlock.value)
                stringBuilder.append("\n")
            }

            binding.ttvTextFromImage.text = stringBuilder.toString()

        }

    }

    companion object {
        private const val CAMERA_CODE = 100
    }

}