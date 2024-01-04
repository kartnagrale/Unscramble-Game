/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel containing the app data and methods to process the data
 */
class GameViewModel : ViewModel() {
    init {
        resetGame()
    }
    //game ui state
    //backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState : StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var currentWord : String
    private var usedWords:MutableSet<String> = mutableSetOf()

    var userGuess by mutableStateOf("")
        private set
    private fun pickRandomWordAndShuffle() : String{
        //continue pick until unused word is found
        currentWord = allWords.random()
        return if(usedWords.contains(currentWord)){
            pickRandomWordAndShuffle()
        }else{
            usedWords.add(currentWord)
            shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word:String):String{
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        while(String(tempWord) == word){
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun resetGame(){
        usedWords.clear()
        _uiState.value= GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord:String){
        userGuess = guessedWord
    }

    fun checkUserGuess(){
        if(userGuess.equals(currentWord,ignoreCase = true)){
            val updatedScore= _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        }else{
            _uiState.update { currentState->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore :Int){
        _uiState.update { currentState->
            currentState.copy(
                isGuessedWordWrong = false,
                currentScrambledWord = pickRandomWordAndShuffle(),
                score = updatedScore,
                currentWordCount = currentState.currentWordCount.inc()
            )
        }
    }

    fun skipWord(){
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }
}
