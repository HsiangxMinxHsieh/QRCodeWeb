package project.main.model

import com.google.gson.annotations.SerializedName
import project.main.const.constantName
import project.main.const.constantPassword


data class KeyDefault(
    @SerializedName("key_name")
    var keyName:String = constantName,

    @SerializedName("key_password")
    var keyPassword:String = constantPassword,

    @SerializedName("setting_Status")
    var settingStatus :Int= 0 // 0=>未設定

)