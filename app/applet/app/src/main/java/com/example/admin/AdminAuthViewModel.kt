package com.example.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminAuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _adminLoginState = MutableStateFlow<AdminLoginState>(AdminLoginState.Idle)
    val adminLoginState: StateFlow<AdminLoginState> = _adminLoginState.asStateFlow()

    fun loginAdmin(email: String, pass: String) {
        if (email != "royal2781925@gmail.com" || pass != "Dinesh007") {
            _adminLoginState.value = AdminLoginState.Error("Invalid admin credentials")
            return
        }
        
        viewModelScope.launch {
            _adminLoginState.value = AdminLoginState.Loading
            try {
                // In a real scenario, you'd authenticate with Firebase Auth using these credentials
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                if (result.user?.email == "royal2781925@gmail.com") {
                     _adminLoginState.value = AdminLoginState.Success
                } else {
                     _adminLoginState.value = AdminLoginState.Error("Not an admin account")
                     auth.signOut()
                }
            } catch (e: Exception) {
                // If the account doesn't exist yet, we could create it, 
                // but usually the admin creates it from the Firebase console.
                _adminLoginState.value = AdminLoginState.Error(e.message ?: "Authentication failed")
            }
        }
    }
}

sealed class AdminLoginState {
    object Idle : AdminLoginState()
    object Loading : AdminLoginState()
    object Success : AdminLoginState()
    data class Error(val message: String) : AdminLoginState()
}
