package utils

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes

fun Context.getColorByBuildVersion(@ColorRes id:Int) :Int{
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        this.resources.getColor(id)
    } else{
        this.getColor(id)
    }
}