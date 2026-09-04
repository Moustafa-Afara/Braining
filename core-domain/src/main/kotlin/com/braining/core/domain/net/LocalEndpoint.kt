package com.braining.core.domain.net

/**
 * Turns what the user typed into a base URL — and **refuses anything that is not on their own
 * network.**
 *
 * ## Why the refusal is the point of this file
 *
 * Every other provider in this app is reached over `https://` at an address compiled into the
 * APK. Ollama is the first that is reached over **plain `http://`**, at an address the user types,
 * and those two facts together are the whole risk: a prompt sent in cleartext to a public address
 * is a prompt anyone on the path can read.
 *
 * Android's own defence — `cleartextTrafficPermitted` — cannot express "private addresses only",
 * because a network security config matches DNS names and cannot do CIDR. So the manifest has to
 * permit cleartext for the app, and **the real guarantee has to live here instead**: this object
 * is the only thing that produces an Ollama base URL, it accepts nothing but a loopback or
 * RFC 1918 / RFC 4193 address, and it is pure Kotlin, so the guarantee is unit-tested rather
 * than asserted.
 *
 * That is a stronger promise than the manifest flag ever made, not a weaker one — a config that
 * permits cleartext to any host and code that will only ever build a private URL beats a config
 * nobody can read and code that would send anywhere.
 *
 * **Permitting cleartext does not weaken the other four providers.** Their URLs are `https://`
 * string constants; a permission to speak plainly is not an instruction to downgrade.
 *
 * ## What the user is allowed to type
 *
 * All of these mean the same machine, because all of them are what people actually write:
 *
 * ```
 * 192.168.1.5                 → http://192.168.1.5:11434
 * 192.168.1.5:11434           → http://192.168.1.5:11434
 * http://192.168.1.5:11434    → http://192.168.1.5:11434
 * http://192.168.1.5:11434/   → http://192.168.1.5:11434
 * ```
 *
 * Rejecting a trailing slash, or demanding the scheme, would be a machine refusing a human for
 * the machine's convenience. `PROJECT_STATE.md` §10 entry 13: a screen that stops on something
 * it could have fixed is a screen that stopped for no reason.
 */
object LocalEndpoint {

    /** Ollama's own default. Applied when the user types an address with no port. */
    const val DEFAULT_PORT = 11434

    sealed interface Result {
        /** [url] has no trailing slash and always carries a scheme, host and port. */
        data class Ok(val url: String, val host: String, val port: Int) : Result

        /** Nothing typed yet. Not an error — the field is simply empty. */
        data object Empty : Result

        /** Typed, but not an address this object can parse. */
        data object Malformed : Result

        /**
         * Parsed, and **refused**: a routable address.
         *
         * Carried as its own case rather than folded into [Malformed] because the two need
         * completely different sentences. "That is not an address" sends the user back to check
         * their typing; this one has to say *why the app will not do it*, or the user will
         * reasonably assume the app is broken and go looking for a workaround.
         */
        data class NotPrivate(val host: String) : Result
    }

    /**
     * Parse and validate [raw].
     *
     * `https://` is accepted and preserved — somebody running Ollama behind a reverse proxy with
     * a certificate has done more work than this app asks for, and refusing them would be
     * gratuitous. The private-address rule still applies: this is a LAN feature.
     */
    /**
     * Names Tailscale issues through MagicDNS, and only Tailscale.
     *
     * **This is why the tunnel case is a hostname rule and not merely a wider IP range.**
     * Tailscale assigns from `100.64.0.0/10` — which is also carrier-grade NAT, the range a
     * *mobile network* hands out, so on cellular those addresses are other subscribers' devices
     * (§10 entry 52, learned the day before this was written). An address in that range cannot
     * tell you which of the two it is.
     *
     * `.ts.net` can. It is Tailscale's own domain, resolved to a machine on the user's own
     * tailnet and nothing else. So the precise permission — "my machine, over an encrypted
     * tunnel" — is expressible as a **name** and not as a number, which is the reverse of the
     * usual situation and worth stating plainly.
     */
    private const val TAILSCALE_SUFFIX = ".ts.net"

