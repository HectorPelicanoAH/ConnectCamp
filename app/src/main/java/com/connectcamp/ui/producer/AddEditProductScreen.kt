package com.connectcamp.ui.producer

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.connectcamp.data.model.Product
import com.connectcamp.data.model.ProductCategory
import com.connectcamp.data.model.ProductUnit
import com.connectcamp.data.model.Season
import com.connectcamp.viewmodel.ProducerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    producerViewModel: ProducerViewModel,
    productId: String? = null,
    onBack: () -> Unit
) {
    val myProducts by producerViewModel.myProducts.collectAsState()
    val isLoading by producerViewModel.isLoading.collectAsState()
    val operationResult by producerViewModel.operationResult.collectAsState()

    val existingProduct = productId?.let { id -> myProducts.find { it.id == id } }

    var name by rememberSaveable { mutableStateOf(existingProduct?.name ?: "") }
    var description by rememberSaveable { mutableStateOf(existingProduct?.description ?: "") }
    var price by rememberSaveable { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    var quantity by rememberSaveable { mutableStateOf(existingProduct?.availableQuantity?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(existingProduct?.category ?: ProductCategory.FRUITS) }
    var selectedSeason by remember { mutableStateOf(existingProduct?.season ?: Season.ALL_YEAR) }
    var selectedUnit by remember { mutableStateOf(existingProduct?.unit ?: ProductUnit.KG) }
    var isAvailable by rememberSaveable { mutableStateOf(existingProduct?.isAvailable ?: true) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var seasonExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationResult) {
        operationResult?.let {
            snackbarHostState.showSnackbar(it)
            producerViewModel.clearOperationResult()
            if (it.contains("correctamente") || it.contains("añadido")) {
                onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId != null) "Editar producto" else "Añadir producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Category dropdown
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
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Season dropdown
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
                            onClick = {
                                selectedSeason = season
                                seasonExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                // Unit dropdown
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
                                onClick = {
                                    selectedUnit = unit
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Cantidad disponible") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Disponible", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val product = Product(
                        id = existingProduct?.id ?: "",
                        name = name.trim(),
                        category = selectedCategory,
                        season = selectedSeason,
                        price = price.toDoubleOrNull() ?: 0.0,
                        unit = selectedUnit,
                        availableQuantity = quantity.toDoubleOrNull() ?: 0.0,
                        description = description.trim(),
                        isAvailable = isAvailable
                    )
                    if (existingProduct != null) {
                        producerViewModel.updateProduct(product)
                    } else {
                        producerViewModel.addProduct(product)
                    }
                },
                enabled = name.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text(if (productId != null) "Actualizar producto" else "Añadir producto")
                }
            }
        }
    }
}
