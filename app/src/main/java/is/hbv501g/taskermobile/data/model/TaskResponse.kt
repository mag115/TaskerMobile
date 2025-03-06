package `is`.hbv501g.taskermobile.data.model

import com.google.gson.annotations.SerializedName
//seralizedname so that api responses are mapped correctly


data class TaskResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null
)
