package com.connectcamp.ui.consumer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.connectcamp.R
import com.connectcamp.data.model.ShoppingListItem
import com.connectcamp.viewmodel.ConsumerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    consumerViewModel: ConsumerViewModel,
    onAddItem: () -> Unit,
    onBack: () -> Unit
) {
    val shoppingList by consumerViewModel.shoppingList.collectAsState()
    val pending = shoppingList.filter { !it.isAcquired }
    val acquired = shoppingList.filter { it.isAcquired }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_list)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_to_list))
            }
        }
    ) { padding ->
        if (shoppingList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Tu lista de la compra está vacía")
                Text(
                    "Pulsa + para añadir productos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pending.isNotEmpty()) {
                    item {
                        Text(
                            "Por comprar (${pending.size})",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    items(pending) { item ->
                        ShoppingItemCard(
                            item = item,
                            onToggle = { consumerViewModel.toggleItemAcquired(item.id, !item.isAcquired) },
                            onDelete = { consumerViewModel.deleteShoppingListItem(item.id) }
                        )
                    }
                }
                if (acquired.isNotEmpty()) {
                    item {
                        Text(
                            "Adquiridos (${acquired.size})",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(acquired) { item ->
                        ShoppingItemCard(
                            item = item,
                            onToggle = { consumerViewModel.toggleItemAcquired(item.id, !item.isAcquired) },
                            onDelete = { consumerViewModel.deleteShoppingListItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingListItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isAcquired,
                onCheckedChange = { onToggle() }
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = item.productName,
                    style = if (item.isAcquired)
                        MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
                    else MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${item.quantity} ${item.unit.displayName} · ${item.season.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}
