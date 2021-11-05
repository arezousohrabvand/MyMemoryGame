package com.example.mymemorygame

import android.animation.ArgbEvaluator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemorygame.models.CardMemories
import com.example.mymemorygame.models.CardSize
import com.example.mymemorygame.models.GameMemory
import com.example.mymemorygame.utils.DEFAULT_IMAGES
import com.google.android.material.snackbar.Snackbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.example.mymemorygame.utils.EXTRA_CARD_SIZE

class MainActivity : AppCompatActivity() {
    companion object{
        private  const val TAG="MainActivity"
        private  const val CREATE_REQUEST_CODE=705

    }

    private  lateinit var constraintLayoutRoot:ConstraintLayout
    private lateinit var recyclerViewBoard:RecyclerView
    private lateinit var numMoves:TextView
    private lateinit var numPairs:TextView
    private lateinit var gameMemory: GameMemory
    private lateinit var adapter: MemoryCardAdapter
    private  var cardSize: CardSize =CardSize.EASY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        constraintLayoutRoot=findViewById(R.id.constraintLayoutRoot)
        recyclerViewBoard=findViewById(R.id.recyclerViewBoard)
        numMoves=findViewById(R.id.numMoves)
        numPairs=findViewById(R.id.numPairs)

        // pass into the adapter image drawables
        val  imgChosen=DEFAULT_IMAGES.shuffled().take(cardSize.getPairsNum())
        val imgRandomized=(imgChosen+imgChosen).shuffled()
        val cardMemories=imgRandomized.map{CardMemories(it) }

       setupCardBoard()

    }



    //method for creating menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }
//method for selecting options
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){

            R.id.ref_menu ->{
                if(gameMemory.getNumMoves()>0 && !gameMemory.winGame()){
                    showAlertWarningDialog("Do you want to quit?",null,View.OnClickListener { setupCardBoard() })

                }
                else{
                    setupCardBoard()
                }
                return true
            }
            R.id.size_menu ->{
                showNewGameDialog()
                return true
            }
            //register and define custom game for menu item for choosing custom game and action happen when user tap on custom game
            R.id.cutom_menu ->{
                //function of showCustomDialog
                showCustomDialog()
                return true
            }
        }
    return super.onOptionsItemSelected(item)
    }
//this function is similar to showNewGameDialog it is used for what size of game that we want to create

    private fun showCustomDialog() {
        val cardSizeView=LayoutInflater.from(this).inflate(R.layout.dialog_card_size,null)
        val radioGroupSize= cardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertWarningDialog("Make your own memory card",cardSizeView,View.OnClickListener {
            val wantedCardNum=when (radioGroupSize.checkedRadioButtonId){
                R.id.radioBtn1->CardSize.EASY
                R.id.radioBtn2->CardSize.MEDIUM
                else ->CardSize.HARD
            }
            
            //navigate user to the new screen so instead of setupCardBoard here it goes to custom activity
            val intent=Intent(this,CreateActivity::class.java)
            intent.putExtra(EXTRA_CARD_SIZE,wantedCardNum)
        //method for navigate to CustomActivity
            startActivityForResult(intent,CREATE_REQUEST_CODE)


           
        })
    }

    private fun showNewGameDialog() {

        val cardSizeView=LayoutInflater.from(this).inflate(R.layout.dialog_card_size,null)
        val radioGroupSize= cardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (cardSize){
            CardSize.EASY -> radioGroupSize.check((R.id.radioBtn1))
            CardSize.MEDIUM -> radioGroupSize.check((R.id.radioBtn2))
            CardSize.HARD -> radioGroupSize.check((R.id.radioBtn3))
        }
        showAlertWarningDialog("Choose your game",cardSizeView,View.OnClickListener {
            cardSize=when (radioGroupSize.checkedRadioButtonId){
                    R.id.radioBtn1->CardSize.EASY
                    R.id.radioBtn2->CardSize.MEDIUM
                else ->CardSize.HARD
            }
            setupCardBoard()

        })
    }

    private fun setupCardBoard() {
        when(cardSize){
            CardSize.EASY -> {
                numMoves.text="Easy: 4*2"
                numPairs.text="Pairs:0/4"
            }
            CardSize.MEDIUM -> {
                numMoves.text="Medium: 6*3"
                numPairs.text="Pairs:0/9"
            }
            CardSize.HARD -> {
                numMoves.text="Hard: 6*6"
                numPairs.text="Pairs:0/12"
            }
        }
        numPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_stop))
        //construct GameMemory
        gameMemory=GameMemory(cardSize)
        //setup recycler view
        //adapter binding dataset to the view of recyclerview
        adapter=MemoryCardAdapter(this,cardSize,gameMemory.cards,object :MemoryCardAdapter.CardClickListener{
            override fun cardClicked(position: Int) {
                updateCardFlip(position)
            }

        })
        recyclerViewBoard.adapter=adapter
        //layout manager position item views
        recyclerViewBoard.layoutManager= GridLayoutManager(this,cardSize.getWidth())
        recyclerViewBoard.setHasFixedSize(true)
    }


    private fun showAlertWarningDialog(title:String, view: View?,positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Ok"){ _, _ ->
            positiveClickListener.onClick(null)
            }.show()

    }


    private fun updateCardFlip(position: Int) {
        if(gameMemory.winGame()){
            Snackbar.make(constraintLayoutRoot,"You are won",Snackbar.LENGTH_LONG).show()
            return
        }
        if(gameMemory.isFaceUpCard(position)){
            Snackbar.make(constraintLayoutRoot,"Invalid Move",Snackbar.LENGTH_LONG).show()
            return
        }
        //flip the card over
        if( gameMemory.flipCard(position)){
            Log.i(TAG,"Match found.. Pairs numbers found: ${gameMemory.pairsNumFound}")
            val color=ArgbEvaluator().evaluate(
                gameMemory.pairsNumFound.toFloat()/cardSize.getPairsNum(),
                ContextCompat.getColor(this,R.color.color_progress_stop),
                ContextCompat.getColor(this,R.color.color_progress_pass)
            ) as Int
            numPairs.setTextColor(color)
            numPairs.text="Pairs: ${gameMemory.pairsNumFound}/${cardSize.getPairsNum()}"
            if(gameMemory.winGame()){
                Snackbar.make(constraintLayoutRoot,"Congrats! You won",Snackbar.LENGTH_LONG).show()
            }
        }
        numMoves.text="Moves: ${gameMemory.getNumMoves()}"

        adapter.notifyDataSetChanged()

    }
}