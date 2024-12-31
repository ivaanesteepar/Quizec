package com.example.quizec.utils

import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class AMovServer {
    companion object {
        private const val SERVER = "http://amov.servehttp.com:11111"
        private const val SERVER_UPLOAD = "$SERVER/upload"
        private const val SERVER_DELETE = "$SERVER/delete/"
        private const val SERVER_GET = "$SERVER/file/"

        fun uploadImage(inputStream: InputStream, extension: String): String? {
            val boundary = "Boundary-${System.currentTimeMillis()}"
            val lineEnd = "\r\n"
            val twoHyphens = "--"

            val filename = "file.$extension"
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                ?: "application/octet-stream"

            try {
                Log.d("UploadImage", "Starting image upload with filename: $filename and MIME type: $mimeType")
                val url = URL(SERVER_UPLOAD)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.useCaches = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Connection", "Keep-Alive")
                connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=$boundary"
                )

                val outputStream = DataOutputStream(connection.outputStream)

                outputStream.writeBytes("$twoHyphens$boundary$lineEnd")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$filename\"$lineEnd")
                outputStream.writeBytes("Content-Type: $mimeType$lineEnd")
                outputStream.writeBytes(lineEnd)

                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.writeBytes(lineEnd)

                outputStream.writeBytes("$twoHyphens$boundary$twoHyphens$lineEnd")
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                Log.d("AmovServer", "Response Code: $responseCode")
                val responseMessage = connection.responseMessage
                println("AmovServer Response Code: $responseCode")
                println("AmovServer Response Message: $responseMessage")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    if (response.startsWith("File uploaded successfully:")) {
                        val serverFilename = response.substringAfter(": ").trim()
                        return "$SERVER_GET$serverFilename"
                    }
                }
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        fun asyncUploadImage(inputStream: InputStream, extension: String, onResult: (String?) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = uploadImage(inputStream, extension)
                onResult(result)
            }
        }

        fun deleteFileFromServer(fileName: String, serverUrl: String): Boolean {
            try {
                // Create the full URL for the delete endpoint
                val url = URL("$SERVER_DELETE$fileName")
                Log.d("DeleteFile", "url: $SERVER_DELETE$fileName")
                val connection = url.openConnection() as HttpURLConnection

                // Set request method to DELETE
                connection.requestMethod = "DELETE"
                connection.connectTimeout = 10000 // Timeout in milliseconds
                connection.readTimeout = 10000 // Timeout in milliseconds

                // Get response code and message
                val responseCode = connection.responseCode
                connection.disconnect()

               return (responseCode == HttpURLConnection.HTTP_OK)
            } catch (_: IOException) {
            }

            return false
        }

        fun asyncDeleteFileFromServer(fileName: String, serverUrl: String, onResult: (Boolean) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = deleteFileFromServer(fileName, serverUrl)
                onResult(result)
            }
        }
    }

}