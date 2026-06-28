package com.connectcamp.ui.consumer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.connectcamp.data.model.ProductCategory
import com.connectcamp.data.model.ProductUnit
import com.connectcamp.data.model.Season
import com.connectcamp.data.model.ShoppingListItem
import com.connectcamp.viewmodel.ConsumerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoppingItemScreen(
    consumerViewModel: ConsumerViewModel,
    onBack: () -> Unit
) {
    val operationResult by consumerViewModel.operationResult.collectAsState()

    var productName by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var notes by rememberSaveable { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.FRUITS) }
    var selectedSeason by remember { mutableStateOf(Season.ALL_YEAR) }
    var selectedUnit by remember { mutableStateOf(ProductUnit.KG) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var seasonExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(operationResult) {
        if (operationResult?.contains("añadido") == true) {
            consumerViewModel.clearOperationResult()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir a la lista") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Nombre del producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Box {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    label = { Text("Categoría") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { categoryExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ProductCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = { selectedCategory = category; categoryExpanded = false }
                        )
                    }
                }
            }

            Box {
                OutlinedTextField(
                    value = selectedSeason.displayName,
                    onValueChange = {},
                    label = { Text("Temporada") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { seasonExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = seasonExpanded,
                    onDismissRequest = { seasonExpanded = false }
                ) {
                    Season.entries.forEach { season ->
                        DropdownMenuItem(
                            text = { Text(season.displayName) },
                            onClick = { selectedSeason = season; seasonExpanded = false }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedUnit.displayName,
                        onValueChange = {},
                        label = { Text("Unidad") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { unitExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        ProductUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.displayName) },
                                onClick = { selectedUnit = unit; unitExpanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    consumerViewModel.addShoppingListItem(
                        ShoppingListItem(
                            productName = productName.trim(),
                            category = selectedCategory,
                            season = selectedSeason,
                            quantity = quantity.toDoubleOrNull() ?: 1.0,
                            unit = selectedUnit,
                            notes = notes.trim()
                        )
                    )
                },
                enabled = productName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Añadir a la lista")
            }
        }
    }
}
