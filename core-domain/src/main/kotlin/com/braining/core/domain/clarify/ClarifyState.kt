package com.braining.core.domain.clarify

import kotlinx.serialization.Serializable

/**
 * The CLARIFY state machine.
 *
 * These five names are **not a design choice made here**. `ANSWERS.md` Part 2 §5 approved them
 * by name — "strongly approved — this is the heart of the product and must be deterministic and
 * testable" — so they are transcribed rather than reinvented, and `docs/M3_DESIGN_NOTE.md` §3.4
 * records that the question is closed and is not to be reopened.
 *
 * The order below is the legal order. The only transition that skips is
 * [AWAITING_USER_DECISION] → [READY], which no code path may take on its own: it belongs to
 * `ClarifyEngine.declareReady`, and `BRAINING.md` §2.3 makes the user the only one who may
 * decide an idea has matured.
 *
 * **`@Serializable` since M5, and the condition it waited for has been met.** Until 2026-08-28
 * this KDoc said the annotation was withheld because it would serve a persistence layer that did
 * not exist. Room now exists, `SessionRecord.turns` is stored as JSON, and the schema is real
 * rather than guessed — so the annotation is added against it, exactly as that paragraph
 * promised. `ANSWERS.md` Part 7 §M3-4 is untouched by this: an **interrupted** interrogation
 * still does not survive the process. What M5 persists is a **finished** run.
 */
@Serializable
enum class ClarifyState {

    /** The idea has been sent and no token has come back yet. */
    ANALYZING,

    /** A turn is streaming and it is a question. */
    ASKING,

    /** A turn is streaming and it is a suggestion or a caveat. */
    SUGGESTING,

    /**
     * A turn finished. The engine is idle and the user may answer, or declare the idea mature.
     *
     * **This is where the machine rests, and that is the design.** An engine that decided for
     * itself when enough had been asked would be deciding the one thing `BRAINING.md` §2.3
     * reserves for the user.
     */
    AWAITING_USER_DECISION,

    /** The user declared the idea mature. FORGE may run; Clarify asks nothing further. */
    READY,
}
