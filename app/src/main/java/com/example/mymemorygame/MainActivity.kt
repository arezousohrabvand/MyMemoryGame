package com.example.mymemorygame

import android.animation.ArgbEvaluator
import android.app.Activity
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
import com.example.mymemorygame.models.UserImgList
import com.example.mymemorygame.utils.EXTRA_CARD_SIZE
import com.example.mymemorygame.utils.EXTRA_GAME_NAME
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    companion object{
        private  const val TAG="MainActivity"
        private  const val CREATE_REQUEST_CODE=705

    }
    private  var customizeGameImg:List<String>?=null
    private val database= Firebase.firestore
    private  var  gameName:String? = null

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

        // pass into the adapter image drawables which make the game
        val  imgChosen=DEFAULT_IMAGES.shuffled().take(cardSize.getPairsNum())
        val imgRandomized=(imgChosen+imgChosen).shuffled()
        //create list of memory cards
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
            //setup a game again with refresh menu item
            R.id.ref_menu ->{
                if(gameMemory.getNumMoves()>0 && !gameMemory.winGame()){
                    //show the alert dialog because its close to wining but user hit the refresh btn
                    showAlertWarningDialog("Do you want to quit?",null,View.OnClickListener { setupCardBoard() })

                }
                else{
                    setupCardBoard()
                }
                return true
            }
            //menu item for choosing the game size
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode== CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val customGName=data?.getStringExtra(EXTRA_GAME_NAME)
            if(customGName == null){
                Log.e(TAG,"Error from Create Activity ")
                return
            }
            dlGame(customGName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun dlGame(customizeGameName: String) {
        database.collection("memorygame").document(customizeGameName).get().addOnSuccessListener {document ->
            val userImgList=document.toObject(UserImgList::class.java)
            if (userImgList?.images == null){
                Log.e(TAG,"Error from Firestore because your data is invalid")
                Snackbar.make(recyclerViewBoard,"Sorry , your game is not found '$customizeGameName'",Snackbar.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }
            val cardNumbers=userImgList .images.size * 2
            cardSize=CardSize.getByValue(cardNumbers)
            customizeGameImg=userImgList.images
            setupCardBoard()
            gameName=customizeGameName



        }.addOnFailureListener{exception ->
            Log.e(TAG,"Exception Error",exception)

        }

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
//method for  size_menu  item and showing the radio btn which one to choose
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
    //method for setup The game and when you use refresh menu item it set up again with method

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
                numMoves.text="Hard: 6*4"
                numPairs.text="Pairs:0/12"
            }
        }
        numPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_stop))
        //construct GameMemory
        gameMemory=GameMemory(cardSize,customizeGameImg)
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
//method for showing alert and warning

    private fun showAlertWarningDialog(title:String, view: View?,positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Ok"){ _, _ ->
            positiveClickListener.onClick(null)
            }.show()

    }

//method for updating memory game with flip attempted flip at his position
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