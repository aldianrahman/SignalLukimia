package com.lukimia.signalapp.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context
) {
    private val auth = Firebase.auth

    private val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // GANTI DENGAN WEB CLIENT ID ANDA DARI GOOGLE CLOUD CONSOLE
            .requestIdToken("1076919487304-mfbn4supjgmsjiev23vqodvoae0cvck2.apps.googleusercontent.com")
            .requestEmail()
            .build()
    )

    suspend fun signIn(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun signOut() {
        googleSignInClient.signOut().await()
        auth.signOut()
    }

    suspend fun getSignedInUser() = auth.currentUser

    suspend fun signInWithIntent(intent: Intent): Result<FirebaseUser> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)
                ?: return Result.failure(Exception("Google sign-in returned null account"))
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Firebase authentication returned null user"))
        } catch (e: ApiException) {
            Result.failure(Exception("Google sign-in failed (code: ${e.statusCode}). Check SHA-1 fingerprint and Web Client ID configuration."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}