package `is`.hbv501g.taskermobile.data.model

data class LoginResponse(
    val token: String,
    val expiresIn: Long,
    val role: String,
    val userId: Long,
    val username: String
)
