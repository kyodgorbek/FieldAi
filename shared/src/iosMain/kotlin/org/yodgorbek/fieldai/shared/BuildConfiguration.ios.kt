package org.yodgorbek.fieldai.shared

actual object BuildConfiguration {
    // Using environment injection or secure build configurations in production
    actual val GROQ_API_KEY: String = "local_dev_key"
}