    fun parse(raw: String, allowTunnel: Boolean = false): Result {
        val trimmed = raw.trim().trimEnd('/')
        if (trimmed.isEmpty()) return Result.Empty

        val secure = trimmed.startsWith("https://", ignoreCase = true)
        val body = trimmed
            .removePrefix("http://").removePrefix("HTTP://")
            .removePrefix("https://").removePrefix("HTTPS://")
        if (body.isEmpty() || body.contains('/')) return Result.Malformed

        // Split from the RIGHT, once: an IPv6 literal is full of colons and only the last one
        // can be a port separator. `[::1]:11434` has to survive this.
        val host: String
        val port: Int
        val lastColon = body.lastIndexOf(':')
        val closingBracket = body.lastIndexOf(']')
        if (lastColon > closingBracket && lastColon != -1) {
            host = body.substring(0, lastColon)
            port = body.substring(lastColon + 1).toIntOrNull() ?: return Result.Malformed
            if (port !in 1..65535) return Result.Malformed
        } else {
            host = body
            port = DEFAULT_PORT
        }
        if (host.isEmpty()) return Result.Malformed

        // Balanced or not at all. `removePrefix` and `removeSuffix` applied independently
        // accepted `[fd00::1]evil.com` by silently dropping the opening bracket and leaving
        // the rest — the same class of hole as the one above.
        val bare = if (host.startsWith("[") && host.endsWith("]")) {
            host.substring(1, host.length - 1)
        } else {
            host
        }
        if (bare.contains('[') || bare.contains(']')) return Result.Malformed
        if (!isParseable(bare, allowTunnel)) return Result.Malformed
        if (!isPrivate(bare, allowTunnel)) return Result.NotPrivate(bare)

        val scheme = if (secure) "https" else "http"
        return Result.Ok(url = "$scheme://$host:$port", host = bare, port = port)
    }

    /** Convenience for callers that only care whether a stored value is usable. */
    fun baseUrlOrNull(raw: String, allowTunnel: Boolean = false): String? =
        (parse(raw, allowTunnel) as? Result.Ok)?.url

    /**
     * Something this object can reason about: an IPv4 literal, an IPv6 literal, or `localhost`.
     *
     * **Hostnames are deliberately not accepted.** `my-pc.local` would have to be resolved before
     * anyone could know whether it points somewhere private, and this object cannot resolve
     * anything — it is pure. Accepting a name would mean the private-address rule silently stops
     * applying for exactly the inputs where it is hardest to reason about, which is the worst
     * place to have a gap. An IP address is one `ipconfig` away and it is what the setup guide
     * tells the user to read.
     */
    private fun isParseable(host: String, allowTunnel: Boolean): Boolean =
        host.equals("localhost", ignoreCase = true) ||
            isIpv4(host) ||
            isIpv6(host) ||
            (allowTunnel && isTailscaleName(host))

    /**
     * A MagicDNS name on the user's own tailnet.
     *
     * Deliberately strict: the suffix must be exactly [TAILSCALE_SUFFIX] with a real label in
     * front, so `ts.net` alone and `evil-ts.net` are both refused. A hostname is accepted **only**
     * in tunnel mode — outside it the no-hostnames rule stands, because nothing about a name can
     * be checked without resolving it, and this object resolves nothing.
     */
    private fun isTailscaleName(host: String): Boolean {
        val lower = host.lowercase()
        if (!lower.endsWith(TAILSCALE_SUFFIX)) return false
        val label = lower.removeSuffix(TAILSCALE_SUFFIX)
        if (label.isEmpty() || label.startsWith(".") || label.endsWith(".")) return false
        return label.all { it.isLetterOrDigit() || it == '-' || it == '.' }
    }

