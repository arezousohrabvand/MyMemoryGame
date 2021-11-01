package com.example.mymemorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemorygame.models.CardSize

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewBoard:RecyclerView
    private lateinit var numMoves:TextView
    private lateinit var numPairs:TextView
    private  var cardSize: CardSize =CardSize.HARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerViewBoard=findViewById(R.id.recyclerViewBoard)
        numMoves=findViewById(R.id.numMoves)
        numPairs=findViewById(R.id.numPairs)

        //setup recycler view
        //adapter binding dataset to the view of recyclerview
        recyclerViewBoard.adapter=MemoryCardAdapter(this,cardSize)
        //layout manager position item views
        recyclerViewBoard.layoutManager= GridLayoutManager(this,cardSize.getWidth())
        recyclerViewBoard.setHasFixedSize(true)

    }
}