package com.example.mymemorygame.models

enum class CardSize (val cardNumbers:Int){
    EASY(8),
    MEDIUM(18),
    HARD(24);
    fun getWidth():Int{
        return when(this){
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }
    fun getHeight():Int{
        return  cardNumbers/getWidth()
    }
    fun getPairsNum():Int{
        return  cardNumbers/2
    }
}