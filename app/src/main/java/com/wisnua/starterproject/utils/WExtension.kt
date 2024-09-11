package com.wisnua.starterproject.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


fun TextInputEditText.setupDrawableRightEditText(drawableResId: Int) {
    val drawable = ContextCompat.getDrawable(this.context, drawableResId)
    drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}


fun TextInputEditText.onSearch(action: () -> Unit) {
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                action()
                true
            } else {
                false
            }
        }
    }

fun TextInputEditText.onClickIconRightEditText(action: () -> Unit) {
        this.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val rightDrawable = this.compoundDrawables[2]
                if (rightDrawable != null && event.rawX >= (this.right - rightDrawable.bounds.width())) {
                    action()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }


fun TextInputEditText.textChanges(): Flow<CharSequence?> = callbackFlow {
    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            trySend(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    addTextChangedListener(textWatcher)
    awaitClose { removeTextChangedListener(textWatcher) }
}

fun View.goVisible() {
    if (visibility != View.VISIBLE)
        visibility = View.VISIBLE
}

fun View.goInvisible() {
    if (visibility != View.INVISIBLE)
        visibility = View.INVISIBLE
}

fun View.goGone() {
    if (visibility != View.GONE)
        visibility = View.GONE
}
