package com.example.tp1_arqmov

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel() : ViewModel() {

    val firestore = FirebaseFirestore.getInstance()

    val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Loading)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun loginWithGoogleCredential(credential: AuthCredential) {

        viewModelScope.launch {

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        verifyIfIsSigned()
                    } else {
                        _uiState.value =
                            LoginUiState.Error(task.exception?.message ?: "Error desconocido")
                    }
                }

        }
    }

    fun verifyIfIsSigned() {
        viewModelScope.launch {

            val firebaseUser = auth.currentUser?.run {
                FirebaseUser(
                    userId = uid,
                    userName = displayName,
                    email = email,
                    avatarUrl = ""
                )
            }

            if (firebaseUser != null) {

                val userExists = checkIfUserExists(firebaseUser)

                if (userExists) {
                    _uiState.value = LoginUiState.Success(true, firebaseUser, true)
                } else {
                    _uiState.value = LoginUiState.Success(true, firebaseUser, false)
                }


            } else {
                Log.d("USER", "NULO")
                _uiState.value = LoginUiState.Success(false, null, false)
            }
        }

    }

    suspend fun checkIfUserExists(firebaseUser: FirebaseUser): Boolean {

        return try {
            val documentSnapshot =
                firestore.collection("users").document(firebaseUser.userId).get().await()
            documentSnapshot.exists()
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error al verificar si el usuario existe: $e")
            false
        }
    }

    fun saveUserToFirestore(firebaseUser: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val gson = Gson()
        try {
            val userJson = gson.toJson(firebaseUser)

            val userMap: Map<*, *>? = gson.fromJson(userJson, Map::class.java)

            db.collection("users")
                .document(firebaseUser.userId)
                .set(userMap!!)
                .addOnSuccessListener {
                    Log.d("Firestore", "Usuario guardado correctamente: ${firebaseUser.userName}")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Error al guardar el usuario en Firestore: $e")
                }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error al procesar JSON: $e")
        }
    }

    fun uploadImageToFirebaseStorage(imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageReference = Firebase.storage.reference
        val fileName = "profile_images/${System.currentTimeMillis()}.jpg"
        val imageRef = storageReference.child(fileName)

        imageUri.let { uri ->
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                // Get the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener { e ->
                    onFailure(e)
                }
            }.addOnFailureListener { e ->
                onFailure(e)
            }
        }
    }
}

