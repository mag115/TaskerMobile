package `is`.hbv501g.taskermobile.data.model

data class TaskResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)