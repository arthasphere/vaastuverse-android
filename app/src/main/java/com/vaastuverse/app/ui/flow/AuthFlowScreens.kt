package com.vaastuverse.app.ui.flow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState

@Composable
fun WelcomeScreen(state: AppUiState, coordinator: AppCoordinatorViewModel) {
    FlowScaffold("Welcome to VaastuVerse", state, coordinator::clearMessage) {
        HintText("Sign in or create an account before exploring reports, partner tools, and more.")
        PrimaryButton("Continue with OTP") { coordinator.goToOtp() }
        SecondaryButton("Register with phone") { coordinator.goToRegister() }
        SecondaryButton("Sign in with email") { coordinator.goToEmailLogin() }
    }
}

@Composable
fun OtpLoginScreen(state: AppUiState, coordinator: AppCoordinatorViewModel) {
    var phone by rememberSaveable { mutableStateOf(state.otpPhone) }
    var code by rememberSaveable { mutableStateOf("") }
    val sent = state.otpSent

    FlowScaffold("OTP sign in", state, coordinator::clearMessage, onBack = coordinator::goToWelcome) {
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' || c == ' ' || c == '-' }.take(14) },
            label = { androidx.compose.material3.Text("Mobile (+91 optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !sent,
        )
        HintText("10-digit Indian number, e.g. 9876543210 or +91 9876543210")
        if (!sent) {
            PrimaryButton(
                "Send OTP",
                enabled = phone.filter { it.isDigit() }.length >= 10 && !state.isLoading,
            ) {
                coordinator.sendOtp(phone)
            }
        } else {
            HintText("OTP sent. Check your SMS or ask your administrator if you did not receive it.")
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter { ch -> ch.isDigit() }.take(6) },
                label = { androidx.compose.material3.Text("6-digit OTP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            PrimaryButton("Verify & continue", enabled = code.length == 6 && !state.isLoading) {
                coordinator.verifyOtp(phone, code)
            }
            SecondaryButton("Resend OTP") {
                coordinator.sendOtp(phone)
            }
            SecondaryButton("Change number") {
                phone = ""
                code = ""
                coordinator.resetOtpFlow()
            }
        }
    }
}

@Composable
fun RegisterScreen(state: AppUiState, coordinator: AppCoordinatorViewModel) {
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }

    FlowScaffold("Create account", state, coordinator::clearMessage, onBack = coordinator::goToWelcome) {
        OutlinedTextField(phone, { phone = it }, label = { androidx.compose.material3.Text("Mobile *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(email, { email = it }, label = { androidx.compose.material3.Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(password, { password = it }, label = { androidx.compose.material3.Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(displayName, { displayName = it }, label = { androidx.compose.material3.Text("Display name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        HintText("Next: basic profile, then a one-time Customer or Partner choice.")
        PrimaryButton("Register", enabled = phone.length == 10 && !state.isLoading) {
            coordinator.register(phone, email, password, displayName)
        }
    }
}

@Composable
fun EmailLoginScreen(state: AppUiState, coordinator: AppCoordinatorViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    FlowScaffold("Email sign in", state, coordinator::clearMessage, onBack = coordinator::goToWelcome) {
        OutlinedTextField(email, { email = it }, label = { androidx.compose.material3.Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(password, { password = it }, label = { androidx.compose.material3.Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        PrimaryButton("Sign in", enabled = email.isNotBlank() && password.isNotBlank() && !state.isLoading) {
            coordinator.loginWithEmail(email, password)
        }
    }
}

@Composable
fun CustomerProfileScreen(state: AppUiState, coordinator: AppCoordinatorViewModel) {
    var displayName by rememberSaveable {
        mutableStateOf(state.customerProfile?.displayName ?: "")
    }
    var city by rememberSaveable {
        mutableStateOf(state.customerProfile?.city ?: "")
    }

    FlowScaffold("Your profile", state, coordinator::clearMessage) {
        HintText("Basic details for your account. On the next screen you choose Customer or Partner — this cannot be changed later.")
        OutlinedTextField(displayName, { displayName = it }, label = { androidx.compose.material3.Text("Full name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(city, { city = it }, label = { androidx.compose.material3.Text("City") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        PrimaryButton("Save & continue", enabled = displayName.isNotBlank() && !state.isLoading) {
            coordinator.saveCustomerProfile(displayName, city)
        }
    }
}
