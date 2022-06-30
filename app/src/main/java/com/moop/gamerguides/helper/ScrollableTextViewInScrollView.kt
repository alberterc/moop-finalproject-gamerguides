package com.moop.gamerguides.helper

import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.widget.TextView

fun TextView.makeScrollableInScrollView() {
    movementMethod = ScrollingMovementMethod()
    setOnTouchListener { v, event ->
        v.parent.requestDisallowInterceptTouchEvent(true)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP -> {
                v.parent.requestDisallowInterceptTouchEvent(false)
                //It is required to call performClick() in onTouch event.
                performClick()
            }
        }
        false
    }
}
