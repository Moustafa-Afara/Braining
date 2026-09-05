# L8 — Marketing, and support that runs without a team

## Goal
Two things, again separated because they are built differently:
- **(a)** A complete marketing plan — free and paid — to get Braining known and downloaded.
- **(b)** Technical and operational readiness for **many** downloads without a team: the app must not
  need one, and the questions and support must be handled by agents.

## Part (b) first, because it is mostly already true

**The app itself scales for free.** There is no server, no account system, no database of users, no
API cost borne by the owner — BYOK means every user pays their own provider. A hundred thousand
installs cost the owner exactly what ten did: nothing. This is the single biggest structural advantage
of the design and the marketing should say so (§ messaging below).

**What does not scale by itself is the humans asking questions.** That is solved in three layers:
1. **The guide (L3) and the FAQ**, written to answer the top questions before they are asked. The
   errors section of the guide is the front line.
2. **M8 reports + the aggregate index** — so a problem arrives as a structured file, not a chat
   message, and a pattern across users is visible in one document.
3. **A support agent on Telegram** — a bot in a public Braining channel/group and in direct
   messages: answers from the guide and FAQ, asks for an M8 report when it cannot, and **escalates to
   the owner only with a summary**. Same agent drafts social replies for the owner to approve.
   It runs on a small host (the owner's PC via Tailscale, or a free tier) — *separate from the app*,
   which keeps the app's no-server promise intact.

Deliverables for (b): `docs/FAQ_AR.md` + `FAQ_EN.md`; the Telegram bot (spec + code, own repo or
`tools/support-bot/`); an escalation rule ("owner sees only what the bot could not answer, once a
day, as one message"); the public Telegram channel.

## Part (a) — the marketing plan

### Messaging — what is true and unusual about Braining, in the order it should be said
1. **Arabic-first, by voice.** Not a translation of an English app.
2. **Your keys, your cost, your privacy.** Nothing leaves your phone; there is no company in the
   middle. This is the line that turns the "no server" architecture into a reason to install.
3. **Six brains, one app** — switch providers, fall back when one fails, use your own PC's model.
4. **Clarify** — the app that asks *you* twelve questions before it lets the model answer.
5. **Your PC from anywhere** (after M7) — with the three guardrails stated as the feature.

### Free channels (the launch sequence, in order)
1. **GitHub release + README** polished (screenshots, the one-line promise, the guide link).
2. **Telegram**: a channel for announcements, a group for users; the bot from (b) lives there.
3. **The video (L3)** on YouTube — the owner's own explanation, following the PDF.
4. **Arabic tech communities** — Reddit (r/arabs, r/Android, r/LocalLLaMA for the Ollama angle),
   Arabic developer Discords and Telegram groups, LinkedIn post from the owner's own profile,
   X/Twitter thread in Arabic. One honest post each: what it is, what it costs (nothing), the link.
5. **Product Hunt / Hacker News "Show HN"** — one launch day, prepared: the BYOK/no-server story is
   exactly what those audiences reward.
6. **Arabic tech YouTubers and newsletters** — a short list of ten, a personal message each, the
   APK and the guide attached. Free coverage from three of them beats any ad budget at this stage.

### Paid channels — only after the free ones show what message converts
- Small, measured tests: Meta ads to Arabic-speaking Android users (the largest reach), Google
  App Campaigns once on Play (the only channel that optimises for installs directly), a sponsored
  slot in one Arabic tech newsletter. **Fixed budget per test, one message per test, kill what does
  not convert.** No brand campaigns until retention is known.

### Measurement without surveillance
The app collects nothing, and that stays. Measurement happens **outside** it: GitHub release
download counts, Play Console installs/uninstalls/ratings, Telegram member counts, YouTube views,
UTM-tagged links on the website. Retention is inferred from Play's own anonymised stats.

### Deliverables
`docs/marketing/MESSAGING.md` (the five lines above, AR + EN, with the words to avoid);
`LAUNCH_CALENDAR.md` (week by week for the first eight weeks); `CHANNEL_PLAYBOOK.md` (one page per
channel: audience, post template, what to measure); `PAID_TESTS.md` (budget, message, stop rule);
the FAQ; the bot.

## Steps
1. FAQ + messaging first — they feed everything else. 2. Telegram channel + bot. 3. GitHub/README
polish. 4. Video published. 5. Community posts on a calendar, one per day, not all at once. 6. PH/HN
launch day. 7. Outreach list. 8. First paid test only after step 6's numbers are in.

## What the owner must provide
His public voice — every post goes out in his name, so he approves the templates once; the paid
budget ceiling; whether he wants a **public** Telegram group (moderation load) or channel-only with
DM support.
