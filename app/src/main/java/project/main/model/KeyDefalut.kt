package project.main.model

import com.google.gson.annotations.SerializedName
import project.main.const.constantID
import project.main.const.constantName
import project.main.const.constantPassword


data class KeyDefault(
    @SerializedName("key_name")
    var keyName: String = constantName,

    @SerializedName("key_password")
    var keyPassword: String = constantPassword,

    @SerializedName("key_id")
    var keyID: String = constantID,

    @SerializedName("setting_Status")
    var settingStatus: Int = 0 // 0=>未設定,有值以後是1，每次設定都+1

)