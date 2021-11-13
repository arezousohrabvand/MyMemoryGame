package com.example.mymemorygame.utils

import android.graphics.Bitmap

object BitmapScaler{
    fun scaleFitWidth(b:Bitmap,width:Int):Bitmap{
        val factor=width/b.width.toFloat()
        return Bitmap.createScaledBitmap(b,width,(b.height * factor).toInt(),true)
    }
    fun scaleFitHeight(b:Bitmap,height:Int):Bitmap{
        val factor=height/b.height.toFloat()
        return  Bitmap.createScaledBitmap(b,(b.width * factor).toInt(),height,true)
    }
}
