package `is`.hbv501g.taskermobile.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `is`.hbv501g.taskermobile.data.model.User

class UserItem {
}@Composable
fun UserItem(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Username: ${user.username}", style = MaterialTheme.typography.titleLarge)
            Text(text = "Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Role: ${user.role}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
