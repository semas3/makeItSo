/*
Copyright 2022 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.example.makeitso.screens.login

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.example.makeitso.LOGIN_SCREEN
import com.example.makeitso.R.string as AppText
import com.example.makeitso.SETTINGS_SCREEN
import com.example.makeitso.common.ext.isValidEmail
import com.example.makeitso.common.snackbar.SnackbarManager
import com.example.makeitso.model.service.AccountService
import com.example.makeitso.model.service.LogService
import com.example.makeitso.screens.MakeItSoViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val accountService: AccountService,
  logService: LogService
) : MakeItSoViewModel(logService) {
  var uiState = mutableStateOf(LoginUiState())
    private set

  private val email
    get() = uiState.value.email
  private val password
    get() = uiState.value.password

  fun onEmailChange(newValue: String) {
    uiState.value = uiState.value.copy(email = newValue)
  }

  fun onPasswordChange(newValue: String) {
    uiState.value = uiState.value.copy(password = newValue)
  }

  fun onSignInClick(openAndPopUp: (String, String) -> Unit) {
//    if (!email.isValidEmail()) {
//      SnackbarManager.showMessage(AppText.email_error)
//      return
//    }
//
//    if (password.isBlank()) {
//      SnackbarManager.showMessage(AppText.empty_password_error)
//      return
//    }

    launchCatching {
      accountService.authenticate(email, password)
      openAndPopUp(SETTINGS_SCREEN, LOGIN_SCREEN)
    }
  }

  fun onSignInGoogleClick(context: Context, openAndPopUp: (String, String) -> Unit) {
//    if (!email.isValidEmail()) {
//      SnackbarManager.showMessage(AppText.email_error)
//      return
//    }
//
//    if (password.isBlank()) {
//      SnackbarManager.showMessage(AppText.empty_password_error)
//      return
//    }

    launchCatching {
      val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("689530234935-rbi12mj0pjq5mp5anpcau4pioj41evrt.apps.googleusercontent.com")
        .requestEmail()
        .build()
      val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
      val intent: Intent = googleSignInClient.signInIntent
      val signInAccountTask: Task<GoogleSignInAccount> =
        GoogleSignIn.getSignedInAccountFromIntent(intent)
      val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
      val credential: AuthCredential = GoogleAuthProvider.getCredential(
        googleSignInAccount.idToken, null
      )
      accountService.authenticateWithGoogle(credential)
      openAndPopUp(SETTINGS_SCREEN, LOGIN_SCREEN)
    }
  }

  fun onForgotPasswordClick() {
    if (!email.isValidEmail()) {
      SnackbarManager.showMessage(AppText.email_error)
      return
    }

    launchCatching {
      accountService.sendRecoveryEmail(email)
      SnackbarManager.showMessage(AppText.recovery_email_sent)
    }
  }
}
