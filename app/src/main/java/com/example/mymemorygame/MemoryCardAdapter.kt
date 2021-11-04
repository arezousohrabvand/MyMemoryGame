package com.example.mymemorygame

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemorygame.models.CardMemories
import com.example.mymemorygame.models.CardSize
import kotlin.math.min

class MemoryCardAdapter(
    private val context: Context,
    private val cardSize: CardSize,
    private val cards: List<CardMemories>,
    private val cardClickListener: CardClickListener
) :
    RecyclerView.Adapter<MemoryCardAdapter.ViewHolder>() {
    companion object{
        private const val margin_size=8
        private const val TAG="MemoryCardAdapter"

    }
    //define interface
    interface  CardClickListener {
        fun cardClicked(position:Int)
        
    }

//implement members
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    //2*4 cards game
    val cardWidth=parent.width/cardSize.getWidth()-(2* margin_size)
    val cardHeight=parent.height/cardSize.getHeight()-(2* margin_size)
    val cardSide=min(cardHeight,cardHeight)
    val view=LayoutInflater.from(context).inflate(R.layout.game_card,parent,false)
    val layoutParams=view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.width=cardSide
    layoutParams.height=cardSide
    layoutParams.setMargins(margin_size, margin_size, margin_size, margin_size)
    return  ViewHolder(view)
    }
//taking data which is at this position binding to view holder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }
//how many items in recycler view
    override fun getItemCount()=cardSize.cardNumbers
    inner  class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private  val imgbtn=itemView.findViewById<ImageButton>(R.id.imgbtn)
        fun bind(position: Int) {
            val cardMemories = cards[position]
            imgbtn.setImageResource( if (cardMemories.isCardFaceUp) cardMemories.identifier else R.drawable.ic_launcher_background)
            imgbtn.alpha=if(cardMemories.isMatches) .4f else 1.0f
            var colorStateList=if(cardMemories.isMatches) ContextCompat.getColorStateList(context,R.color.color_blue) else null
            ViewCompat.setBackgroundTintList(imgbtn,colorStateList)
            imgbtn.setOnClickListener{
                Log.i(TAG,"Clicked on cards $position")
                cardClickListener.cardClicked(position)
            }

            }


    }


}
