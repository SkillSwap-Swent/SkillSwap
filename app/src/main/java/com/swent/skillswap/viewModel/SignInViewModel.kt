package com.swent.skillswap.viewModel

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swent.skillswap.model.SignIn.SignInGoogleModel
import com.swent.skillswap.model.SignIn.SignInGoogleParams
import com.swent.skillswap.model.SignIn.SignInInterface
import kotlinx.coroutines.launch

class SignInViewModel(
    private val goToMainScreen: () -> Unit,
    private val goToCreateAccountScreen: () -> Unit,
    private val goToClassicSignIn: () -> Unit
) : ViewModel() {
    private val googleModel: SignInInterface = SignInGoogleModel()

    fun googleSignIn(credentialManager: CredentialManager, activity: Activity) =
        viewModelScope.launch {
            try {
                googleModel.signIn(SignInGoogleParams(activity, credentialManager))
                if ((googleModel as SignInGoogleModel).googleAccountInfoAreSavedInFirestore()) {
                    goToMainScreen()
                } else {
                    goToCreateAccountScreen
                }
            } catch (e: Exception) {}
        }

    fun classicSignIn() {
        goToClassicSignIn()
    }

    fun createAccount() {
        goToCreateAccountScreen()
    }
}

class SignInVmFactory(
    private val goToMainScreen: () -> Unit = {},
    private val goToCreateAccountScreen: () -> Unit = {},
    private val goToClassicSignIn: () -> Unit = {}
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignInViewModel(goToMainScreen, goToCreateAccountScreen, goToClassicSignIn) as T
    }
}
