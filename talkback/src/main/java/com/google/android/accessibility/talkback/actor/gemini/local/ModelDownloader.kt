/*
 * Copyright 2026 Backtalk contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.accessibility.talkback.actor.gemini.local

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Downloads a model file, resuming a partial download and checking the result. */
class ModelDownloader(
  private val connect: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection }
) {
  sealed interface Result {
    data object Done : Result

    data object Cancelled : Result

    data class Failed(val failure: ModelFiles.Failure) : Result
  }

  private enum class CopyResult {
    COMPLETE,
    CANCELLED,
    TOO_LARGE,
  }

  /**
   * Downloads [url] to [target]. [onProgress] gets the bytes on disk so far, including a resumed
   * part. Returns [Result.Cancelled] when [isCancelled] turns true, and keeps the partial file so a
   * later call resumes it.
   */
  fun download(
    url: String,
    target: File,
    expectedSize: Long,
    expectedSha256: String,
    onProgress: (Long) -> Unit,
    isCancelled: () -> Boolean,
  ): Result {
    target.parentFile?.mkdirs()
    val part = ModelFiles.partFile(target)
    var have = if (part.isFile) part.length() else 0L
    if (have > expectedSize) {
      part.delete()
      have = 0L
    }
    if (have < expectedSize) {
      val connection = connect(URL(url))
      try {
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        if (have > 0) connection.setRequestProperty("Range", "bytes=$have-")
        val append =
          when (connection.responseCode) {
            HttpURLConnection.HTTP_PARTIAL -> true
            HttpURLConnection.HTTP_OK -> false
            else -> return Result.Failed(ModelFiles.Failure.HTTP)
          }
        if (!append) have = 0L
        when (copy(connection, part, append, have, expectedSize, onProgress, isCancelled)) {
          CopyResult.COMPLETE -> Unit
          CopyResult.CANCELLED -> return Result.Cancelled
          CopyResult.TOO_LARGE -> {
            part.delete()
            return Result.Failed(ModelFiles.Failure.SIZE_MISMATCH)
          }
        }
      } catch (_: IOException) {
        return Result.Failed(ModelFiles.Failure.NETWORK)
      } finally {
        connection.disconnect()
      }
    }
    val failure = ModelFiles.verifyAndInstall(part, target, expectedSize, expectedSha256)
    return if (failure == null) Result.Done else Result.Failed(failure)
  }

  /** Stops before writing beyond [expectedSize]. */
  private fun copy(
    connection: HttpURLConnection,
    part: File,
    append: Boolean,
    startAt: Long,
    expectedSize: Long,
    onProgress: (Long) -> Unit,
    isCancelled: () -> Boolean,
  ): CopyResult {
    var total = startAt
    connection.inputStream.use { input ->
      FileOutputStream(part, append).use { output ->
        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
          if (isCancelled()) return CopyResult.CANCELLED
          val remaining = expectedSize - total
          val readLimit = minOf(buffer.size.toLong(), remaining + 1).toInt()
          val read = input.read(buffer, 0, readLimit)
          if (read < 0) return CopyResult.COMPLETE
          if (read > remaining) return CopyResult.TOO_LARGE
          output.write(buffer, 0, read)
          total += read
          onProgress(total)
        }
      }
    }
  }

  private companion object {
    const val TIMEOUT_MS = 30_000
    const val BUFFER_SIZE = 64 * 1024
  }
}
