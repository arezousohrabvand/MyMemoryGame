package com.example.mymemorygame.models

import org.intellij.lang.annotations.Identifier
//list of any attribute in memory card
data class CardMemories (
    val identifier: Int,
    val imageUrl:String? = null,
    var isCardFaceUp:Boolean=false,//every card at default is face down so card face up is false
    var isMatches:Boolean=false
        )

