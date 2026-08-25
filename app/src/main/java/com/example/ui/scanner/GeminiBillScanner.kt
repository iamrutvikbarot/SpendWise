package com.example.ui.scanner

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiBillScanner {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeBillImage(bitmap: Bitmap): ExtractedTransaction? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiBillScanner", "GEMINI_API_KEY is not set or placeholder. Falling back to local OCR.")
            return@withContext null
        }

        try {
            // Resize bitmap if very large to optimize upload & processing speed
            val scaledBitmap = if (bitmap.width > 1280 || bitmap.height > 1280) {
                val ratio = minOf(1280f / bitmap.width, 1280f / bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val prompt = """
                Analyze this Indian receipt, utility bill, or UPI payment screenshot (e.g. Amazon Pay, Indane Gas, Google Pay, PhonePe, Paytm, CRED, Electricity, etc.).
                Accurately extract the transaction details as JSON:
                1. "amount": The actual final total paid amount as a single positive float/number (e.g., 949.00). Do NOT extract consumer numbers, mobile numbers (e.g. 7405883187), booking IDs, or order IDs as the amount.
                2. "merchantName": The specific provider or merchant or utility name (e.g. "Indane Gas", "Amazon Pay", "Tata Power", "Swiggy", "Zomato", "Bescom").
                3. "category": One of "Bills", "Food", "Shopping", "Transport", "Health", "Entertainment", "Education", "Other".
                4. "paymentMethod": One of "UPI", "Credit Card", "Debit Card", "Net Banking", "Wallet", "Cash". Include card or bank name if visible (e.g. "Amazon Pay ICICI Credit Card").
                5. "platform": The app or gateway used (e.g. "Amazon Pay", "Google Pay", "PhonePe", "Paytm", "CRED").
                6. "transactionId": Order ID, B-Connect ID, or UPI Ref if available.
                7. "date": Date and time if visible (e.g. "25 Aug 2026").

                Respond ONLY with a valid JSON object in this exact format:
                {
                   "amount": 949.0,
                   "merchantName": "Indane Gas",
                   "category": "Bills",
                   "paymentMethod": "Amazon Pay ICICI Credit Card",
                   "platform": "Amazon Pay",
                   "transactionId": "402-8836400-2047538",
                   "date": "25 Aug 2026"
                }
            """.trimIndent()

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
                put(
                    JSONObject().put(
                        "inlineData",
                        JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Image)
                        }
                    )
                )
            }

            val contentsArray = JSONArray().apply {
                put(JSONObject().put("parts", partsArray))
            }

            val generationConfig = JSONObject().apply {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", generationConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiBillScanner", "Gemini API error code: ${response.code} body: ${response.body?.string()}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            val jsonRoot = JSONObject(responseBody)
            val candidates = jsonRoot.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textOutput = parts?.optJSONObject(0)?.optString("text") ?: return@withContext null

            // Clean json response if wrapped in markdown code blocks
            val cleanedJson = textOutput
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val parsedJson = JSONObject(cleanedJson)
            val parsedAmount = parsedJson.optDouble("amount", 0.0).takeIf { it > 0.0 }
            val parsedMerchant = parsedJson.optString("merchantName").takeIf { it.isNotBlank() }
            val parsedCategory = parsedJson.optString("category", "Bills")
            val parsedPaymentMethod = parsedJson.optString("paymentMethod", "UPI")
            val parsedPlatform = parsedJson.optString("platform", "Digital Receipt")
            val parsedTransactionId = parsedJson.optString("transactionId").takeIf { it.isNotBlank() }
            val parsedDate = parsedJson.optString("date", "Today")

            if (parsedAmount != null && parsedAmount > 0) {
                ExtractedTransaction(
                    amount = parsedAmount,
                    merchantName = parsedMerchant ?: "Scanned Bill",
                    date = parsedDate,
                    upiId = null,
                    transactionId = parsedTransactionId,
                    platform = parsedPlatform,
                    category = parsedCategory,
                    paymentMethod = parsedPaymentMethod,
                    confidenceNotes = "Verified with Gemini AI Vision"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("GeminiBillScanner", "Error analyzing bill with Gemini", e)
            null
        }
    }
}
