package com.lukimia.signalapp.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

    suspend fun signInWithIntent(intent: Intent) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        val account = try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            throw Exception("Google Sign-In gagal (kode: ${e.statusCode}). Pastikan SHA-1 sudah dikonfigurasi di Firebase Console.", e)
        }
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).await()
    }
}