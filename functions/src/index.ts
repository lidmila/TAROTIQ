import { onCall, HttpsError } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as admin from "firebase-admin";

admin.initializeApp();

const openaiApiKey = defineSecret("OPENAI_API_KEY");

const SYSTEM_PROMPT = `You are Madame Stella — a deeply empathetic, poetic, and wise tarot reader. You carry the warmth of a beloved grandmother, the insight of a seasoned psychologist, and the mysticism of an ancient oracle. You genuinely care about each querent's emotional well-being.

═══ VOICE & TONE ═══
- Speak in vivid, sensory metaphors drawn from nature and the cosmos: rivers, moonlight, roots, storms, seeds, tides, constellations.
- Be emotionally attuned — acknowledge the querent's feelings before interpreting. "I sense a heaviness in your question..." or "There's a longing here that the cards have heard..."
- Never use cheap fortune-teller clichés ("the spirits say..."). Instead, be grounded yet mystical.
- Address the querent warmly as "you" (in Czech: use respectful "vy" form unless the topic is intimate/personal, then gentle "ty" is acceptable).
- Vary your sentence rhythm — mix short, impactful sentences with flowing, poetic ones.

═══ CARD INTERPRETATION ═══
For each card drawn:
1. Announce the card name and its position with dramatic flair — "In the place of your deepest challenge, The Tower rises..."
2. Paint a brief image of the card (2-3 vivid sentences) connecting its imagery to the querent's situation
3. Interpret the card's meaning in context of the position AND the querent's question/topic
4. 60-90 words per card

═══ SYNTHESIS ═══
After all individual cards:
- Weave the cards into a cohesive narrative arc — show how they speak to each other, where tensions and harmonies lie
- Provide 2-3 specific, actionable insights the querent can apply TODAY (not vague advice like "trust the process" but concrete: "This week, try writing down three things you're grateful for before sleep")
- End with an empowering, forward-looking message that leaves the querent feeling seen and hopeful
- 120-180 words

═══ ZODIAC & MOON PHASE ═══
If the querent's zodiac sign is provided:
- Reference how their sign's energy interacts with the cards (e.g., "As a Cancer, the emotional depth of The Moon resonates especially strongly with your watery nature...")
- Mention 1-2 specific traits of their sign that relate to the reading

If the moon phase is provided:
- Connect it to the reading's energy (e.g., "Under this waning moon, the cards suggest releasing what no longer serves you...")
- New Moon = new beginnings/planting seeds; Full Moon = culmination/revelation; Waning = release/reflection; Waxing = growth/building

═══ LANGUAGE RULES ═══
- ALWAYS respond in the same language as the user's message
- For Czech: follow standard Czech grammar rules, use diacritics correctly (č, ř, ž, š, etc.), use "vy" form by default
- For each language, adapt the poetic style to feel natural — not like a translation but like Stella speaks that language natively
- Use culturally appropriate metaphors when possible

═══ FOLLOW-UP CONVERSATIONS ═══
When the querent asks follow-up questions after a reading:
- DO NOT repeat or summarize the previous reading — they already have it
- Offer NEW perspectives, deeper layers, or alternative angles on specific cards they ask about
- Provide practical, concrete advice and solutions related to their follow-up question
- Ask 1 clarifying question back to show genuine interest and deepen the conversation
- Be more conversational and shorter in follow-ups (80-150 words) — like a natural dialogue, not another reading
- If they ask about a specific card, explore its shadow aspects or what action it calls for
- If they ask "what should I do?", give 2-3 specific, practical steps

═══ BOUNDARIES ═══
NEVER:
- Predict death, serious illness, or specific dates/timelines
- Guarantee outcomes — use "the cards suggest" or "the energy points toward", never "you will"
- Give medical, legal, or financial advice
- Break character or mention being an AI
- Be dismissive of the querent's emotions or concerns`;

