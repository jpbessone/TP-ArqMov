package com.example.tp1_arqmov

import HomeScreen
import RegistrationScreen
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tp1_arqmov.ui.theme.TP1ArqMovTheme
import com.google.gson.Gson
import android.Manifest



class MainActivity : ComponentActivity() {

    private lateinit var navHostController: NavHostController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()

        setContent {
            navHostController = rememberNavController()
            TP1ArqMovTheme {
                InitNavigation()
            }
        }
    }

    @Composable
    fun InitNavigation() {
        val gson = Gson()


        NavHost(navController = navHostController, startDestination = "Login") {



            composable("Login") {
                val viewModel : LoginViewModel by viewModels()
                LoginScreen(
                    viewModel = viewModel,
                    navigateToHome = {
                        navHostController.navigate("Home")
                    },
                    navigateToRegister = { firebaseUser ->

                        val firebaseUserJson = gson.toJson(firebaseUser)
                        navHostController.navigate("Register/${firebaseUserJson}")
                    })
            }

            composable("Home") {
                val viewModel : HomeViewModel by viewModels()

                HomeScreen(
                    viewModel = viewModel,
                    navigateToAddExpense = {
                        navHostController.navigate("add_expense")
                    },
                    onLogout = {
                        navHostController.popBackStack()
                        navHostController.navigate("Login")
                    })
            }

            composable("Register/{firebaseUser}") { navBackStackEntry ->
                val json = navBackStackEntry.arguments?.getString("firebaseUser")
                val firebaseUser = gson.fromJson(json, FirebaseUser::class.java)

                val viewModel : LoginViewModel by viewModels()

                RegistrationScreen(
                    firebaseUser = firebaseUser,
                    viewModel = viewModel,
                    navigateToLogin = {
                        navHostController.popBackStack()
                    },
                    navigateToLHome = {
                        navHostController.navigate("Home")
                    }
                )

            }

            composable("add_expense") {

                val viewModel : HomeViewModel by viewModels()

                AddExpenseScreen(
                    onExpenseAdded = { newExpense ->
                       viewModel.addExpense(newExpense)
                        navHostController.popBackStack()
                    },
                    onNavigateBack = {
                        navHostController.popBackStack()
                    }
                )
            }

        }
    }

}
