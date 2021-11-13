package com.example.mymemorygame

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemorygame.models.CardSize
import com.example.mymemorygame.utils.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {
    companion object{
        private const val TAG="CreateActivity"
        private const val PICK_IMAGE_CODE=416
        private const val READ_EXTERNAL_IMAGE_CODE=123
        private const val  READ_IMAGE_PERMISSION=android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MIN_GAME_LENGTH=3
        private const val MAX_GAME_LENGTH=14

    }
    private lateinit var progressBarProcessing:ProgressBar
    private  lateinit var recyclerViewChosenPic:RecyclerView
    private lateinit var saveBtn:Button
    private lateinit var editGameName:EditText


    private  lateinit var adapter: ImageChosenAdapter
    private  lateinit var  cardSize: CardSize
    private  var imgNumRequired= -1
    private val  chosenImgUris= mutableListOf<Uri>()
    private  val storage=Firebase.storage
    private val database=Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create2)
        recyclerViewChosenPic=findViewById(R.id.recyclerViewChosenPic)
        saveBtn=findViewById(R.id.saveBtn)
        editGameName=findViewById(R.id.editGameName)
        progressBarProcessing=findViewById(R.id.progressBarProcessing)

        //supportActionBar gives us a chance to back page means homepage(back button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        cardSize=intent.getSerializableExtra(EXTRA_CARD_SIZE) as CardSize
        imgNumRequired=cardSize.getPairsNum()
        supportActionBar?.title="Choose pictures(0/ $imgNumRequired)"
        //add clickListener for saveBtn
        saveBtn.setOnClickListener{
            saveDataOnFireBase()
        }

        editGameName.filters= arrayOf(InputFilter.LengthFilter(MAX_GAME_LENGTH))
        editGameName.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                saveBtn.isEnabled=shouldEnableSaveBtn()
            }

        }
        )

        adapter=ImageChosenAdapter(this,chosenImgUris,cardSize,object:ImageChosenAdapter.ImgClickListener{
            //method for user has tapped on the one of the gray squares of image views
            override fun onPlaceholderClicked() {
                if (isPermissionGranted(this@CreateActivity,READ_IMAGE_PERMISSION)){
                launchIntentForPictures()
                }
                else{
                   requestPermission(this@CreateActivity, READ_IMAGE_PERMISSION,
                       READ_EXTERNAL_IMAGE_CODE)
                }

            }

        })
        //recycler view components
        //set adapter
        recyclerViewChosenPic.adapter=adapter
        recyclerViewChosenPic.setHasFixedSize(true)
        //setup layout manager
        recyclerViewChosenPic.layoutManager=GridLayoutManager(this,cardSize.getWidth())

    }

