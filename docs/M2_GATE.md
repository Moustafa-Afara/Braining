# M2 GATE — the 60–90 second Arabic test

**Authority:** `ANSWERS.md` Part 1 §1 (the gate itself, mandatory), refined by Part 5 §M2-3
(auto-restart is an allowed mitigation, segment counts must be published). Neither is
superseded by this file — this is the instrument, not the ruling.

**What is being decided.** `SpeechRecognizer` is tuned for short commands. This app's input is
a long spoken Arabic paragraph. If the transcript does not reach the end of the source text,
the default engine changes to **Vosk** (~30 MB, pre-approved — it needs figures, not a new
decision). Everything else about M2 already passed on device, 2026-08-06.

---

## The one criterion

> **Does the transcript reach the end of the source text?**

That is the whole judgement. Two things that are *not* the judgement, and must be recorded
rather than counted against the run:

- **Segment count above 1.** The engine restarting on a pause is an allowed mitigation
  (Part 5 §M2-3). It is reported, never hidden — the gap between stopping and restarting
  swallows words, so a stitched transcript is a weaker result than an unbroken one, and the
  number is how anyone later can tell which they are looking at.
- **Wrong words.** Accuracy is recorded roughly and separately. A transcript that reaches
  sentence 11 with poor accuracy passes this gate and fails a different one.

**If it truncates, record the exact word it stopped at.** Not "it truncated near the end" —
the word. `ANSWERS.md` Part 5: *do not describe truncation as a platform limit.*

---

## Before you start

1. `.\gradlew.bat installDebug` — the build under test must be the current one.
2. **Developer Mode on** (Settings, first card). The gate is judged on numbers it prints.
3. UI language **Arabic**. Confirm the line reads `لغة التعرّف المطلوبة: ar` before recording.
4. Network **on** — the ladder leads with the network model and that is what ships.
5. Somewhere quiet. Read at a normal pace; do not slow down to help the engine, and do not
   avoid your natural pauses. **The pauses are part of the test.**

Read it **three times**, straight through, without stopping to check the screen.

---

## The passage

161 words. At an ordinary reading pace that is **67–77 seconds** — inside the 60–90 second
window the ruling requires. Sentence numbers are for locating a truncation, not for reading
aloud.

> **1.** يبدأ كل مشروع جيّد بفكرة غامضة، لا بخطة واضحة.
>
> **2.** نظنّ أننا نعرف ما نريد، ثم نكتشف عند أول محاولة للشرح أنّ ما في رؤوسنا أقلّ تماسكاً مما ظننّا.
>
> **3.** هذه ليست علامة ضعف، بل هي الطريقة الطبيعية التي يعمل بها التفكير.
>
> **4.** الفرق بين فكرة تنجح وأخرى تتعثّر ليس في لحظة الإلهام الأولى، وإنما في عدد الأسئلة التي طُرحت عليها قبل التنفيذ.
>
> **5.** سؤال واحد في الوقت المناسب قد يوفّر أسبوعين من العمل الضائع، وثلاثة أسئلة قاسية قد تنقذ مشروعاً كاملاً من الفشل.
>
> **6.** لذلك فإنّ أثمن ما يقدّمه المساعد ليس الإجابة السريعة، بل الاستجواب الصبور.
>
> **7.** أن يسألك: ما الذي تحاول حلّه فعلاً؟ ولمن؟ وكيف ستعرف أنك نجحت؟
>
> **8.** كثيرون يهربون من هذه الأسئلة لأنها تكشف أنّ الفكرة لم تكن جاهزة، ويفضّلون البدء بالعمل فوراً ليشعروا بالتقدّم.
>
> **9.** لكن التقدّم في الاتجاه الخطأ ليس تقدّماً، وإنما مسافة إضافية يجب قطعها في طريق العودة.
>
> **10.** حين تنضج الفكرة أخيراً، تصبح صياغتها سهلة إلى حدّ يثير الدهشة.
>
> **11.** أما قبل ذلك، فكلّ صياغة تكون مجرّد تغطية على غموضٍ لم يُحلّ بعد.

**The last word is `بعد`.** A transcript that ends there reached the end. Anything else is a
truncation and needs its stopping word written down.

### Why this passage and not another

It has the shape of what will actually be spoken into this app — a line of reasoning being
worked out, not a shopping list. It has real sentence-boundary pauses (11 of them), including
three question marks in sentence 7 where a reader naturally stops. Those are exactly the
silences that make `SpeechRecognizer` decide the utterance is over, so the passage is
adversarial on purpose rather than by accident.

