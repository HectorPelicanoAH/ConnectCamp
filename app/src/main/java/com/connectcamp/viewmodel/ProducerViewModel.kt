package com.connectcamp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectcamp.data.model.Product
import com.connectcamp.data.model.ProducerProfile
import com.connectcamp.data.model.User
import com.connectcamp.data.repository.ProducerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProducerViewModel @Inject constructor(
    private val producerRepository: ProducerRepository
) : ViewModel() {

    private val _producerProfile = MutableStateFlow<ProducerProfile?>(null)
    val producerProfile: StateFlow<ProducerProfile?> = _producerProfile.asStateFlow()

    private val _myProducts = MutableStateFlow<List<Product>>(emptyList())
    val myProducts: StateFlow<List<Product>> = _myProducts.asStateFlow()

    private val _allProducers = MutableStateFlow<List<User>>(emptyList())
    val allProducers: StateFlow<List<User>> = _allProducers.asStateFlow()

    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProfile()
        observeMyProducts()
        observeAllProducers()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = producerRepository.getOrCreateProducerProfile()
            _producerProfile.value = profile
            profile?.uid?.let { uid ->
                producerRepository.getProducerProfileFlow(uid).collect {
                    _producerProfile.value = it
                }
            }
        }
    }

    private fun observeMyProducts() {
        viewModelScope.launch {
            producerRepository.getMyProductsFlow().collect {
                _myProducts.value = it
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

    fun updateProfile(profile: ProducerProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = producerRepository.updateProducerProfile(profile)
            _isLoading.value = false
            _operationResult.value = if (result.isSuccess) {
                "Perfil actualizado correctamente"
            } else {
                "Error al actualizar el perfil"
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, locationName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = producerRepository.updateLocation(latitude, longitude, locationName)
            _isLoading.value = false
            _operationResult.value = if (result.isSuccess) {
                "Ubicación actualizada correctamente"
            } else {
                "Error al actualizar la ubicación"
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = producerRepository.addProduct(product)
            _isLoading.value = false
            _operationResult.value = if (result.isSuccess) {
                "Producto añadido correctamente"
            } else {
                "Error al añadir el producto: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = producerRepository.updateProduct(product)
            _isLoading.value = false
            _operationResult.value = if (result.isSuccess) {
                "Producto actualizado correctamente"
            } else {
                "Error al actualizar el producto"
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = producerRepository.deleteProduct(productId)
            _isLoading.value = false
            _operationResult.value = if (result.isSuccess) {
                "Producto eliminado"
            } else {
                "Error al eliminar el producto"
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }
}
