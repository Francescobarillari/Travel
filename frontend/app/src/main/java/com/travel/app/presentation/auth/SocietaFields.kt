package com.travel.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.travel.app.presentation.components.auth.TravelTextField

@Composable
fun SocietaFields(
    companyName: String,
    onCompanyNameChange: (String) -> Unit,
    vatNumber: String,
    onVatNumberChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TravelTextField(
            value = companyName,
            onValueChange = onCompanyNameChange,
            label = "Nome Società*",
            leadingIcon = Icons.Default.Business,
        )
        TravelTextField(
            value = vatNumber,
            onValueChange = onVatNumberChange,
            label = "Partita IVA *",
            leadingIcon = Icons.Default.Badge,
            keyboardType = KeyboardType.Number,
        )
    }
}
