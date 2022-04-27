/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase

import kotlinx.coroutines.runBlocking

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = runBlocking { test() }