    /**
     * A real IPv6 literal — **not merely a string containing a colon.**
     *
     * The distinction is the difference between a rule and the appearance of one. [isPrivate]
     * decides the IPv6 case by prefix (`fc`, `fd`, `fe8`…), and while "contains a colon" was the
     * gate, `fd00::1.evil.com` walked straight through it: not an IPv4 address, contains a colon,
     * starts with `fd` — accepted, and the file's own promise that it only ever yields a private
     * address was false. Found in review on 2026-08-31, before it shipped.
     *
     * Requiring every group to be hexadecimal is what kills it: `1.evil` is not hex.
     */
    private fun isIpv6(host: String): Boolean {
        // A zone id (`fe80::1%wlan0`) is a local interface name, not part of the address.
        val h = host.substringBefore('%')
        if (!h.contains(':')) return false
        if (h.count { it == ':' } !in 2..8) return false
        // "::" may appear once. Twice is ambiguous and therefore not an address.
        if (h.indexOf("::") != h.lastIndexOf("::")) return false
        return h.split(':').all { group ->
            group.isEmpty() ||
                (group.length <= 4 && group.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' })
        }
    }

    private fun isIpv4(host: String): Boolean {
        val parts = host.split('.')
        if (parts.size != 4) return false
        return parts.all { p ->
            // "01" is rejected: a leading zero means octal to some resolvers and decimal to
            // others, and an address that means two things is not an address.
            p.isNotEmpty() && p.length <= 3 && p.all(Char::isDigit) &&
                (p.length == 1 || !p.startsWith("0")) &&
                (p.toIntOrNull() ?: 999) in 0..255
        }
    }

    /**
     * The rule the whole file exists for.
     *
     * Loopback · RFC 1918 (`10/8`, `172.16/12`, `192.168/16`) · RFC 3927 link-local
     * (`169.254/16`, which is what two devices negotiate with no DHCP) · RFC 4193 unique-local
     * IPv6 (`fc00::/7`) · IPv6 link-local (`fe80::/10`).
     *
     * **`100.64/10` — carrier-grade NAT — is refused unless `allowTunnel`.** It was briefly
     * allowed outright because Tailscale assigns from it. That was wrong in the way that matters:
     * CGNAT is also what a **mobile carrier** hands out, so on a cellular connection those
     * addresses belong to *other subscribers*, and a mistyped `100.x` would put a prompt in
     * cleartext on a stranger's device.
     *
     * It is reachable now only when the user has affirmed they run Tailscale — and what makes
     * that safe is **not the address**. It is that Tailscale routes those addresses inside a
     * WireGuard tunnel, so the packets on the wire are encrypted whatever this app sends. The
     * preferred spelling remains a `.ts.net` name, which cannot be confused with carrier space
     * at all.
     *
     * Everything else is refused, `0.0.0.0` and public addresses alike.
     */
    private fun isPrivate(host: String, allowTunnel: Boolean): Boolean {
        if (host.equals("localhost", ignoreCase = true)) return true
        if (allowTunnel && isTailscaleName(host)) return true

        // **Only inside the tunnel.** See [TAILSCALE_SUFFIX]: this range is Tailscale's *and*
        // every mobile carrier's, so it opens only when the user has affirmed they run
        // Tailscale — and then the traffic rides inside WireGuard whatever the address means.
        if (allowTunnel && isIpv4(host)) {
            val cgnat = host.split('.').map { it.toInt() }
            if (cgnat[0] == 100 && cgnat[1] in 64..127) return true
        }

        if (isIpv4(host)) {
            val o = host.split('.').map { it.toInt() }
            return when {
                o[0] == 127 -> true
                o[0] == 10 -> true
                o[0] == 192 && o[1] == 168 -> true
                o[0] == 172 && o[1] in 16..31 -> true
                o[0] == 169 && o[1] == 254 -> true
                else -> false
            }
        }

        val v6 = host.lowercase()
        if (v6 == "::1") return true
        // fc00::/7 is fc.. and fd..; fe80::/10 is fe8. fe9. fea. feb.
        return v6.startsWith("fc") || v6.startsWith("fd") ||
            v6.startsWith("fe8") || v6.startsWith("fe9") ||
            v6.startsWith("fea") || v6.startsWith("feb")
    }
}
