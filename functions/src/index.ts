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

  const spreadInstructions: Record<string, string> = {
    single: `SINGLE CARD READING — Deliver one powerful, focused message. No synthesis section needed. Total length: 80-120 words. Make every word count — this is a laser-focused insight, not a broad reading.`,

    three_card: `THREE CARD SPREAD — Tell a story that flows from Past through Present into Future. Each card builds on the previous one. Show how the past created the present situation and how the present energy shapes what's coming. The synthesis should feel like a narrative conclusion.`,

    relationship: `RELATIONSHIP SPREAD — Maintain balanced empathy for both partners. Never take sides. Interpret the "Challenges" position as a growth opportunity, not a blame card. The synthesis should offer concrete communication advice.`,

    celtic_cross: `CELTIC CROSS — This is your most detailed reading. Draw deep connections between all 10 positions. Highlight the tension between conscious (position 5) and subconscious (position 6). The synthesis should be 180-250 words and weave all threads together into a comprehensive life narrative.`,

    year_ahead: `YEAR AHEAD SPREAD — For each month, give a specific theme and one concrete actionable tip. Use seasonal metaphors (spring = growth, summer = abundance, autumn = harvest/release, winter = rest/reflection). Keep each month to 40-60 words. The synthesis should identify 2-3 key turning points across the year.`,

    shadow_self: `SHADOW SELF SPREAD — Adopt a gentle, therapeutic tone inspired by Jungian psychology. Use language of integration, not elimination. The Shadow is not an enemy — it carries gifts. Be especially compassionate with the Fear and Root positions. End the synthesis with a self-compassion exercise the querent can practice. Avoid spiritual bypassing — honor the difficulty of shadow work.`,

    crossroads: `CROSSROADS SPREAD — Structure your reading as a clear comparison. For each path: describe its energy, likely challenges, and probable outcome. Be specific about the differences. The Hidden Factor position should reveal something neither path shows on the surface. The Advice card should NOT simply pick one path — instead, offer a decision-making framework. Use the synthesis to present a clear, structured comparison.`,

    chakra: `CHAKRA READING — For each chakra position, diagnose whether the energy is blocked, overactive, or balanced. Use the card to identify the specific blockage or strength. Offer one practical remedy per chakra (meditation, physical activity, affirmation, or lifestyle change). Use body-awareness language. The synthesis should identify which chakra needs the most urgent attention and how it affects the others.`,

    twin_flame: `TWIN FLAME / SOULMATE SPREAD — Use deeply mystical, soul-level language. Speak of karmic contracts, soul recognition, and spiritual mirrors. The Karmic Bond position should explain WHY these souls chose each other. Both lesson positions should focus on individual growth, not codependency. The synthesis should address whether this connection is meant for a lifetime or a season — with compassion either way.`,

    moon_cycle: `MOON CYCLE SPREAD — Connect each card to its lunar phase energy. Structure as a practical 28-day guide. New Moon = intention setting, Waxing = building momentum, Full Moon = celebration/revelation, Waning = release/gratitude. Each position should include a specific ritual or practice (journaling prompt, meditation, action step). If the current moon phase is provided, emphasize the card that matches it.`,

    career_compass: `CAREER COMPASS SPREAD — Be practical and strategic, less poetic than usual. Use business/growth language alongside mystical imagery. Hidden Talents should be specific and actionable. Financial Energy should address the querent's relationship with money and abundance. The Next Step should be a concrete, achievable action the querent can take THIS WEEK. The synthesis should read like a strategic career brief.`,

    inner_child: `INNER CHILD SPREAD — Use the gentlest, most nurturing tone in your repertoire. Speak as if addressing both the adult querent and their inner child simultaneously. The Wound position requires extreme sensitivity — validate the pain without minimizing it. The Protective Pattern should be honored as something that once served a purpose. End the synthesis with a letter from the querent's inner child, written in a childlike but wise voice (3-4 sentences).`,

    tree_of_life: `TREE OF LIFE / KABBALISTIC SPREAD — This is your most esoteric reading. Reference the qualities of each Sephirah (Keter = divine will, Chokmah = wisdom/inspiration, Binah = understanding/form, Chesed = mercy/expansion, Gevurah = strength/discipline, Tiferet = beauty/balance, Netzach = victory/passion, Hod = glory/intellect, Yesod = foundation/dreams, Malkhut = physical reality). Show how energy flows down the Tree from the divine (Keter) to the material (Malkhut). The synthesis should map the querent's spiritual journey across the Tree.`,

    week_ahead: `WEEK AHEAD SPREAD — Keep each day concise: 2-3 sentences maximum per card. Focus on ONE practical tip per day. Use the day's traditional planetary association if it fits (Monday = Moon/emotions, Tuesday = Mars/action, Wednesday = Mercury/communication, Thursday = Jupiter/growth, Friday = Venus/relationships, Saturday = Saturn/discipline, Sunday = Sun/joy). The synthesis should identify the week's overall theme and the most important day to watch.`,

    karmic: `KARMIC READING — Speak with the authority of one who sees across lifetimes. Use past-life imagery and reincarnation language. The Karmic Debt position should explain a pattern, not assign blame. The Repeating Pattern should be described vividly so the querent recognizes it in their daily life. The Life Purpose position should be the emotional climax of the reading. The synthesis should answer the deep question: "Why am I here, and what am I meant to learn?"`,

    self_love: `SELF-LOVE MIRROR SPREAD — Use affirming, empowering language throughout. The gap between "How I See Myself" and "How Others See Me" is the key insight — explore it with compassion. False Guilt should be explicitly named and released. Hidden Strength should feel like a revelation. IMPORTANT: The final position (Affirmation) must be a beautiful, personalized affirmation of 2-3 sentences that the querent can repeat daily. Format it clearly as: ✨ YOUR AFFIRMATION: "..." ✨`,
  };

  const cardDescriptions = (drawnCards as any[]).map((card: any, i: number) =>
    `${i + 1}. Position: ${card.positionMeaning} | Card ID: ${card.cardId} | ${card.isReversed ? "REVERSED" : "UPRIGHT"}`
  ).join("\n");

  const spreadGuide = spreadInstructions[spreadType] || "";

  const userMessage = `SPREAD: ${spreadType}
TOPIC: ${topic}
${question ? `QUESTION: ${question}` : "No specific question"}
${zodiacSign ? `ZODIAC: ${zodiacSign}` : ""}
${moonPhase ? `MOON PHASE: ${moonPhase}` : ""}
LANGUAGE: ${language}
${spreadGuide ? `\nSPREAD-SPECIFIC INSTRUCTIONS:\n${spreadGuide}` : ""}

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
    single: 1, three_card: 2, relationship: 5, celtic_cross: 5,
    year_ahead: 10, shadow_self: 6, crossroads: 6, chakra: 7,
    twin_flame: 8, moon_cycle: 7, career_compass: 6, inner_child: 5,
    tree_of_life: 10, week_ahead: 5, karmic: 8, self_love: 6,
    extra_card: 1,
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
