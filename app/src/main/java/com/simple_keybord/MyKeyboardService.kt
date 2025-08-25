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
        var keyboardBackgroundColor: Int = 0xFFFFFFFF.toInt() // Light default
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
            "Light" to Triple(0xFFFFFFFF.toInt(), Color.LTGRAY, Color.BLACK),
            "Dracular" to Triple(0xFF000000.toInt(), Color.DKGRAY, Color.RED),
            "Gray" to Triple(0xFF9E9E9E.toInt(), Color.GRAY, Color.BLACK),
            "SkyBlue" to Triple(0xFF87CEEB.toInt(), Color.CYAN, Color.BLACK),
            "Forest" to Triple(0xFF228B22.toInt(), Color.GREEN, Color.WHITE)
        )

        const val ACTION_CHANGE_THEME = "com.simple_keybord.ACTION_CHANGE_THEME"
        const val EXTRA_THEME_NAME = "theme_name"
    }

    private val themeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CHANGE_THEME) {
                val themeName = intent.getStringExtra(EXTRA_THEME_NAME) ?: return
                val colors = themes[themeName] ?: return
                // Apply theme colors
                keyboardBackgroundColor = colors.first
                keyBackgroundColor = colors.second
                keyTextColor = colors.third
                applyTheme()
            }
        }
    }

    // Simple & capital rows
    private val simpleRow1 = listOf("q","w","e","r","t","y","u","i","o","p")
    private val simpleRow2 = listOf("a","s","d","f","g","h","j","k","l")
    private val simpleRow3 = listOf("^","z","x","c","v","b","n","m","Esc")
    private val capitalRow1 = listOf("Q","W","E","R","T","Y","U","I","O","P")
    private val capitalRow2 = listOf("A","S","D","F","G","H","J","K","L")
    private val capitalRow3 = listOf("^","Z","X","C","V","B","N","M","Esc")

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

        // Register theme broadcast receiver
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

        // Bottom row
        val bottomRow = createBottomRow()
        keyboardLayout.addView(bottomRow)

        applyTheme()
    }

    private fun createBottomRow(): LinearLayout {
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(keyHeightDp))
        }

        val numberToggle = createButton("123") { showNumberKeys() }
        val spaceButton = createButton("Space") { currentInputConnection.commitText(" ", 1) }
            .apply { layoutParams = LinearLayout.LayoutParams(0, dpToPx(keyHeightDp), 4f) }
        val enterButton = createButton("Enter") { currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)) }
        val settingsButton = createButton("⚙️") { startActivity(Intent(this@MyKeyboardService, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }

        bottomRow.addView(numberToggle)
        bottomRow.addView(spaceButton)
        bottomRow.addView(enterButton)
        bottomRow.addView(settingsButton)

        return bottomRow
    }

    private fun showNumberKeys() {
        keyboardLayout.removeAllViews()
        isNumber = true
        letterRows.clear()

        val row1Keys = listOf("1","2","3","4","5","6","7","8","9","0")
        val row2Keys = listOf("-","/",";",":","(",")","$","&","@","\"")
        val row3Keys = listOf("^",".",",","?","!","#","%","*","Esc")

        letterRows.add(createRow(row1Keys))
        letterRows.add(createRow(row2Keys))
        letterRows.add(createRow(row3Keys))
        letterRows.forEach { keyboardLayout.addView(it) }

        val bottomRow = createBottomRow()
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
                setColor(keyBackgroundColor) // CHANGE THIS FOR ROW COLORS IF NEEDED
                setStroke(dpToPx(keyBorderWidthDp), keyBorderColor)
                cornerRadius = dpToPxF(keyCornerRadiusDp)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun handleButtonClick(key: String) {
        when (key) {
            "^" -> { isCapital = !isCapital; updateLetterButtons() }
            "Esc" -> currentInputConnection.deleteSurroundingText(1, 0)
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

    private fun applyTheme() {
        // Update keyboard background
        keyboardLayout.background = ColorDrawable(keyboardBackgroundColor)

        // Update all key colors including bottom row
        letterRows.forEach { row ->
            for (i in 0 until row.childCount) {
                val btn = row.getChildAt(i) as Button
                val gd = GradientDrawable().apply {
                    setColor(keyBackgroundColor) // Row color
                    setStroke(dpToPx(keyBorderWidthDp), keyBorderColor)
                    cornerRadius = dpToPxF(keyCornerRadiusDp)
                }
                btn.background = gd
                btn.setTextColor(keyTextColor)
            }
        }

        // Update bottom row
        val bottomRow = keyboardLayout.getChildAt(keyboardLayout.childCount - 1) as LinearLayout
        for (i in 0 until bottomRow.childCount) {
            val btn = bottomRow.getChildAt(i) as Button
            val gd = GradientDrawable().apply {
                setColor(keyBackgroundColor) // Bottom row color same as theme
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
