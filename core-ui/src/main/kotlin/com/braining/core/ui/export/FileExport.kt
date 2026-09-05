package com.braining.core.ui.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Turn a model's answer into a real file the phone can save and share. M6.
 *
 * The model writes the *content*; this owns the *file*. `docs/M6_FILE_GENERATION.md`.
 *
 * **It lives in `core-ui` because chat and Clarify both need it**, and feature modules are siblings
 * (hard constraint 8) — a peer dependency between them compiles right up until the day it does not.
 *
 * **Security.** It writes exactly the text it is handed, into one directory, and exposes that one
 * directory through `FileProvider`. It never reads the key store, never touches a request body, and
 * `file_paths.xml` grants nothing but `cache/exports/`. A key cannot reach a file from here because
 * no path from here leads to a key.
 */
object FileExport {

    /** Markdown: plain text, no library, opens everywhere, and keeps headings, lists and tables. */
    const val EXTENSION = "md"

    /**
     * `text/markdown` when *creating* a document, so the system picker names it `.md`.
     */
    const val MIME_DOCUMENT = "text/markdown"

    /**
     * `text/plain` when *sharing*, on purpose and not by accident. Chat apps filter the share sheet
     * by MIME, and several show no targets at all for `text/markdown`. The file keeps its `.md`
     * name either way; this only decides who is willing to receive it.
     */
    const val MIME_SHARE = "text/plain"

    private const val DIR = "exports"
    private const val MAX_NAME = 60

    /** The first non-blank line, with any leading `#` removed. Empty when there is none. */
    fun titleFrom(content: String): String =
        content.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty().trimStart('#', ' ', '\t').trim()

    /**
     * A name a filesystem will accept. Arabic is kept — it is a filename, not an identifier — while
     * the characters Android and Windows both refuse are replaced rather than dropped, so words do
     * not silently run together.
     */
    fun sanitize(raw: String): String = raw
        .replace(Regex("[\\\\/:*?\"<>|\\r\\n\\t]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(MAX_NAME)
        .trim()
        .trim('.')

    fun fileName(content: String, now: Date = Date()): String {
        val stem = sanitize(titleFrom(content)).ifBlank {
            "braining-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(now)
        }
        return "$stem.$EXTENSION"
    }

    /** Writes UTF-8 into `cache/exports/` and returns a `content://` URI for it. */
    fun writeToCache(context: Context, content: String, fileName: String): Uri {
        val dir = File(context.cacheDir, DIR).apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeText(content, Charsets.UTF_8)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun shareChooser(context: Context, uri: Uri, chooserTitle: String): Intent {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = MIME_SHARE
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, chooserTitle)
    }
}

/**
 * The two actions a screen needs, with the "Save to…" launcher already wired.
 *
 * Both swallow failure rather than crashing: an export is a convenience, and no convenience is
 * worth taking the app down with it (hard constraint — nothing reaching the UI may crash).
 */
class FileExporter internal constructor(
    private val context: Context,
    private val pending: MutableState<String>,
    private val saveLauncher: ManagedActivityResultLauncher<String, Uri?>,
    private val chooserTitle: String,
) {
    fun share(content: String) {
        runCatching {
            val uri = FileExport.writeToCache(context, content, FileExport.fileName(content))
            context.startActivity(FileExport.shareChooser(context, uri, chooserTitle))
        }
    }

    fun save(content: String) {
        runCatching {
            pending.value = content
            saveLauncher.launch(FileExport.fileName(content))
        }
    }
}

@Composable
fun rememberFileExporter(chooserTitle: String): FileExporter {
    val context = LocalContext.current
    val pending = remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(FileExport.MIME_DOCUMENT),
    ) { uri ->
        // Null means the user backed out of the picker. Nothing to do, and not an error.
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pending.value.toByteArray(Charsets.UTF_8))
                }
            }
        }
        pending.value = ""
    }
    return remember(context, launcher, chooserTitle) {
        FileExporter(context, pending, launcher, chooserTitle)
    }
}
