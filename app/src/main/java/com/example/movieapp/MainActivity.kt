package com.example.movieapp

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.movieapp.Classes.Dialog.D_lastImages
import com.example.movieapp.Services.JsonTask
import com.example.movieapp.Services.Lib
import com.example.movieapp.Utils.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var textOutput : TextView
    private lateinit var imageOutput : ImageView
    private lateinit var button : Button
    private val baseUrl = "http://10.30.243.232:8000/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textOutput = findViewById(R.id.output)
        imageOutput = findViewById(R.id.image)
        button = findViewById(R.id.button)

        getJsonResponseObj(baseUrl)

        //val postUrl = ""


        button.setOnClickListener {

            //postJason(baseUrl)
            getLastImages()
        }

    }

    private fun getJsonResponseObj(url : String) {
        JsonTask.getJsonArray(this, url){ success, res->
            if(success){
                Handler(this.mainLooper).post{
                    println("--> $res")
                    textOutput.text = res.toString()
                }
            }
        }

        //return JSONObject(textRes)
    }

    private fun postJason(url : String){
        val data = JSONObject()

        data.put("productName", "product")
        data.put("isSold", "true")
        data.put("price", 335)
        /*JsonTask.postProperty(this, url, data){
            if (it){
                println("--> That Send Successfully")
            }
        }*/
    }



    ///==============================================
    ///==============================================
    ///==============================================
    ///==============================================


    private fun getLastImages(){
        if(Lib.isStoragePermissionGranted(this, this)){
            var i = 0
            val res = ArrayList<Uri>()
            val imageColumns = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
            val imageOrderBy = MediaStore.Images.Media._ID + " DESC"
            val imageCursor: Cursor? = this.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy)
            val col = MediaStore.Images.Media._ID
            if(imageCursor != null && imageCursor.moveToFirst()){
                do {
                    val ind = imageCursor.getColumnIndex(col)
                    val fullPath = imageCursor.getLong(ind)
                    val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fullPath)
                    res.add(contentUri)
                    i++
                } while (imageCursor.moveToNext() && i < 200)
            }
            imageCursor?.close()
            var d : D_lastImages? = null
            d = D_lastImages(this, res){
                launchImageCrop(it)
                d?.dismiss()
            }
            d.buildAndDisplay()
        }
    }

    private fun launchImageCrop(uri: Uri){
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        launchImageCrop(it)
                    }
                } else {
                    println(">>>couldn't select image from the gallery")
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        saveImageToDataBase(it)
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    println(">>>CropError : ${result.error}")
                }
            }
        }
    }

    private fun saveImageToDataBase(uri: Uri){
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss.SSS", Locale.US).format(Date())
        val name = "Image_$timeStamp.jpeg"

        val bitmap = BitmapFactory.decodeFile(uri.encodedPath)
        imageOutput.setImageBitmap(bitmap)

        postJsonWithFile(baseUrl, getBytesFromBitmap(bitmap))
    }

    private fun postJsonWithFile(url : String, image : ByteArray?){
        val data = JSONObject()

        data.put("productName", "product")
        data.put("isSold", "true")
        data.put("price", 335)
        JsonTask.postJsonObjectFile(this, url, data, image){success, res ->
            if (success){
                println("--> Data Send Successfully $res")
            }
        }
    }

    private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        val dataSize = bitmap.rowBytes * bitmap.height
        when {
            dataSize > HUGE_IMAGE -> {
                bitmap.compress(Bitmap.CompressFormat.JPEG, HUGE_COMPRESS, stream)
            }
            dataSize > BIG_IMAGE -> {
                bitmap.compress(Bitmap.CompressFormat.JPEG, HEIGHT_COMPRESS, stream)
            }
            else -> {
                bitmap.compress(Bitmap.CompressFormat.JPEG, LOW_COMPRESS, stream)
            }
        }
        return stream.toByteArray()
    }



}