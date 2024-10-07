package com.example.tp1_arqmov

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(): ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _sessionClosed = MutableLiveData<Boolean>()
    val sessionClosed: LiveData<Boolean> = _sessionClosed

    fun getAllExpenses() {

        firestore.collection("expenses")
            .get()
            .addOnSuccessListener { result ->

                val expenses = result.map { document ->
                    document.toObject(Expense::class.java)
                }
               _expenses.value = expenses
            }
            .addOnFailureListener { exception ->
                _expenses.value = emptyList()
            }
    }




    fun addExpense(newExpense: Expense) {
        firestore.collection("expenses")
            .document(newExpense.id)
            .set(newExpense)
            .addOnSuccessListener {

                println("Expense guardado correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al guardar el expense: $e")
            }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _sessionClosed.value = true
        }
    }

}
