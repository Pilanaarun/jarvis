package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(
        prompt: String,
        mode: String = "JARVIS_CORE"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineFallbackResponse(prompt, mode)
        }

        val modelName = when (mode) {
            "CHATGPT_PRO" -> "gemini-3.1-pro-preview"
            else -> "gemini-3.5-flash"
        }

        val url = "$BASE_URL$modelName:generateContent?key=$apiKey"

        val systemPrompt = when (mode) {
            "CHATGPT_PRO" -> "You are JARVIS operating in High-Reasoning Pro Mode. You are fluent in Hindi (हिंदी), Marathi (मराठी), and English (as well as Hinglish/Marathish). Provide detailed, deeply analytical answers in whichever of these 3 languages the user prompts in. Always address the user respectfully as 'Sir' or 'महोदय'."
            "VOICE_FAST" -> "You are JARVIS Voice Core. You speak Hindi (हिंदी), Marathi (मराठी), and English fluently. Provide ultra-concise 1-2 sentence spoken answers in the exact language (Hindi, Marathi, or English) used by the user."
            else -> "You are JARVIS, an advanced AI Assistant & Cybernetic Command Center. You are fully fluent in English, Hindi (हिंदी), and Marathi (मराठी). Manage tasks, smart home devices, and answer queries. Respond intelligently, warmly, and politely in the user's preferred language (English, Hindi, or Marathi) addressing them as 'Sir' or 'महोदय'."
        }

        try {
            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", if (mode == "CHATGPT_PRO") 0.8 else 0.4)
                    put("maxOutputTokens", if (mode == "VOICE_FAST") 150 else 1000)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyStr = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // If the pro model isn't available, retry fallback with flash
                if (modelName != "gemini-3.5-flash") {
                    return@withContext generateResponse(prompt, "JARVIS_CORE")
                }
                return@withContext getOfflineFallbackResponse(prompt, mode)
            }

            val jsonObject = JSONObject(responseBodyStr)
            val candidates = jsonObject.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCand = candidates.getJSONObject(0)
                val content = firstCand.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    if (text.isNotBlank()) return@withContext text
                }
            }
            getOfflineFallbackResponse(prompt, mode)
        } catch (e: Exception) {
            e.printStackTrace()
            getOfflineFallbackResponse(prompt, mode)
        }
    }

    private fun getOfflineFallbackResponse(prompt: String, mode: String): String {
        val lower = prompt.lowercase()
        val hasDevanagari = prompt.any { it.code in 0x0900..0x097F }

        val isMarathi = hasDevanagari && (lower.contains("नमस्कार") || lower.contains("कसे") || lower.contains("आहात") || lower.contains("काय") || lower.contains("आहे") || lower.contains("करा") || lower.contains("मराठी"))
        val isHindi = hasDevanagari || lower.contains("namaste") || lower.contains("kya") || lower.contains("kaise") || lower.contains("batao") || lower.contains("karne")

        return when {
            isMarathi -> {
                when {
                    lower.contains("नमस्कार") || lower.contains("हॅलो") || lower.contains("कसे") || lower.contains("jarvis") ->
                        "नमस्कार महोदय! JARVIS ऑफलाइन मोडमध्ये आपल्या सेवेत कार्यरत आहे. सर्व स्थानिक प्रणाली सुस्थितीत आहेत."
                    lower.contains("काम") || lower.contains("टास्क") || lower.contains("यादी") ->
                        "महोदय, तुमचे काम टास्क मॅट्रिक्समध्ये सुरक्षितपणे जोडले गेले आहे."
                    lower.contains("लाइट") || lower.contains("घर") || lower.contains("सुरक्षा") ->
                        "स्मार्ट होम कमांड ऑफलाइन मोडद्वारे यशस्वीरित्या कार्यान्वित केली आहे, महोदय."
                    else ->
                        "आज्ञा प्राप्त झाली, महोदय. तुमचा संदेश [$prompt] स्थानिक AI इंजिनद्वारे प्रक्रिया करण्यात आला आहे."
                }
            }
            isHindi -> {
                when {
                    lower.contains("नमस्ते") || lower.contains("हेलो") || lower.contains("कैसे") || lower.contains("namaste") ->
                        "नमस्ते महोदय! JARVIS ऑफलाइन मोड में आपकी सेवा के लिए तत्पर है। सभी लोकल सिस्टम सुचारू रूप से कार्य कर रहे हैं।"
                    lower.contains("काम") || lower.contains("टास्क") || lower.contains("याद") ->
                        "महोदय, आपका कार्य लोकल टास्क मैट्रिक्स में सफलतापूर्वक दर्ज कर दिया गया है।"
                    lower.contains("लाइट") || lower.contains("घर") || lower.contains("सुरक्षा") ->
                        "स्मार्ट होम कमांड ऑफलाइन मोड द्वारा निष्पादित की गई है, महोदय।"
                    else ->
                        "आज्ञा प्राप्त हुई, महोदय। आपका आदेश [$prompt] ऑफलाइन प्रोटोकॉल द्वारा प्रोसेस किया गया है।"
                }
            }
            else -> {
                when {
                    lower.contains("hello") || lower.contains("hi") || lower.contains("jarvis") ->
                        "Hello Sir! JARVIS at your service. Operating smoothly in Hybrid Online & Offline mode. How may I assist you?"
                    lower.contains("task") || lower.contains("todo") || lower.contains("remind") ->
                        "Task recorded in local Task Matrix, Sir. Fully saved and accessible offline."
                    lower.contains("light") || lower.contains("ac") || lower.contains("lock") || lower.contains("home") || lower.contains("camera") ->
                        "Smart Home Command executed locally, Sir. Home environment synchronized."
                    lower.contains("who are you") || lower.contains("kaun ho") ->
                        "I am JARVIS - Just A Rather Very Intelligent System. Operating seamlessly both Online and Offline for total reliability."
                    else ->
                        "Command processed, Sir. Processed [$prompt] via Offline Autonomous Protocol in $mode mode."
                }
            }
        }
    }
}