//method for get permission to use images from external storage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray

    ) {
        if (requestCode== READ_EXTERNAL_IMAGE_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                launchIntentForPictures()
            }else{
                Toast.makeText(this,"In order to create game you should  upload your images",Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

//method for going back to home page
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            //finish this activity and go back
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
//method for getting photo from storage and show it in UI
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode != PICK_IMAGE_CODE || resultCode != Activity.RESULT_OK || data == null) {
        Log.w(TAG, "Did not get data back from the launched activity, user likely canceled flow")
        return
    }
    Log.i(TAG, "onActivityResult")
    val selectedUri = data.data
    val clipData = data.clipData
    if (clipData != null) {
        Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
        for (i in 0 until clipData.itemCount) {
            val clipItem = clipData.getItemAt(i)
            if (chosenImgUris.size < imgNumRequired) {
                chosenImgUris.add(clipItem.uri)
            }
        }
    } else if (selectedUri != null) {
        Log.i(TAG, "data: $selectedUri")
        chosenImgUris.add(selectedUri)
    }
    adapter.notifyDataSetChanged()
    supportActionBar?.title = "Choose pics (${chosenImgUris.size} / $imgNumRequired"
    saveBtn.isEnabled=shouldEnableSaveBtn()

}
    //method for saving on firebase  responsible for taking all images and game name save to fire base
    private fun saveDataOnFireBase() {
        saveBtn.isEnabled=false
        val chosenGameName=editGameName.text.toString()
       Log.i(TAG,"We are going to save our data on firebase")
        //check do not have duplicate game name
        database.collection("memorygame").document(chosenGameName).get().addOnSuccessListener {
            document ->
            if(document !=null && document.data !=null){
                AlertDialog.Builder(this)
                .setTitle("Name Chosen").setMessage("A '$chosenGameName' you chose it has already existed.Please choose another name ")
                    .setPositiveButton("OK",null)
                    .show()
                saveBtn.isEnabled=true
            }else{
                handleAllImagesUploading(chosenGameName)
            }
        }.addOnFailureListener{exception ->
            Log.e(TAG,"Error happens while saving game process",exception)
            Toast.makeText(this,"Error happens while saving game process",Toast.LENGTH_SHORT).show()
            saveBtn.isEnabled=true
        }

    }
//method for storing images on firebase storage
    private fun handleAllImagesUploading(gameName: String) {
        progressBarProcessing.visibility=View.VISIBLE
        var didEncounterError=false
        val uploadedImgUrls= mutableListOf<String>()
        for((index,photoUri)in chosenImgUris.withIndex()) {
            val imgByteArray = getImgByteArray(photoUri)

            val filePath = "images/$gameName/${System.currentTimeMillis()}-${index}.jpg"
            val imgRefrence = storage.reference.child(filePath)
            imgRefrence.putBytes(imgByteArray)
                .continueWith { photoUploadTask ->
                    Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                    imgRefrence.downloadUrl
                }.addOnCompleteListener { downloadUrlTask ->
                    if (!downloadUrlTask.isSuccessful) {
                        Log.e(TAG, "Exception with firebase storage", downloadUrlTask.exception)
                        Toast.makeText(this, "failed to upload photo", Toast.LENGTH_SHORT).show()
                        didEncounterError = true
                        return@addOnCompleteListener
                    }
                    if (didEncounterError) {
                        progressBarProcessing.visibility=View.GONE
                        return@addOnCompleteListener
                    }
                    val downloadUrl = downloadUrlTask.result.toString()
                    uploadedImgUrls.add(downloadUrl)
                    progressBarProcessing.progress=uploadedImgUrls.size * 100 /chosenImgUris.size
                    Log.i(
                        TAG,
                        "Finished Uploading $photoUri,number of upload ${uploadedImgUrls.size}"
                    )
                    if (uploadedImgUrls.size == chosenImgUris.size) {
                        handleAllImagesUploaded(gameName, uploadedImgUrls)
                    }
                }

        }

    }

    private fun handleAllImagesUploaded(gameName: String, imgUrls: MutableList<String>) {
        database.collection("memorygame").document(gameName)
            .set(mapOf("images" to imgUrls))
            .addOnCompleteListener { gameCreationTask ->
                progressBarProcessing.visibility=View.GONE
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG, "Exceptiom with game creation ", gameCreationTask.exception)
                    Toast.makeText(this, "Failed game creation", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                Log.i(TAG, "it is successfully created $gameName")
                AlertDialog.Builder(this)
                    .setTitle("Your upload files completed.Lets play '$gameName'")
                    .setPositiveButton("OK") { _, _ ->
                        val resultData = Intent()
                        resultData.putExtra(EXTRA_GAME_NAME, gameName)
                        setResult(Activity.RESULT_OK, resultData)
                        finish()

                    }
                    .show()
            }


    }
//method for uploading image file firebase its take of downgrading quality
    private fun getImgByteArray(photoUri: Uri):ByteArray {
        val imgBitamp=if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source=ImageDecoder.createSource(contentResolver,photoUri)
            ImageDecoder.decodeBitmap(source)
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver,photoUri)
        }
        Log.i(TAG,"Origianl width ${imgBitamp.width} and height ${imgBitamp.height}")
        val scaledBitmap= BitmapScaler.scaleFitHeight(imgBitamp,250)
        Log.i(TAG,"Scaled width ${scaledBitmap.width} and height ${scaledBitmap.height}")
        val byteOutputStream=ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG,60,byteOutputStream)
        return byteOutputStream.toByteArray()

    }

//method for saving btn after uploading images and enter the game name
    private fun shouldEnableSaveBtn(): Boolean {
        if(chosenImgUris.size != imgNumRequired){
            return false
        }
        if (editGameName.text.isBlank()||editGameName.text.length < MIN_GAME_LENGTH){
            return false
        }
        return true

    }

    private fun launchIntentForPictures() {
       val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(Intent.createChooser(intent,"Choose photo"),PICK_IMAGE_CODE)
    }

}