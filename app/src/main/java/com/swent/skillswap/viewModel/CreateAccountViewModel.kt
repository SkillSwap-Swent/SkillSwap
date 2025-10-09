package com.swent.skillswap.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swent.skillswap.model.SignIn.SignInClassicModel
import com.swent.skillswap.model.SignIn.SignInGoogleModel
import com.swent.skillswap.model.SignIn.SignInInterface

class CreateAccountViewModel(
    private val goToMainScreen: () -> Unit,
    private val isGoogleAccount: Boolean
) : ViewModel() {
    private val model: SignInInterface =
        if (isGoogleAccount) SignInGoogleModel() else SignInClassicModel()
}

class CreateAccountVmFactory(
    private val goToMainScreen: () -> Unit = {},
    private val isGoogleAccount: Boolean
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateAccountViewModel(goToMainScreen, isGoogleAccount) as T
    }
}
