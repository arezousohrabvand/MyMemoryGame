package com.example.mymemorygame.models

/**
 * enum class for different card board game size
 */
enum class CardSize (val cardNumbers:Int){
    EASY(8),
    MEDIUM(18),
    HARD(24);
    companion object{
        fun getByValue(value: Int) = values().first { it.cardNumbers == value }

    }
    //method for  get width of the game
    fun getWidth():Int{
        return when(this){
            EASY -> 2//(4*2)
            MEDIUM -> 3//(6*3)
            HARD -> 4//(6*4)
        }
    }
    //method for get height
    fun getHeight():Int{
        return  cardNumbers/getWidth()
    }
    //method for how many pairs are there
    fun getPairsNum():Int{
        return  cardNumbers/2
    }
}