package com.duocuc.tiendaropa.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBadgeIcon(
    itemCount: Int,
    contentDescription: String? = null
) {
    Box {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = contentDescription
        )
        if (itemCount > 0) {
            @OptIn(ExperimentalMaterial3Api::class)
            Badge(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = if (itemCount > 99) "99+" else itemCount.toString(),
                    fontSize = 10.sp
                )
            }
        }
    }
}

