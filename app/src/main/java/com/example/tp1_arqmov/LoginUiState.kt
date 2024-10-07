package com.example.tp1_arqmov


sealed class LoginUiState {

    object Loading : LoginUiState()
    data class Success(val isSignedIn: Boolean, val firebaseUser: FirebaseUser?, val userExists: Boolean) : LoginUiState()
    data class Error(val errorMessage: String) : LoginUiState()
}