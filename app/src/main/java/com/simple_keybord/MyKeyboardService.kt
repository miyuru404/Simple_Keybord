package com.simple_keybord

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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

    companion object {
        // Configurable variables
        var keyboardBackgroundColor: Int = 0xFF000000.toInt()
        var keyBackgroundColor: Int = Color.LTGRAY
        var keyTextColor: Int = Color.BLACK
        var keyBorderColor: Int = Color.BLACK
        var keyBorderWidthDp: Int = 2
        var keyCornerRadiusDp: Float = 8f
        var keyTextSizeSp: Float = 22f
        var keyHeightDp: Int = 60
        var keyboardPaddingDp: Int = 5

        val themes = mapOf(
            "Dark" to Triple(0xFF000000.toInt(), 0xFF212121.toInt(), Color.WHITE),
            "Light" to Triple(0xFF000000.toInt(), Color.LTGRAY, Color.BLACK),
            "Dracula" to Triple(0xFF000000.toInt(), Color.DKGRAY, Color.RED),
            "Gray" to Triple(0xFF000000.toInt(), Color.GRAY, Color.BLACK),
            "SkyBlue" to Triple(0xFF000000.toInt(), Color.CYAN, Color.BLACK),
            "Forest" to Triple(0xFF000000.toInt(), Color.GREEN, Color.WHITE),
            "Ocean Blue" to Triple(0xFF000000.toInt(), 0xFF1E90FF.toInt(), Color.WHITE),
            "Fiery Red" to Triple(0xFF000000.toInt(), 0xFFFF4500.toInt(), Color.WHITE),
            "Sunshine Yellow" to Triple(0xFF000000.toInt(), 0xFFFFD700.toInt(), Color.BLACK),
            "Soft Pink" to Triple(0xFF000000.toInt(), 0xFFFFC0CB.toInt(), Color.BLACK),
            "Mystic Purple" to Triple(0xFF000000.toInt(), 0xFF800080.toInt(), Color.WHITE),
            "Deep Blue" to Triple(0xFF000000.toInt(), 0xFF00008B.toInt(), Color.WHITE)
        )

        const val ACTION_CHANGE_THEME = "com.simple_keybord.ACTION_CHANGE_THEME"
        const val EXTRA_THEME_NAME = "theme_name"

        // Icon constants
        const val ICON_SHIFT_UP = "⇧"
        const val ICON_SHIFT_DOWN = "⇩"
        const val ICON_DELETE = "⌫"
        const val ICON_ENTER = "⏎"
        const val ICON_SETTINGS = "⚙️"
    }

    private val themeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CHANGE_THEME) {
                val themeName = intent.getStringExtra(EXTRA_THEME_NAME) ?: return
                val colors = themes[themeName] ?: return
                keyboardBackgroundColor = colors.first
                keyBackgroundColor = colors.second
                keyTextColor = colors.third
                applyTheme()
            }
        }
    }

    // Letter layouts
    private val simpleRow1 = listOf("q","w","e","r","t","y","u","i","o","p")
    private val simpleRow2 = listOf("a","s","d","f","g","h","j","k","l")
    private val simpleRow3 = listOf(ICON_SHIFT_UP,"z","x","c","v","b","n","m",ICON_DELETE)

    private val capitalRow1 = listOf("Q","W","E","R","T","Y","U","I","O","P")
    private val capitalRow2 = listOf("A","S","D","F","G","H","J","K","L")
    private val capitalRow3 = listOf(ICON_SHIFT_DOWN,"Z","X","C","V","B","N","M",ICON_DELETE)

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreateInputView(): View {
        keyboardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dpToPx(keyboardPaddingDp),
                dpToPx(keyboardPaddingDp),
                dpToPx(keyboardPaddingDp),
                dpToPx(keyboardPaddingDp)
            )
            background = ColorDrawable(keyboardBackgroundColor)
        }

        showLetterKeys()

        val filter = IntentFilter(ACTION_CHANGE_THEME)
        registerReceiver(themeReceiver, filter)

        return keyboardLayout
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(themeReceiver)
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

        val bottomRow = createBottomRow()
        keyboardLayout.addView(bottomRow)

        applyTheme()
    }

    private fun showNumberKeys() {
        keyboardLayout.removeAllViews()
        isNumber = true
        letterRows.clear()

        val row1Keys = listOf("1","2","3","4","5","6","7","8","9","0")
        val row2Keys = listOf("-","/",";",":","(",")","$","&","@","\"")
        val row3Keys = listOf("^",".",",","?","!","#","%","*",ICON_DELETE)

        letterRows.add(createRow(row1Keys))
        letterRows.add(createRow(row2Keys))
        letterRows.add(createRow(row3Keys))
        letterRows.forEach { keyboardLayout.addView(it) }

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(keyHeightDp))
        }

        val abcButton = createButton("ABC") { showLetterKeys() }
        val spaceButton = createButton("Space") { currentInputConnection.commitText(" ", 1) }
            .apply { layoutParams = LinearLayout.LayoutParams(0, dpToPx(keyHeightDp), 4f) }
        val enterButton = createButton(ICON_ENTER) { handleButtonClick(ICON_ENTER) }
        val settingsButton = createButton(ICON_SETTINGS) { handleButtonClick(ICON_SETTINGS) }

        bottomRow.addView(abcButton)
        bottomRow.addView(spaceButton)
        bottomRow.addView(enterButton)
        bottomRow.addView(settingsButton)

        keyboardLayout.addView(bottomRow)

        applyTheme()
    }

    private fun createRow(keys: List<String>): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(keyHeightDp))
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
            ICON_SHIFT_UP, ICON_SHIFT_DOWN -> {
                isCapital = !isCapital
                updateLetterButtons()
            }
            ICON_DELETE -> { currentInputConnection.deleteSurroundingText(1, 0) }
            ICON_ENTER -> { currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)) }
            ICON_SETTINGS -> { startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            "123" -> { showNumberKeys() }
            "ABC" -> { showLetterKeys() }
            "Space" -> { currentInputConnection.commitText(" ", 1) }
            else -> {
                val charToCommit = if (isCapital && !isNumber && key.length == 1) key.uppercase() else key
                currentInputConnection.commitText(charToCommit, 1)
            }
        }
    }

    private fun updateLetterButtons() {
        val allRows = listOf(
            if (isCapital) capitalRow1 else simpleRow1,
            if (isCapital) capitalRow2 else simpleRow2,
            if (isCapital) capitalRow3 else simpleRow3
        )
        for (i in letterRows.indices) {
            val row = letterRows[i]
            val keys = allRows[i]
            for (j in 0 until row.childCount) {
                val btn = row.getChildAt(j) as Button
                btn.text = keys[j]
            }
        }
    }

    private fun createBottomRow(): LinearLayout {
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(keyHeightDp))
        }

        val numberToggle = createButton("123") { showNumberKeys() }
        val spaceButton = createButton("Space") { currentInputConnection.commitText(" ", 1) }
            .apply { layoutParams = LinearLayout.LayoutParams(0, dpToPx(keyHeightDp), 4f) }
        val enterButton = createButton(ICON_ENTER) { handleButtonClick(ICON_ENTER) }
        val settingsButton = createButton(ICON_SETTINGS) { handleButtonClick(ICON_SETTINGS) }

        bottomRow.addView(numberToggle)
        bottomRow.addView(spaceButton)
        bottomRow.addView(enterButton)
        bottomRow.addView(settingsButton)

        return bottomRow
    }

    private fun applyTheme() {
        keyboardLayout.background = ColorDrawable(keyboardBackgroundColor)

        letterRows.forEach { row ->
            for (i in 0 until row.childCount) {
                val btn = row.getChildAt(i) as Button
                val gd = GradientDrawable().apply {
                    setColor(keyBackgroundColor)
                    setStroke(dpToPx(keyBorderWidthDp), keyBorderColor)
                    cornerRadius = dpToPxF(keyCornerRadiusDp)
                }
                btn.background = gd
                btn.setTextColor(keyTextColor)
            }
        }

        val bottomRow = keyboardLayout.getChildAt(keyboardLayout.childCount - 1) as LinearLayout
        for (i in 0 until bottomRow.childCount) {
            val btn = bottomRow.getChildAt(i) as Button
            val gd = GradientDrawable().apply {
                setColor(keyBackgroundColor)
                setStroke(dpToPx(keyBorderWidthDp), keyBorderColor)
                cornerRadius = dpToPxF(keyCornerRadiusDp)
            }
            btn.background = gd
            btn.setTextColor(keyTextColor)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
    private fun dpToPxF(dp: Float): Float = (dp * resources.displayMetrics.density)
}
