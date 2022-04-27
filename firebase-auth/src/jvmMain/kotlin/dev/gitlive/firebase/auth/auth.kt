/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("jvm")
package dev.gitlive.firebase.auth

import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.ActionCodeMultiFactorInfo
import com.google.firebase.auth.ActionCodeResult.*
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual val Firebase.auth
    get() = FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance())

actual fun Firebase.auth(app: FirebaseApp) =
    FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance(app.jvm))

actual class FirebaseAuth internal constructor(val jvm: com.google.firebase.auth.FirebaseAuth) {
    actual val currentUser: FirebaseUser?
        get() = jvm.currentUser?.let { FirebaseUser(it) }

    actual val authStateChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val listener = object : AuthStateListener {
            override fun onAuthStateChanged(auth: com.google.firebase.auth.FirebaseAuth) {
                trySend(auth.currentUser?.let { FirebaseUser(it) })
            }
        }
        jvm.addAuthStateListener(listener)
        awaitClose { jvm.removeAuthStateListener(listener) }
    }

    actual val idTokenChanged get(): Flow<FirebaseUser?> = callbackFlow {
        val listener = object : com.google.firebase.auth.FirebaseAuth.IdTokenListener {
            override fun onIdTokenChanged(auth: com.google.firebase.auth.FirebaseAuth) {
                trySend(auth.currentUser?.let { FirebaseUser(it) })
            }
        }
        jvm.addIdTokenListener(listener)
        awaitClose { jvm.removeIdTokenListener(listener) }
    }

    actual var languageCode: String
        get() = jvm.languageCode ?: ""
        set(value) { jvm.setLanguageCode(value) }


    actual suspend fun applyActionCode(code: String) = jvm.applyActionCode(code).await().run { Unit }
    actual suspend fun confirmPasswordReset(code: String, newPassword: String) = jvm.confirmPasswordReset(code, newPassword).await().run { Unit }

    actual suspend fun createUserWithEmailAndPassword(email: String, password: String) =
        AuthResult(jvm.createUserWithEmailAndPassword(email, password).await())

    actual suspend fun fetchSignInMethodsForEmail(email: String): List<String> = jvm.fetchSignInMethodsForEmail(email).await().signInMethods.orEmpty()

    actual suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?) {
        jvm.sendPasswordResetEmail(email, actionCodeSettings?.toAndroid()).await()
    }

    actual suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings) = jvm.sendSignInLinkToEmail(email, actionCodeSettings.toAndroid()).await().run { Unit }

    actual fun isSignInWithEmailLink(link: String) = jvm.isSignInWithEmailLink(link)

    actual suspend fun signInWithEmailAndPassword(email: String, password: String) =
        AuthResult(jvm.signInWithEmailAndPassword(email, password).await())

    actual suspend fun signInWithCustomToken(token: String) =
        AuthResult(jvm.signInWithCustomToken(token).await())

    actual suspend fun signInAnonymously() = AuthResult(jvm.signInAnonymously().await())

    actual suspend fun signInWithCredential(authCredential: AuthCredential) =
        AuthResult(jvm.signInWithCredential(authCredential.jvm).await())

    actual suspend fun signInWithEmailLink(email: String, link: String) =
        AuthResult(jvm.signInWithEmailLink(email, link).await())

    actual suspend fun signOut() = jvm.signOut()

    actual suspend fun updateCurrentUser(user: FirebaseUser) = jvm.updateCurrentUser(user.jvm).await().run { Unit }
    actual suspend fun verifyPasswordResetCode(code: String): String = jvm.verifyPasswordResetCode(code).await()

    actual suspend fun <T : ActionCodeResult> checkActionCode(code: String): T {
        val result = jvm.checkActionCode(code).await()
        @Suppress("UNCHECKED_CAST")
        return when(result.operation) {
            SIGN_IN_WITH_EMAIL_LINK -> ActionCodeResult.SignInWithEmailLink
            VERIFY_EMAIL -> ActionCodeResult.VerifyEmail(result.info!!.email)
            PASSWORD_RESET -> ActionCodeResult.PasswordReset(result.info!!.email)
            RECOVER_EMAIL -> (result.info as ActionCodeEmailInfo).run {
                ActionCodeResult.RecoverEmail(email, previousEmail)
            }
            VERIFY_BEFORE_CHANGE_EMAIL -> (result.info as ActionCodeEmailInfo).run {
                ActionCodeResult.VerifyBeforeChangeEmail(email, previousEmail)
            }
            REVERT_SECOND_FACTOR_ADDITION -> (result.info as ActionCodeMultiFactorInfo).run {
                ActionCodeResult.RevertSecondFactorAddition(email, MultiFactorInfo(multiFactorInfo))
            }
            ERROR -> throw UnsupportedOperationException(result.operation.toString())
            else -> throw UnsupportedOperationException(result.operation.toString())
        } as T
    }

    actual fun useEmulator(host: String, port: Int) = jvm.useEmulator(host, port)
}

actual class AuthResult internal constructor(val jvm: com.google.firebase.auth.AuthResult) {
    actual val user: FirebaseUser?
        get() = jvm.user?.let { FirebaseUser(it) }
}

actual class AuthTokenResult(val jvm: com.google.firebase.auth.GetTokenResult) {
//    actual val authTimestamp: Long
//        get() = jvm.authTimestamp
    actual val claims: Map<String, Any>
        get() = jvm.claims
//    actual val expirationTimestamp: Long
//        get() = jvm.expirationTimestamp
//    actual val issuedAtTimestamp: Long
//        get() = jvm.issuedAtTimestamp
    actual val signInProvider: String?
        get() = jvm.signInProvider
    actual val token: String?
        get() = jvm.token
}

internal fun ActionCodeSettings.toAndroid() = com.google.firebase.auth.ActionCodeSettings.newBuilder()
    .setUrl(url)
    .also { jvmPackageName?.run { it.setAndroidPackageName(packageName, installIfNotAvailable, minimumVersion) } }
    .also { dynamicLinkDomain?.run { it.setDynamicLinkDomain(this) } }
    .setHandleCodeInApp(canHandleCodeInApp)
    .also { iOSBundleId?.run { it.setIOSBundleId(this) } }
    .build()

actual typealias FirebaseAuthException = com.google.firebase.auth.FirebaseAuthException
actual typealias FirebaseAuthActionCodeException = com.google.firebase.auth.FirebaseAuthActionCodeException
actual typealias FirebaseAuthEmailException = com.google.firebase.auth.FirebaseAuthEmailException
actual typealias FirebaseAuthInvalidCredentialsException = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
actual typealias FirebaseAuthInvalidUserException = com.google.firebase.auth.FirebaseAuthInvalidUserException
actual typealias FirebaseAuthMultiFactorException = com.google.firebase.auth.FirebaseAuthMultiFactorException
actual typealias FirebaseAuthRecentLoginRequiredException = com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
actual typealias FirebaseAuthUserCollisionException = com.google.firebase.auth.FirebaseAuthUserCollisionException
actual typealias FirebaseAuthWebException = com.google.firebase.auth.FirebaseAuthWebException
