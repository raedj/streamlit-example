package com.polyglot.academy.core

import com.polyglot.academy.core.sync.SyncEnvelope
import com.polyglot.academy.core.sync.SyncError
import com.polyglot.academy.core.sync.WebDavRemote
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyncTest {
    @Test fun envelopeEncryptsAndRejectsWrongPassphraseOrTampering() {
        val secret = """{"format":"x","profile":{"displayName":"Omar"}}"""
        val file = SyncEnvelope.seal(secret, "correct horse battery", iterations = 20_000)
        val text = file.decodeToString()
        assertFalse("Omar" in text, "learning data is not readable on the server")
        assertTrue("PBKDF2-HMAC-SHA256" in text && "AES-256-GCM" in text)
        assertEquals(secret, SyncEnvelope.open(file, "correct horse battery"))
        assertFailsWith<SyncError.WrongPassphrase> { SyncEnvelope.open(file, "wrong passphrase!") }
        val tampered = text.replace(Regex("\"data\":\"(.)")) { m -> "\"data\":\"" + (if (m.groupValues[1] == "A") "B" else "A") }.encodeToByteArray()
        assertFailsWith<SyncError.WrongPassphrase> { SyncEnvelope.open(tampered, "correct horse battery") }
        assertFailsWith<SyncError.NotASyncFile> { SyncEnvelope.open("hello".encodeToByteArray(), "x") }
        assertFailsWith<SyncError.NewerFormat> { SyncEnvelope.open(text.replace("\"version\":1", "\"version\":9").encodeToByteArray(), "x") }
        assertTrue(SyncEnvelope.seal(secret, "correct horse battery", 20_000).decodeToString() != text, "fresh salt and nonce each time")
    }

    @Test fun fingerprintIgnoresExportTimeAndDeviceSettings() {
        val a = """{"exportedAt":1,"settings":{"theme":"DARK"},"attempts":[1,2]}"""
        val b = """{"exportedAt":2,"settings":{"theme":"LIGHT"},"attempts":[1,2]}"""
        assertEquals(SyncEnvelope.fingerprint(a), SyncEnvelope.fingerprint(b))
        assertTrue(SyncEnvelope.fingerprint(a) != SyncEnvelope.fingerprint("""{"exportedAt":1,"attempts":[1,2,3]}"""))
    }

    @Test fun onlyHttpsOrLocalNetworkServersAreAccepted() {
        assertTrue(WebDavRemote.isAllowed(Url("https://cloud.example.com/remote.php/dav/files/me")))
        assertTrue(WebDavRemote.isAllowed(Url("http://192.168.1.20:8080/dav")))
        assertTrue(WebDavRemote.isAllowed(Url("http://nas.local/dav")))
        assertFalse(WebDavRemote.isAllowed(Url("http://cloud.example.com/dav")))
        assertFalse(WebDavRemote.isAllowed(Url("ftp://192.168.1.2/")))
        assertFailsWith<SyncError.InsecureUrl> { WebDavRemote(HttpClient(MockEngine { respond("") }), "http://example.com/dav", "u", "p") }
    }

    @Test fun webDavReadsWritesAndDetectsConflicts() = runTest { withContext(Dispatchers.Default) {
        val log = mutableListOf<String>()
        var stored: ByteArray? = null
        val client = HttpClient(MockEngine { req ->
            log += "${req.method.value} ${req.url.encodedPath} ${req.headers["If-Match"] ?: req.headers["If-None-Match"] ?: ""}".trim()
            assertTrue(req.headers["Authorization"]!!.startsWith("Basic "))
            when (req.method.value) {
                "GET" -> stored?.let { respond(it, HttpStatusCode.OK, headersOf("ETag", "\"e1\"")) } ?: respond("", HttpStatusCode.NotFound)
                "MKCOL" -> respond("", HttpStatusCode.MethodNotAllowed)
                "PUT" -> if (req.headers["If-Match"] == "\"stale\"") respond("", HttpStatusCode.PreconditionFailed)
                    else { stored = (req.body as io.ktor.http.content.OutgoingContent.ByteArrayContent).bytes(); respond("", HttpStatusCode.Created, headersOf("ETag", "\"e1\"")) }
                else -> respond("", HttpStatusCode.BadRequest)
            }
        })
        val dav = WebDavRemote(client, "https://dav.example.com/files/me/", "me", "pw")
        assertNull(dav.read())
        assertEquals("\"e1\"", dav.write("abc".encodeToByteArray(), null, exists = false))
        assertEquals("abc", dav.read()!!.bytes.decodeToString())
        assertFailsWith<SyncError.Changed> { dav.write("x".encodeToByteArray(), "\"stale\"", exists = true) }
        assertEquals(listOf("GET /files/me/polyglot-academy/sync.json", "MKCOL /files/me/polyglot-academy/", "PUT /files/me/polyglot-academy/sync.json *",
            "GET /files/me/polyglot-academy/sync.json", "PUT /files/me/polyglot-academy/sync.json \"stale\""), log)
        val denied = WebDavRemote(HttpClient(MockEngine { respond("", HttpStatusCode.Unauthorized) }), "https://dav.example.com", "me", "bad")
        assertFailsWith<SyncError.Authentication> { denied.read() }
    } }
}
