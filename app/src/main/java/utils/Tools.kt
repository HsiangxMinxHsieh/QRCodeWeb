package utils

import android.app.Activity
import android.content.Intent

fun Activity.goToNextPageFinishThisPage(intent: Intent) {
    this.startActivity(intent)
    this.finish()
}