**One honest limitation.** It is written in MSA, and you speak Syrian Levantine. That makes it
repeatable — the same words three times — but it does not measure how the engine handles your
everyday dialect. The gate is about **truncation**, which does not care about dialect. Accuracy
in real use is a separate question worth a separate, informal test later.

---

## Record for each run

| | Run 1 | Run 2 | Run 3 |
|---|---|---|---|
| Reached the end? (last word `بعد`) | | | |
| If not — the **exact word** it stopped at | | | |
| `مقاطع التفريغ` (segment count) | | | |
| `محرّك التعرّف` (tag · mode) | | | |
| Rough word accuracy | | | |
| Anything odd | | | |

**Copy the transcript out of the input field before starting the next run** — paste it into a
note. The count and the tag can be re-read from the screen; the text cannot.

---

## What each outcome means

| Outcome | Reading |
|---|---|
| Reaches `بعد` all three times | **Gate passes.** `SpeechRecognizer` stays. Publish the three segment counts in `PROJECT_STATE.md` §7 and close M2 |
| Reaches the end but segments are high (say 6+) | Passes, and the number goes on the record. High counts mean the mitigation is doing heavy lifting and words are likely being lost in the gaps — relevant to M3, which builds on this text |
| Truncates in any run | **Gate fails.** Vosk becomes the default. Record the stopping word for each run — where it stops tells us whether it is a silence timeout or a hard length ceiling, and those have different consequences for Vosk's configuration |
| `محرّك التعرّف` shows `على الجهاز` | The run measured the wrong engine. Check the network and redo it — the offline model is not what ships |
| Different tags across runs | Note it. It means the engine is not settling on one rung, which would make the three runs not comparable |

A mixed result — two clean runs and one truncation — is a **fail**, not a two-out-of-three
pass. The product's core interaction cannot work two times in three.

---

# THE DIALECT TEST — the one that matches real use

Added 2026-08-06 after the passage above passed on truncation while natural speech lost roughly
a third of its words. **This is now the more important of the two tests**, and it is the one to
repeat after any change to the speech engine.

## Talking points, not a script — and that is the whole design

**Do not write this out and read it.** Reading is what makes the passage above easy: a reader
stops at full stops, the engine's restarts land in silence, and almost nothing is lost.
Spontaneous speech has no clean gaps, so every restart cuts mid-phrase. A script would measure
the condition the product does not run in.

Speak from the points below in your own words, at your own pace, with your own «يعني» and your
own pauses. **Do not try to be fluent.** These are the owner's own subject from the 2026-08-06
run, so successive tests stay comparable.

> - سلام وسؤال عن الحال
> - عندك فكرة رسالة دكتوراه في مجال البلوكتشين والديب ليرننج
> - بدّك تنفّذها، بس في قيود بتمنعك
> - قيود تقنية — اشرح واحداً منها بالتفصيل
> - قيود مهنية، واجتماعية، واقتصادية
> - ما في جامعة رسمية منتمي إلها
> - وبدّك تنجز الرسالة بشكل سليم مثل أي طالب دكتوراه
> - وبدّك نصيحة، وبدّك تفتح حوار حول الموضوع

Aim for **60–90 seconds**. Add a sentence of your own if you run short — the content is not
what is being measured.

## Record — and the duration is not optional

| | Value |
|---|---|
| **How long you spoke** (stopwatch) | |
| `مقاطع التفريغ` | |
| `محرّك التعرّف` | |
| The transcript, copied out before clearing the field | |

**The duration is the number that was missing on 2026-08-06** and it is what turns a word count
into a loss estimate: 84 words means nothing until you know whether it covered 45 seconds or 90.
Without it the transcript can be read but not measured.

## Reading it

| Signal | Meaning |
|---|---|
| Words per minute near 120–150 | Nothing is being lost. Whatever else is wrong, it is not the seams |
| Words per minute near 60–80 | Roughly half the speech is disappearing into the restart gaps |
| Segments 1–2 | One continuous session — segmented mode is working |
| Segments ~14 for a minute | A restart every few seconds, each one a place where words can vanish |

Judge the text by one question, the same as before: **would someone who was not in the room
understand what you meant?** Not "is every word right" — the transcript is editable by design,
and M3 Clarify is a conversation, not a dictation.
