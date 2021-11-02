package com.example.mymemorygame.models

import com.example.mymemorygame.utils.DEFAULT_IMAGES

class GameMemory (
    private val cardSize: CardSize
        ){
    val cards:List<CardMemories>
    val pairsNumFound=0
    init {
        val  imgChosen= DEFAULT_IMAGES.shuffled().take(cardSize.getPairsNum())
        val imgRandomized=(imgChosen+imgChosen).shuffled()
        cards=imgRandomized.map{CardMemories(it) }
    }

}