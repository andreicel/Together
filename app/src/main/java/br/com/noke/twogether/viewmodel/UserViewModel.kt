package br.com.noke.twogether.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.noke.twogether.model.Category
import br.com.noke.twogether.model.User
import br.com.noke.twogether.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    private var userId: String? = null
    private val _userCategories = MutableStateFlow<List<String>>(emptyList())
    val userCategories: StateFlow<List<String>> = _userCategories

    init {
        Log.d("UserViewModel", "UserViewModel initialized")
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                userRepository.getUsers().collect { userList ->
                    _users.value = userList
                    Log.d("UserViewModel", "Users loaded: $userList")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading users", e)
            }
        }
    }

    fun addUser(user: User, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            userId = userRepository.addUserAndGetId(user)
            onComplete(userId != null)
        }
    }

    fun updateUserCategories(categories: List<Category>, onComplete: (Boolean) -> Unit) {
        userId?.let { id ->
            viewModelScope.launch {
                val success = userRepository.updateUserCategories(id, categories.map { it.name })
                onComplete(success)
            }
        } ?: onComplete(false)  // Se userId for null, retorna falso
    }

    fun fetchUserCategories() {
        userId?.let { id ->
            viewModelScope.launch {
                val user = userRepository.getUserById(id)
                user?.let {
                    _userCategories.value = it.categories.map { category -> category.displayName }
                }
            }
        }
    }


}



