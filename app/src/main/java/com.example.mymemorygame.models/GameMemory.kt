package com.example.mymemorygame.models

import com.example.mymemorygame.utils.DEFAULT_IMAGES

class GameMemory(
    private val cardSize: CardSize,
    private val customPics: List<String>?
){


    val cards:List<CardMemories>
    var pairsNumFound=0
    private var flipCardsNum=0

    private var indexOfOneSelectedCard:Int? = null

    init {
        if(customPics == null){
            val imgChosen = DEFAULT_IMAGES.shuffled().take(cardSize.getPairsNum())
            val imgRandomized = (imgChosen + imgChosen).shuffled()
            cards = imgRandomized.map { CardMemories(it) }

        }
        else{
            val randomizedPics=(customPics+customPics).shuffled()
            cards=randomizedPics.map{CardMemories(it.hashCode(),it)
            }
        }





    }
    //method for updating memory game with flip attempted flip at his position
    fun flipCard(position: Int):Boolean {
        flipCardsNum++
    val card=cards[position]
        //flipping card has 3 cases
        //no card flip over
        //1 card flip over
        //2 card flip over
        var matchFound=false
        if(indexOfOneSelectedCard == null){
            //it can be 0 or 2 cards flipped over in previous
            restoreCards()
            indexOfOneSelectedCard=position

        }
        else{
             matchFound=matchCheck(indexOfOneSelectedCard!!,position)
            indexOfOneSelectedCard =null
        }


        card.isCardFaceUp=!card.isCardFaceUp
        return matchFound

    }

    private fun matchCheck(position1: Int, position2: Int): Boolean {
        if(cards[position1].identifier !=cards[position2].identifier){
            return false
        }
        cards[position1].isMatches=true
        cards[position2].isMatches=true
        pairsNumFound++
        return  true
    }

    private fun restoreCards() {

        for (card in cards){
            if(!card.isMatches) {
                card.isCardFaceUp = false
            }

        }
    }

    fun winGame(): Boolean {
        return pairsNumFound== cardSize.getPairsNum()

    }

    fun isFaceUpCard(position: Int): Boolean {
      return cards[position].isCardFaceUp

    }

    fun getNumMoves(): Int {
        return flipCardsNum/2
    }

}