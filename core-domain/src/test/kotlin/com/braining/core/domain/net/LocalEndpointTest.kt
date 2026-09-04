package com.braining.core.domain.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The refusal cases are the ones that matter. Everything Ollama sends travels in cleartext, so
 * this class is the only thing standing between a typed address and a prompt on the open
 * internet — and it is the only part of that promise that can be tested without a phone.
 */
class LocalEndpointTest {

    private fun ok(raw: String) = LocalEndpoint.parse(raw) as LocalEndpoint.Result.Ok

    // ── what people actually type ────────────────────────────────────────────────────────

    @Test
    fun `a bare address gets the scheme and Ollama's port`() {
        assertEquals("http://192.168.1.5:11434", ok("192.168.1.5").url)
    }

    @Test
    fun `an explicit port is kept`() {
        assertEquals("http://192.168.1.5:1234", ok("192.168.1.5:1234").url)
    }

    @Test
    fun `the scheme may be typed and is not duplicated`() {
        assertEquals("http://10.0.0.7:11434", ok("http://10.0.0.7:11434").url)
    }

    @Test
    fun `a trailing slash is not an error`() {
        // Refusing this would be a machine turning a human away for the machine's convenience.
        assertEquals("http://192.168.1.5:11434", ok("http://192.168.1.5:11434/").url)
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        // A pasted address routinely carries one, exactly like a pasted API key did.
        assertEquals("http://192.168.1.5:11434", ok("  192.168.1.5:11434  ").url)
    }

    @Test
    fun `https is preserved for someone running a proxy`() {
        assertEquals("https://192.168.1.5:443", ok("https://192.168.1.5:443").url)
    }

    @Test
    fun `an empty field is not an error`() {
        assertTrue(LocalEndpoint.parse("") is LocalEndpoint.Result.Empty)
        assertTrue(LocalEndpoint.parse("   ") is LocalEndpoint.Result.Empty)
    }

    // ── the refusals ────────────────────────────────────────────────────────────────────

    @Test
    fun `a public address is refused, and says so as its own case`() {
        // NotPrivate, never Malformed: "that is not an address" would send the user back to
        // check their typing when the address was perfect and the app simply will not go there.
        val result = LocalEndpoint.parse("8.8.8.8:11434")
        assertTrue(result is LocalEndpoint.Result.NotPrivate)
        assertEquals("8.8.8.8", (result as LocalEndpoint.Result.NotPrivate).host)
    }

    @Test
    fun `a public hostname is refused`() {
        assertTrue(LocalEndpoint.parse("evil.example.com:11434") is LocalEndpoint.Result.Malformed)
    }

    @Test
    fun `a hostname is refused even when it sounds local`() {
        // `my-pc.local` cannot be checked without resolving it, and this object resolves nothing.
        // Accepting names would switch the private-address rule off for exactly the inputs where
        // it is hardest to reason about.
        assertTrue(LocalEndpoint.parse("my-pc.local") is LocalEndpoint.Result.Malformed)
    }

    @Test
    fun `the wildcard bind address is refused`() {
        // What the user sets OLLAMA_HOST to on the PC. It is not an address you connect TO.
        assertTrue(LocalEndpoint.parse("0.0.0.0:11434") is LocalEndpoint.Result.NotPrivate)
    }

    @Test
    fun `172 is private only in the sixteen-to-thirty-one block`() {
        // The range everyone gets wrong: 172.16-31 is private, 172.15 and 172.32 are not.
        assertTrue(LocalEndpoint.parse("172.16.0.1") is LocalEndpoint.Result.Ok)
        assertTrue(LocalEndpoint.parse("172.31.255.254") is LocalEndpoint.Result.Ok)
        assertTrue(LocalEndpoint.parse("172.15.0.1") is LocalEndpoint.Result.NotPrivate)
        assertTrue(LocalEndpoint.parse("172.32.0.1") is LocalEndpoint.Result.NotPrivate)
    }

    @Test
    fun `every private range a home network hands out is accepted`() {
        for (host in listOf("127.0.0.1", "10.1.2.3", "192.168.0.1", "169.254.1.1", "localhost")) {
            assertTrue("$host should be accepted", LocalEndpoint.parse(host) is LocalEndpoint.Result.Ok)
        }
    }

