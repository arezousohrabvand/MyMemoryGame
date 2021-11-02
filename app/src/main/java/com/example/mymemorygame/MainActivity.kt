package com.example.mymemorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemorygame.models.CardMemories
import com.example.mymemorygame.models.CardSize
import com.example.mymemorygame.models.GameMemory
import com.example.mymemorygame.utils.DEFAULT_IMAGES

class MainActivity : AppCompatActivity() {
    companion object{
        private  const val TAG="MainActivity"
    }

    private lateinit var recyclerViewBoard:RecyclerView
    private lateinit var numMoves:TextView
    private lateinit var numPairs:TextView
    private  var cardSize: CardSize =CardSize.EASY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerViewBoard=findViewById(R.id.recyclerViewBoard)
        numMoves=findViewById(R.id.numMoves)
        numPairs=findViewById(R.id.numPairs)



        // pass into the adapter image drawables
        val  imgChosen=DEFAULT_IMAGES.shuffled().take(cardSize.getPairsNum())
        val imgRandomized=(imgChosen+imgChosen).shuffled()
        val cardMemories=imgRandomized.map{CardMemories(it) }
        //construct GameMemory
        val gameMemory=GameMemory(cardSize)
        //setup recycler view
        //adapter binding dataset to the view of recyclerview
        recyclerViewBoard.adapter=MemoryCardAdapter(this,cardSize,gameMemory.cards,object :MemoryCardAdapter.CardClickListener{
            override fun cardClicked(position: Int) {
                Log.i(TAG,"Clicked on cards $position")
            }

        })
        //layout manager position item views
        recyclerViewBoard.layoutManager= GridLayoutManager(this,cardSize.getWidth())
        recyclerViewBoard.setHasFixedSize(true)

    }
}