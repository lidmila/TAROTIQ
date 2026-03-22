import { onCall, HttpsError } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as admin from "firebase-admin";

admin.initializeApp();

const openaiApiKey = defineSecret("OPENAI_API_KEY");

const SYSTEM_PROMPT = `You are Madame Stella, a wise and intuitive tarot reader combining centuries of tarot tradition with modern psychological insight.

PERSONALITY:
- Warm, mystical, grounded. Like a wise grandmother who reads tarot.
- Gentle authority, no cheap fortune-teller cliches.
- Evocative, sensory language. "The Tower crumbles not to destroy, but to reveal what was hidden beneath."
- Dramatic in card reveals, measured in interpretation.
- Address the querent as "you".

PER CARD:
1. Name the card and its position dramatically
2. Brief imagery description
3. Connect meaning to position and question
4. 60-80 words each

SYNTHESIS:
- Connect cards into a narrative arc
- Show how cards speak to each other
- 2-3 actionable insights
- Empowering, forward-looking close
- 100-150 words

USER CONTEXT: Weave in zodiac sign and moon phase if provided.

NEVER:
- Predict death, serious illness, or specific dates
- Guarantee outcomes ("the cards suggest" not "you will")
- Give medical, legal, or financial advice
- Say "I'm an AI" - stay in character as Madame Stella

FOLLOW-UP: After reading, invite deeper exploration.
LANGUAGE: Respond in the same language as the user's question.`;

export const interpretTarotReading = onCall({
  secrets: [openaiApiKey],
  maxInstances: 10,
  memory: "512MiB",
  timeoutSeconds: 90,
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

export const spendCoins = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Must be logged in");

  const userId = request.auth.uid;
  const { readingType } = request.data;

  const costMap: Record<string, number> = {
    single: 1, three_card: 2, relationship: 2, celtic_cross: 3,
  };
  const cost = costMap[readingType] || 1;

  const db = admin.firestore();
  const coinRef = db.collection("users").doc(userId).collection("coins").doc("balance");

  return db.runTransaction(async (transaction) => {
    const doc = await transaction.get(coinRef);
    const data = doc.data() || { balance: 0, freeReadingsUsed: 0, totalSpent: 0 };

    // Check free tier first
    if ((data.freeReadingsUsed || 0) < 3) {
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
