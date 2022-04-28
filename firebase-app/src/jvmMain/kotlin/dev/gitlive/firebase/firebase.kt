/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("jvm")
package dev.gitlive.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseException
import java.io.File
import java.io.FileInputStream

actual typealias FirebaseException = FirebaseException

actual typealias FirebaseNetworkException = NetworkException

actual typealias FirebaseTooManyRequestsException = TooManyRequestsException

actual typealias FirebaseApiNotAvailableException = ApiNotAvailableException

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(com.google.firebase.FirebaseApp.getInstance())

actual fun Firebase.app(name: String): FirebaseApp = FirebaseApp(com.google.firebase.FirebaseApp.getInstance(name))

actual fun Firebase.initialize(context: Any?): FirebaseApp? = com.google.firebase.FirebaseApp.initializeApp()?.let { FirebaseApp(it) }

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp =
    FirebaseApp(com.google.firebase.FirebaseApp.initializeApp(options.toJvm(), name))

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions) =
    FirebaseApp(com.google.firebase.FirebaseApp.initializeApp(options.toJvm()))

actual class FirebaseApp internal constructor(val jvm: com.google.firebase.FirebaseApp) {
    actual val name: String
        get() = jvm.name
    actual val options: FirebaseOptions
        get() = jvm.options.run { options }
}

actual fun Firebase.apps(context: Any?): List<FirebaseApp> = com.google.firebase.FirebaseApp.getApps()
    .map { FirebaseApp(it) }

fun FirebaseOptions.toJvm(): com.google.firebase.FirebaseOptions {
    // There are some conflict while importing platform/jvm
    // Would need to remerge to get the CommonFirebaseOptions support
    val serviceFileName = "unsupported-yet.json"
    val serviceFile = FileInputStream(serviceFileName)

    var builder =  com.google.firebase.FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceFile))
    builder = projectId?.let { builder.setProjectId(it) } ?: builder
    //builder = connectTimeout?.let { builder.setConnectTimeout(it) } ?: builder
    //builder = readTimeout?.let { builder.setReadTimeout(it) } ?: builder
    builder = databaseUrl?.let { builder.setDatabaseUrl(it) } ?: builder
    return builder.build()
}