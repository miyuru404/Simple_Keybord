package com.simple_keybord

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.graphics.Color

class MyKeyboardService : InputMethodService() {

    private var isCapital = false
    private var isNumber = false
    private lateinit var keyboardLayout: LinearLayout
    private val letterRows = mutableListOf<LinearLayout>()

    // Define simple (lowercase) and capital letters
    private val simpleRow1 = listOf("q","w","e","r","t","y","u","i","o","p")
    private val simpleRow2 = listOf("a","s","d","f","g","h","j","k","l")
    private val simpleRow3 = listOf("^","z","x","c","v","b","n","m","Esc")

    private val capitalRow1 = listOf("Q","W","E","R","T","Y","U","I","O","P")
    private val capitalRow2 = listOf("A","S","D","F","G","H","J","K","L")
    private val capitalRow3 = listOf("^","Z","X","C","V","B","N","M","Esc")

    override fun onCreateInputView(): View {
        keyboardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        showLetterKeys()
        return keyboardLayout
    }

    private fun showLetterKeys() {
        keyboardLayout.removeAllViews()
        isNumber = false
        letterRows.clear()

        val row1Keys = if (isCapital) capitalRow1 else simpleRow1
        val row2Keys = if (isCapital) capitalRow2 else simpleRow2
        val row3Keys = if (isCapital) capitalRow3 else simpleRow3

        letterRows.add(createRow(row1Keys))
        letterRows.add(createRow(row2Keys))
        letterRows.add(createRow(row3Keys))

        letterRows.forEach { keyboardLayout.addView(it) }

        // Bottom row: 123 toggle, Space, Enter
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val numberToggle = Button(this).apply {
            text = "123"
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { showNumberKeys() }
        }

        val spaceButton = Button(this).apply {
            text = "Space"
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4f)
            setOnClickListener { currentInputConnection.commitText(" ", 1) }
        }

        val enterButton = Button(this).apply {
            text = "Enter"
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                currentInputConnection.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                )
            }
        }

        bottomRow.addView(numberToggle)
        bottomRow.addView(spaceButton)
        bottomRow.addView(enterButton)
        keyboardLayout.addView(bottomRow)
    }

    private fun showNumberKeys() {
        keyboardLayout.removeAllViews()
        isNumber = true
        letterRows.clear()

        val row1Keys = listOf("1","2","3","4","5","6","7","8","9","0")
        val row2Keys = listOf("-","/",";",":","(",")","$","&","@","\"")
        val row3Keys = listOf("^",".",",","?","!","#","%","*","Esc")

        keyboardLayout.addView(createRow(row1Keys))
        keyboardLayout.addView(createRow(row2Keys))
        keyboardLayout.addView(createRow(row3Keys))

        // Bottom row: ABC toggle, Space, Enter
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val letterToggle = Button(this).apply {
            text = "ABC"
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { showLetterKeys() }
        }

        val spaceButton = Button(this).apply {
            text = "Space"
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4f)
            setOnClickListener { currentInputConnection.commitText(" ", 1) }
        }

        val enterButton = Button(this).apply {
            text = "Enter"
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                currentInputConnection.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                )
            }
        }

        bottomRow.addView(letterToggle)
        bottomRow.addView(spaceButton)
        bottomRow.addView(enterButton)
        keyboardLayout.addView(bottomRow)
    }

    private fun createRow(keys: List<String>): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (key in keys) {
            val button = Button(this).apply {
                text = key
                setBackgroundColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { handleButtonClick(key) }
            }
            row.addView(button)
        }

        return row
    }

    private fun handleButtonClick(key: String) {
        when (key) {
            "^" -> {
                isCapital = !isCapital
                updateLetterButtons() // updates button text
            }
            "Esc" -> currentInputConnection.deleteSurroundingText(1, 0)
            else -> {
                val charToCommit = if (isCapital && !isNumber && key.length == 1) key.uppercase() else key
                currentInputConnection.commitText(charToCommit, 1)
            }
        }
    }


    private fun updateLetterButtons() {
        val row1Keys = if (isCapital) capitalRow1 else simpleRow1
        val row2Keys = if (isCapital) capitalRow2 else simpleRow2
        val row3Keys = if (isCapital) capitalRow3 else simpleRow3

        val allRows = listOf(row1Keys, row2Keys, row3Keys)

        for (i in letterRows.indices) {
            val row = letterRows[i]
            val keys = allRows[i]
            for (j in 0 until row.childCount) {
                val btn = row.getChildAt(j) as Button
                btn.text = keys[j]
            }
        }
    }
}