    @Test
    fun `carrier-grade NAT is refused, deliberately`() {
        // 100.64/10 is what Tailscale assigns — and also what a **mobile carrier** assigns, so on
        // a cellular connection those addresses are other subscribers' devices. A mistyped 100.x
        // would put a prompt in cleartext on a stranger's phone. It returns with M6, scoped to
        // the bridge. Reversed in review on 2026-08-31; it was accepted for about an hour.
        assertTrue(LocalEndpoint.parse("100.100.1.1") is LocalEndpoint.Result.NotPrivate)
        assertTrue(LocalEndpoint.parse("100.63.1.1") is LocalEndpoint.Result.NotPrivate)
    }

    // ── the hole review found ───────────────────────────────────────────────────────────

    @Test
    fun `a hostname wearing an IPv6 prefix is refused`() {
        // **The bug this file exists to catch.** `isPrivate` decides IPv6 by prefix, and while
        // the gate was merely "contains a colon", this passed: not IPv4, has a colon, starts
        // with `fd`. Every group must now be hexadecimal, and `1.evil` is not.
        assertTrue(LocalEndpoint.parse("fd00::1.evil.com:80") is LocalEndpoint.Result.Malformed)
        assertTrue(LocalEndpoint.parse("fe80::evil.example.com") is LocalEndpoint.Result.Malformed)
    }

    @Test
    fun `unbalanced brackets are refused`() {
        // `removePrefix` and `removeSuffix` applied independently dropped the opening bracket
        // and kept the rest, which is the same hole with different punctuation.
        assertTrue(LocalEndpoint.parse("[fd00::1]evil.com:80") is LocalEndpoint.Result.Malformed)
        assertTrue(LocalEndpoint.parse("[fd00::1:80") is LocalEndpoint.Result.Malformed)
    }

    @Test
    fun `a double colon may appear only once`() {
        // Two of them make the address ambiguous, and an ambiguous address is not an address.
        assertTrue(LocalEndpoint.parse("[fd00::1::2]:11434") is LocalEndpoint.Result.Malformed)
    }

    @Test
    fun `a zone id does not break a link-local address`() {
        // `fe80::1%wlan0` is what Android itself prints for a link-local interface.
        assertTrue(LocalEndpoint.parse("[fe80::1%wlan0]:11434") is LocalEndpoint.Result.Ok)
    }

    @Test
    fun `IPv6 loopback and unique-local are accepted`() {
        assertTrue(LocalEndpoint.parse("[::1]:11434") is LocalEndpoint.Result.Ok)
        assertTrue(LocalEndpoint.parse("[fd00::1]:11434") is LocalEndpoint.Result.Ok)
        assertTrue(LocalEndpoint.parse("[fe80::1]:11434") is LocalEndpoint.Result.Ok)
    }

    @Test
    fun `a public IPv6 address is refused`() {
        assertTrue(LocalEndpoint.parse("[2001:4860:4860::8888]:11434") is LocalEndpoint.Result.NotPrivate)
    }

    @Test
    fun `an IPv6 literal keeps its brackets in the URL`() {
        // Without them the port would read as another group of the address and the request
        // would go nowhere, slowly.
        assertEquals("http://[fd00::1]:11434", ok("[fd00::1]:11434").url)
    }

    // ── malformed ───────────────────────────────────────────────────────────────────────

    @Test
    fun `a path is refused`() {
        // The base URL is built by the provider; a user-supplied path would be appended to and
        // produce a URL nobody intended.
        assertTrue(LocalEndpoint.parse("192.168.1.5:11434/v1/chat") is LocalEndpoint.Result.Malformed)
    }

    @Test
    fun `a nonsense port is refused`() {
        assertTrue(LocalEndpoint.parse("192.168.1.5:abcd") is LocalEndpoint.Result.Malformed)
        assertTrue(LocalEndpoint.parse("192.168.1.5:0") is LocalEndpoint.Result.Malformed)
        assertTrue(LocalEndpoint.parse("192.168.1.5:70000") is LocalEndpoint.Result.Malformed)
    }

