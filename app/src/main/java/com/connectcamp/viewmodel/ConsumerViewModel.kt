package com.connectcamp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectcamp.data.model.ProducerWithLocation
import com.connectcamp.data.model.ShoppingListItem
import com.connectcamp.data.model.User
import com.connectcamp.data.repository.ConsumerRepository
import com.connectcamp.data.repository.ProducerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsumerViewModel @Inject constructor(
    private val consumerRepository: ConsumerRepository,
    private val producerRepository: ProducerRepository
) : ViewModel() {

    private val _shoppingList = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val shoppingList: StateFlow<List<ShoppingListItem>> = _shoppingList.asStateFlow()

    private val _allProducers = MutableStateFlow<List<User>>(emptyList())
    val allProducers: StateFlow<List<User>> = _allProducers.asStateFlow()

    private val _producersWithLocation = MutableStateFlow<List<ProducerWithLocation>>(emptyList())
    val producersWithLocation: StateFlow<List<ProducerWithLocation>> = _producersWithLocation.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeShoppingList()
        observeAllProducers()
        observeProducersWithLocation()
    }

    private fun observeShoppingList() {
        viewModelScope.launch {
            consumerRepository.getShoppingListFlow().collect {
                _shoppingList.value = it
            }
        }
    }

    private fun observeAllProducers() {
        viewModelScope.launch {
            producerRepository.getAllProducersFlow().collect {
                _allProducers.value = it
            }
        }
    }

    private fun observeProducersWithLocation() {
        viewModelScope.launch {
            producerRepository.getAllProducersWithLocationFlow().collect {
                _producersWithLocation.value = it
            }
        }
    }

    fun addShoppingListItem(item: ShoppingListItem) {
        viewModelScope.launch {
            val result = consumerRepository.addShoppingListItem(item)
            _operationResult.value = if (result.isSuccess) {
                "Producto añadido a la lista"
            } else {
                "Error al añadir el producto"
            }
        }
    }

    fun updateShoppingListItem(item: ShoppingListItem) {
        viewModelScope.launch {
            val result = consumerRepository.updateShoppingListItem(item)
            _operationResult.value = if (result.isSuccess) {
                "Producto actualizado"
            } else {
                "Error al actualizar el producto"
            }
        }
    }

    fun toggleItemAcquired(itemId: String, isAcquired: Boolean) {
        viewModelScope.launch {
            consumerRepository.toggleItemAcquired(itemId, isAcquired)
        }
    }

    fun deleteShoppingListItem(itemId: String) {
        viewModelScope.launch {
            val result = consumerRepository.deleteShoppingListItem(itemId)
            _operationResult.value = if (result.isSuccess) {
                "Producto eliminado de la lista"
            } else {
                "Error al eliminar el producto"
            }
        }
    }

    fun searchProducersByProduct(productName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _searchResults.value = producerRepository.searchProducersByProduct(productName)
            _isLoading.value = false
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }
}
