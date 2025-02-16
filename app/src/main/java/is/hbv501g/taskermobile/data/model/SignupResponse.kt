package `is`.hbv501g.taskermobile.data.model

// models/SignupResponse.kt
data class SignupResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null
)