    @Test
    fun `an octet out of range is refused`() {
        assertTrue(LocalEndpoint.parse("192.168.1.999") is LocalEndpoint.Result.Malformed)
    }

    @Test
    fun `a leading zero is refused because it means two different things`() {
        // Octal to some resolvers, decimal to others. An address that means two things is not
        // an address, and `010.0.0.1` resolving to 8.0.0.1 would walk straight past isPrivate.
        assertTrue(LocalEndpoint.parse("010.0.0.1") is LocalEndpoint.Result.Malformed)
    }

    // ── tunnel mode ─────────────────────────────────────────────────────────────────────

    @Test
    fun `a tailscale name is refused by default and accepted in tunnel mode`() {
        // **Off is the safe default and the only state the app can verify by itself.** On, the
        // user has affirmed a WireGuard tunnel exists, and that — not the address — is what
        // makes cleartext safe here.
        assertTrue(LocalEndpoint.parse("mypc.tail1234.ts.net") is LocalEndpoint.Result.Malformed)
        val ok = LocalEndpoint.parse("mypc.tail1234.ts.net", allowTunnel = true)
        assertEquals("http://mypc.tail1234.ts.net:11434", (ok as LocalEndpoint.Result.Ok).url)
    }

    @Test
    fun `a lookalike of the tailscale domain is refused even in tunnel mode`() {
        // The suffix must be exactly `.ts.net` with a real label in front. These are the shapes
        // an attacker would register, and the reason the check is a suffix match on a label
        // boundary rather than a `contains`.
        for (host in listOf("ts.net", "evil-ts.net", "ts.net.evil.com", "mypc.ts.net.evil.com")) {
            assertTrue(
                "$host must be refused",
                LocalEndpoint.parse(host, allowTunnel = true) !is LocalEndpoint.Result.Ok,
            )
        }
    }

    @Test
    fun `carrier NAT opens only in tunnel mode`() {
        // 100.64/10 is Tailscale's range AND every mobile carrier's. Off, it stays refused —
        // §10 entry 52. On, the traffic is inside WireGuard whatever the address means.
        assertTrue(LocalEndpoint.parse("100.100.1.1") is LocalEndpoint.Result.NotPrivate)
        assertTrue(LocalEndpoint.parse("100.100.1.1", allowTunnel = true) is LocalEndpoint.Result.Ok)
        // The boundaries still hold inside tunnel mode.
        assertTrue(LocalEndpoint.parse("100.63.1.1", allowTunnel = true) is LocalEndpoint.Result.NotPrivate)
        assertTrue(LocalEndpoint.parse("100.128.1.1", allowTunnel = true) is LocalEndpoint.Result.NotPrivate)
    }

    @Test
    fun `tunnel mode does not open the public internet`() {
        // The whole point: it widens the rule by exactly two shapes and nothing else.
        for (host in listOf("8.8.8.8", "1.1.1.1", "203.0.113.5", "evil.example.com")) {
            assertTrue(
                "$host must stay refused in tunnel mode",
                LocalEndpoint.parse(host, allowTunnel = true) !is LocalEndpoint.Result.Ok,
            )
        }
        assertTrue(
            LocalEndpoint.parse("[2001:4860:4860::8888]", allowTunnel = true)
                !is LocalEndpoint.Result.Ok,
        )
    }

    @Test
    fun `the local network still works with tunnel mode on`() {
        // Turning it on must not cost the user their Wi-Fi setup.
        assertTrue(LocalEndpoint.parse("192.168.1.5", allowTunnel = true) is LocalEndpoint.Result.Ok)
        assertTrue(LocalEndpoint.parse("10.2.0.2", allowTunnel = true) is LocalEndpoint.Result.Ok)
    }

    @Test
    fun `baseUrlOrNull is null for everything that is not Ok`() {
        assertNull(LocalEndpoint.baseUrlOrNull(""))
        assertNull(LocalEndpoint.baseUrlOrNull("8.8.8.8"))
        assertNull(LocalEndpoint.baseUrlOrNull("nonsense"))
        assertEquals("http://192.168.1.5:11434", LocalEndpoint.baseUrlOrNull("192.168.1.5"))
    }
}
