/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("jvm")
package dev.gitlive.firebase.auth

import android.net.Uri
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

actual class FirebaseUser internal constructor(val jvm: com.google.firebase.auth.FirebaseUser) {
    actual val uid: String
        get() = jvm.uid
    actual val displayName: String?
        get() = jvm.displayName
    actual val email: String?
        get() = jvm.email
    actual val phoneNumber: String?
        get() = jvm.phoneNumber
    actual val photoURL: String?
        get() = jvm.photoUrl?.toString()
    actual val isAnonymous: Boolean
        get() = jvm.isAnonymous
    actual val isEmailVerified: Boolean
        get() = jvm.isEmailVerified
    actual val metaData: UserMetaData?
        get() = jvm.metadata?.let{ UserMetaData(it) }
    actual val multiFactor: MultiFactor
        get() = MultiFactor(jvm.multiFactor)
    actual val providerData: List<UserInfo>
        get() = jvm.providerData.map { UserInfo(it) }
    actual val providerId: String
        get() = jvm.providerId
    actual suspend fun delete() = jvm.delete().await().run { Unit }
    actual suspend fun reload() = jvm.reload().await().run { Unit }
    actual suspend fun getIdToken(forceRefresh: Boolean): String? = jvm.getIdToken(forceRefresh).await().token
    actual suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult = jvm.getIdToken(forceRefresh).await().run { AuthTokenResult(this) }
    actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult(jvm.linkWithCredential(credential.android).await())
    actual suspend fun reauthenticate(credential: AuthCredential) = jvm.reauthenticate(credential.android).await().run { Unit }
    actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = AuthResult(jvm.reauthenticateAndRetrieveData(credential.android).await())
    actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?) {
        val request = actionCodeSettings?.let { jvm.sendEmailVerification(it.toAndroid()) } ?: jvm.sendEmailVerification()
        request.await()
    }
    actual suspend fun unlink(provider: String): FirebaseUser? = jvm.unlink(provider).await().user?.let { FirebaseUser(it) }
    actual suspend fun updateEmail(email: String) = jvm.updateEmail(email).await().run { Unit }
    actual suspend fun updatePassword(password: String) = jvm.updatePassword(password).await().run { Unit }
    actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential) = jvm.updatePhoneNumber(credential.android).await().run { Unit }
    actual suspend fun updateProfile(displayName: String?, photoUrl: String?) {
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUri(photoUrl?.let { Uri.parse(it) })
            .build()
        jvm.updateProfile(request).await()
    }
    actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?) =
        jvm.verifyBeforeUpdateEmail(newEmail, actionCodeSettings?.toAndroid()).await().run { Unit }
}

actual class UserInfo(val jvm: com.google.firebase.auth.UserInfo) {
    actual val displayName: String?
        get() = jvm.displayName
    actual val email: String?
        get() = jvm.email
    actual val phoneNumber: String?
        get() = jvm.phoneNumber
    actual val photoURL: String?
        get() = jvm.photoUrl?.toString()
    actual val providerId: String
        get() = jvm.providerId
    actual val uid: String
        get() = jvm.uid
}

actual class UserMetaData(val jvm: com.google.firebase.auth.FirebaseUserMetadata) {
    actual val creationTime: Double?
        get() = jvm.creationTimestamp.toDouble()
    actual val lastSignInTime: Double?
        get() = jvm.lastSignInTimestamp.toDouble()
}
