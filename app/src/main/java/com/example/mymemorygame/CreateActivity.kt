package com.example.mymemorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.mymemorygame.models.CardSize
import com.example.mymemorygame.utils.EXTRA_CARD_SIZE

class CreateActivity : AppCompatActivity() {
    private  lateinit var  cardSize: CardSize
    private  var imgNumRequired=-1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        cardSize=intent.getSerializableExtra(EXTRA_CARD_SIZE) as CardSize
        imgNumRequired=cardSize.getPairsNum()
        supportActionBar?.title="Choose pictures(0/ $imgNumRequired)"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            //finish this activity and go back
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}