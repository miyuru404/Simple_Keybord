package com.simple_keybord

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class MyKeyboardService : InputMethodService() {

    private var isCapital = false
    private var isNumber = false
    private lateinit var keyboardLayout: LinearLayout
    private val letterRows = mutableListOf<LinearLayout>()

    // ====== CONFIGURABLE VARIABLES ======
    companion object {
        // Keyboard background (default: light gray)
        var keyboardBackgroundColor: Int = Color.BLACK
        var keyboardBackgroundImage: Drawable? = null // can be set later from settings

        // Keys styling
        var keyBackgroundColor: Int = Color.BLACK
        var keyTextColor: Int = Color.WHITE

        // Outline configs
        var keyBorderColor: Int = Color.WHITE
        var keyBorderWidthDp: Int = 2
        var keyCornerRadiusDp: Float = 8f

        // Size configs
        var keyTextSizeSp: Float = 22f   // bigger text
        var keyHeightDp: Int = 60        // button height
        var keyboardPaddingDp: Int = 5   // space around keyboard
    }

    // Simple (lowercase) and Capital letters
    private val simpleRow1 = listOf("q","w","e","r","t","y","u","i","o","p")
    private val simpleRow2 = listOf("a","s","d","f","g","h","j","k","l")
    private val simpleRow3 = listOf("^","z","x","c","v","b","n","m","Esc")

    private val capitalRow1 = listOf("Q","W","E","R","T","Y","U","I","O","P")
    private val capitalRow2 = listOf("A","S","D","F","G","H","J","K","L")
    private val capitalRow3 = listOf("^","Z","X","C","V","B","N","M","Esc")

    override fun onCreateInputView(): View {
        keyboardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dpToPx(keyboardPaddingDp),
                dpToPx(keyboardPaddingDp),
                dpToPx(keyboardPaddingDp),
                dpToPx(keyboardPaddingDp)
            )

            // Set background color or image
            background = keyboardBackgroundImage ?: ColorDrawable(keyboardBackgroundColor)
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

        // Bottom row: 123 toggle, Space, Enter, Settings
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(keyHeightDp)
            )
        }

        val numberToggle = createButton("123") { showNumberKeys() }
        val spaceButton = createButton("Space") {
            currentInputConnection.commitText(" ", 1)
        }.apply { layoutParams = LinearLayout.LayoutParams(0, dpToPx(keyHeightDp), 4f) }

        val enterButton = createButton("Enter") {
            currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        }

        val settingsButton = createButton("⚙️") {
            val intent = Intent(this@MyKeyboardService, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        bottomRow.addView(numberToggle)
        bottomRow.addView(spaceButton)
        bottomRow.addView(enterButton)
        bottomRow.addView(settingsButton)
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

        // Bottom row: ABC toggle, Space, Enter, Settings
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(keyHeightDp)
            )
        }

        val letterToggle = createButton("ABC") { showLetterKeys() }
        val spaceButton = createButton("Space") {
            currentInputConnection.commitText(" ", 1)
        }.apply { layoutParams = LinearLayout.LayoutParams(0, dpToPx(keyHeightDp), 4f) }

        val enterButton = createButton("Enter") {
            currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        }

        val settingsButton = createButton("⚙️") {
            val intent = Intent(this@MyKeyboardService, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        bottomRow.addView(letterToggle)
        bottomRow.addView(spaceButton)
        bottomRow.addView(enterButton)
        bottomRow.addView(settingsButton)
        keyboardLayout.addView(bottomRow)
    }

    private fun createRow(keys: List<String>): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(keyHeightDp)
            )
        }

        for (key in keys) {
            val button = createButton(key) { handleButtonClick(key) }
            row.addView(button)
        }

        return row
    }

    private fun createButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(keyTextColor)
            textSize = keyTextSizeSp
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(keyHeightDp), 1f)

            // Set background with border (outline)
            background = GradientDrawable().apply {
                setColor(keyBackgroundColor)
                setStroke(dpToPx(keyBorderWidthDp), keyBorderColor)
                cornerRadius = dpToPxF(keyCornerRadiusDp)
            }

            setOnClickListener { onClick() }
        }
    }

    private fun handleButtonClick(key: String) {
        when (key) {
            "^" -> {
                isCapital = !isCapital
                updateLetterButtons()
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

    // Helpers
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun dpToPxF(dp: Float): Float {
        val density = resources.displayMetrics.density
        return (dp * density)
    }
}
