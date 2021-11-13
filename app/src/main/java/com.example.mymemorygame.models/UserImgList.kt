package com.example.mymemorygame.models
import  com.google.firebase.firestore.PropertyName
data class UserImgList (@PropertyName("images") val images:List<String>? = null)



