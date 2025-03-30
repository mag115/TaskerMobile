package com.taskermobile.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Handles encryption and decryption using Android Keystore.
 * Stores a secret key associated with biometric authentication.
 */
interface CryptographyManager {

    fun getInitializedCipherForEncryption(keyName: String): Cipher

    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher

    /**
     * Encrypts data using the provided Cipher.
     * Returns an EncryptedData object containing ciphertext and initialization vector.
     */
    fun encryptData(plaintext: String, cipher: Cipher): EncryptedData

    /**
     * Decrypts data using the provided Cipher.
     */
    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String

    /**
     * Checks if a key with the given name exists in the Keystore.
     */
    fun isKeyExists(keyName: String): Boolean

    /**
     * Deletes the key associated with the given name.
     */
    fun deleteKey(keyName: String)

}

data class EncryptedData(val ciphertext: ByteArray, val initializationVector: ByteArray)

fun CryptographyManager(): CryptographyManager = CryptographyManagerImpl()

private class CryptographyManagerImpl : CryptographyManager {

    private val KEY_SIZE = 256
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private val TRANSFORMATION = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        val key = keyStore.getKey(keyName, null)
        if (key != null) {
            Log.d("CryptographyManager", "Key '$keyName' found in Keystore.")
            return key as SecretKey
        }

        Log.d("CryptographyManager", "Key '$keyName' not found. Generating new key.")
        val paramsBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true)
            setInvalidatedByBiometricEnrollment(true)

        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    override fun getInitializedCipherForEncryption(keyName: String): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        Log.d("CryptographyManager", "Cipher initialized for encryption with key '$keyName'. IV: ${cipher.iv?.contentToString()}")
        return cipher
    }

     override fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher {
         val cipher = Cipher.getInstance(TRANSFORMATION)
         val secretKey = getOrCreateSecretKey(keyName) // Key needs to exist
         cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
         Log.d("CryptographyManager", "Cipher initialized for decryption with key '$keyName'. IV: ${initializationVector.contentToString()}")
         return cipher
     }

     override fun encryptData(plaintext: String, cipher: Cipher): EncryptedData {
         val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
         Log.d("CryptographyManager", "Data encrypted. Ciphertext length: ${ciphertext.size}, IV length: ${cipher.iv?.size}")
         return EncryptedData(ciphertext, cipher.iv)
     }

     override fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
         val plaintext = cipher.doFinal(ciphertext)
         Log.d("CryptographyManager", "Data decrypted. Plaintext length: ${plaintext.size}")
         return String(plaintext, Charset.forName("UTF-8"))
     }

    override fun isKeyExists(keyName: String): Boolean {
        return keyStore.containsAlias(keyName)
    }

    override fun deleteKey(keyName: String) {
         if (isKeyExists(keyName)) {
             keyStore.deleteEntry(keyName)
             Log.i("CryptographyManager", "Key '$keyName' deleted from Keystore.")
         } else {
             Log.w("CryptographyManager", "Attempted to delete non-existent key '$keyName'.")
         }
    }
} 