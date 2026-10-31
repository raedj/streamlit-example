package com.polyglot.academy.core.sync

/** Platform cryptography (javax.crypto on Android and desktop). */
expect object SyncCrypto {
    fun randomBytes(size: Int): ByteArray
    /** PBKDF2-HMAC-SHA256 → 256-bit key. */
    fun deriveKey(passphrase: String, salt: ByteArray, iterations: Int): ByteArray
    /** AES-256-GCM; returns nonce (12 bytes) followed by ciphertext and tag. */
    fun seal(key: ByteArray, plaintext: ByteArray, associatedData: ByteArray): ByteArray
    /** Throws if the key is wrong or the data was changed. */
    fun open(key: ByteArray, sealed: ByteArray, associatedData: ByteArray): ByteArray
    fun sha256(data: ByteArray): ByteArray
}
