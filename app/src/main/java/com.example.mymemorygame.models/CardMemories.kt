package com.example.mymemorygame.models

import org.intellij.lang.annotations.Identifier

data class CardMemories (
    val identifier: Int,
    var isCardFaceUp:Boolean=false,
    var isMatches:Boolean=false
        )

