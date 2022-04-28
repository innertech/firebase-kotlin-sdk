/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("jvm")
package dev.gitlive.firebase.auth

import kotlinx.coroutines.tasks.await

actual class MultiFactor(val jvm: com.google.firebase.auth.MultiFactor) {
    actual val enrolledFactors: List<MultiFactorInfo>
        get() = jvm.enrolledFactors.map { MultiFactorInfo(it) }
    actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) = jvm.enroll(multiFactorAssertion.android, displayName).await().run { Unit }
    actual suspend fun getSession(): MultiFactorSession = MultiFactorSession(jvm.session.await())
    actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo) = jvm.unenroll(multiFactorInfo.android).await().run { Unit }
    actual suspend fun unenroll(factorUid: String) = jvm.unenroll(factorUid).await().run { Unit }
}

actual class MultiFactorInfo(val jvm: com.google.firebase.auth.MultiFactorInfo) {
    actual val displayName: String?
        get() = jvm.displayName
    actual val enrollmentTime: Double
        get() = jvm.enrollmentTimestamp.toDouble()
    actual val factorId: String
        get() = jvm.factorId
    actual val uid: String
        get() = jvm.uid
}

actual class MultiFactorAssertion(val jvm: com.google.firebase.auth.MultiFactorAssertion) {
    actual val factorId: String
        get() = jvm.factorId
}

actual class MultiFactorSession(val jvm: com.google.firebase.auth.MultiFactorSession)

actual class MultiFactorResolver(val jvm: com.google.firebase.auth.MultiFactorResolver) {
    actual val auth: FirebaseAuth = FirebaseAuth(jvm.firebaseAuth)
    actual val hints: List<MultiFactorInfo> = jvm.hints.map { MultiFactorInfo(it) }
    actual val session: MultiFactorSession = MultiFactorSession(jvm.session)

    actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = AuthResult(jvm.resolveSignIn(assertion.android).await())
}

 */