package com.example.mymemorygame

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemorygame.models.CardSize
import kotlin.math.min

class ImageChosenAdapter(private val context: Context,
                         private val chosenImgUris: List<Uri>,
                         private val cardSize: CardSize,
                         private  val imgClickListener: ImgClickListener) :
    RecyclerView.Adapter<ImageChosenAdapter.ViewHolder>() {

//define interface
    interface ImgClickListener{
        fun onPlaceholderClicked()
    }
    //method  for creating one view in recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //This connects (aka inflates) the individual ViewHolder (which is link to activity_create2.xml)
        //with RecyclerView
        val view=LayoutInflater.from(context).inflate(R.layout.img_card,parent,false)
        val cardWidth=parent.width/cardSize.getWidth()
        val cardHeight=parent.height/cardSize.getHeight()
        val cardSide= min(cardWidth,cardHeight)
        val layoutParams=view.findViewById<ImageView>(R.id.customImgView).layoutParams
        layoutParams.width=cardSide
        layoutParams.height=cardSide
        return ViewHolder(view)
    }
    //method for taking data which is at this position binding to view holder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position < chosenImgUris.size){
            holder.bind(chosenImgUris[position])
        }
        else{
            holder.bind()
        }
    }
    //method for how many items in recycler view
    override fun getItemCount()=cardSize.getPairsNum()
    inner class  ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        /**
         * This class allow us to access the activity_main.xml objects
         */
        private val customImgView=itemView.findViewById<ImageView>(R.id.customImgView)

        fun bind(uri:Uri) {
            customImgView.setImageURI(uri)
            customImgView.setOnClickListener(null)
        }
        fun bind() {
            customImgView.setOnClickListener {
                //launch intent for user to select photo
            imgClickListener.onPlaceholderClicked()
            }
        }

    }
}
