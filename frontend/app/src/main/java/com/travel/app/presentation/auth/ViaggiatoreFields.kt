package com.travel.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.travel.app.presentation.components.auth.TravelTextField

@Composable
fun ViaggiatoreFields(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TravelTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            label = "Nome *",
            leadingIcon = Icons.Default.Person,
        )
        TravelTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = "Cognome *",
            leadingIcon = Icons.Default.Person,
        )
    }
}