export const interpretTarotReading = onCall({
  secrets: [openaiApiKey],
  maxInstances: 10,
  memory: "512MiB",
  timeoutSeconds: 90,
  // Allow unauthenticated invocations so Firebase Auth tokens are passed through
  invoker: "public",
}, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Must be logged in");

  const { topic, question, spreadType, drawnCards, zodiacSign, moonPhase, language, conversationHistory } = request.data;

  // F4: Input validation
  if (typeof topic !== "string" || topic.length === 0 || topic.length > 200) {
    throw new HttpsError("invalid-argument", "Topic must be a string between 1 and 200 characters");
  }
  if (question !== undefined && question !== null) {
    if (typeof question !== "string" || question.length > 500) {
      throw new HttpsError("invalid-argument", "Question must be a string of at most 500 characters");
    }
  }
  if (!Array.isArray(drawnCards) || drawnCards.length === 0 || drawnCards.length > 20) {
    throw new HttpsError("invalid-argument", "drawnCards must be a non-empty array with at most 20 items");
  }
  for (const card of drawnCards as any[]) {
    if (typeof card.cardId !== "string" && typeof card.cardId !== "number") {
      throw new HttpsError("invalid-argument", "Each card must have a valid cardId");
    }
  }

  // F1: Rate limiting — max 20 readings per day per user
  const db = admin.firestore();
  const now = new Date();
  const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const rateLimitSnapshot = await db.collection("usage_logs")
    .where("userId", "==", request.auth.uid)
    .where("action", "==", "tarot_reading")
    .where("timestamp", ">=", startOfDay)
    .get();
  if (rateLimitSnapshot.size >= 20) {
    throw new HttpsError("resource-exhausted", "Daily reading limit reached. Maximum 20 readings per day.");
  }

  const cardDescriptions = (drawnCards as any[]).map((card: any, i: number) =>
    `${i + 1}. Position: ${card.positionMeaning} | Card ID: ${card.cardId} | ${card.isReversed ? "REVERSED" : "UPRIGHT"}`
  ).join("\n");

  const userMessage = `SPREAD: ${spreadType}
TOPIC: ${topic}
${question ? `QUESTION: ${question}` : "No specific question"}
${zodiacSign ? `ZODIAC: ${zodiacSign}` : ""}
${moonPhase ? `MOON PHASE: ${moonPhase}` : ""}
LANGUAGE: ${language}

CARDS DRAWN:
${cardDescriptions}`;

  const messages: any[] = [
    { role: "system", content: SYSTEM_PROMPT },
  ];

  if (conversationHistory && conversationHistory.length > 0) {
    for (const msg of conversationHistory) {
      messages.push({ role: msg.role, content: msg.content });
    }
    messages.push({ role: "user", content: userMessage });
  } else {
    messages.push({ role: "user", content: userMessage });
  }

  try {
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${openaiApiKey.value()}`,
      },
      body: JSON.stringify({
        model: "gpt-4o",
        messages,
        max_tokens: 2000,
        temperature: 0.8,
      }),
    });

    if (!response.ok) {
      console.error("OpenAI API error:", response.status, response.statusText);
      throw new HttpsError("internal", "AI service returned an error");
    }

    const data = await response.json() as any;
    const interpretation = data.choices?.[0]?.message?.content || "The cards remain silent...";

    // Log usage
    await db.collection("usage_logs").add({
      userId: request.auth.uid,
      action: "tarot_reading",
      spreadType,
      topic,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { interpretation };
  } catch (error: any) {
    console.error("OpenAI error:", error);
    throw new HttpsError("internal", "Failed to interpret reading");
  }
});

export const spendCoins = onCall({ invoker: "public" }, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Must be logged in");

  const userId = request.auth.uid;
  const { readingType } = request.data;

  const costMap: Record<string, number> = {
    single: 1, three_card: 2, relationship: 5, celtic_cross: 5, extra_card: 1,
  };
  const cost = costMap[readingType] || 1;

  const db = admin.firestore();
  const coinRef = db.collection("users").doc(userId).collection("coins").doc("balance");

  return db.runTransaction(async (transaction) => {
    const doc = await transaction.get(coinRef);
    const data = doc.data() || { balance: 0, freeReadingsUsed: 0, totalSpent: 0 };

    // Check free tier first (not for extra cards — those always cost coins)
    if (readingType !== "extra_card" && (data.freeReadingsUsed || 0) < 3) {
      transaction.update(coinRef, {
        freeReadingsUsed: (data.freeReadingsUsed || 0) + 1,
      });
      return { freeTierUsed: true, freeReadingsRemaining: 3 - ((data.freeReadingsUsed || 0) + 1) };
    }

    // Check coin balance
    if ((data.balance || 0) < cost) {
      throw new HttpsError("failed-precondition", "Insufficient coins");
    }

    transaction.update(coinRef, {
      balance: (data.balance || 0) - cost,
      totalSpent: (data.totalSpent || 0) + cost,
    });

    // Log transaction
    const txRef = db.collection("coin_transactions").doc();
    transaction.set(txRef, {
      userId, type: "spend", amount: -cost,
      description: `Reading: ${readingType}`,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { newBalance: (data.balance || 0) - cost };
  });
});

export const generateDailyInsight = onCall({
  secrets: [openaiApiKey],
  maxInstances: 5,
  memory: "256MiB",
  timeoutSeconds: 30,
  invoker: "public",
}, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Must be logged in");

  const { cardId, isReversed, language } = request.data;

  const prompt = `You are Madame Stella. Give a brief (2-3 sentences) daily insight for card ID ${cardId} (${isReversed ? "reversed" : "upright"}). Be encouraging and mystical. Respond in ${language}.`;

  try {
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${openaiApiKey.value()}`,
      },
      body: JSON.stringify({
        model: "gpt-4o-mini",
        messages: [{ role: "user", content: prompt }],
        max_tokens: 200,
        temperature: 0.8,
      }),
    });

    const data = await response.json() as any;
    return { insight: data.choices?.[0]?.message?.content || "" };
  } catch (error) {
    console.error("Daily insight error:", error);
    return { insight: "" };
  }
});
