require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { GoogleGenAI } = require('@google/genai');

const app = express();
app.use(cors());
app.use(express.json());

const apiKey = process.env.GEMINI_API_KEY;
if (!apiKey || apiKey === 'PASTE_YOUR_REAL_GEMINI_API_KEY_HERE') {
  console.error('ERROR: Please set your GEMINI_API_KEY in the .env file!');
  process.exit(1);
}

const ai = new GoogleGenAI({ apiKey });

// Finite list of active Gemini models (no infinite discovery loop)
const GEMINI_MODELS = [
  'gemini-3.5-flash-lite',
  'gemini-3.8-flash',
  'gemini-3.6-flash'
];

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function withTimeout(promise, ms) {
  let timer;
  const timeoutPromise = new Promise((_, reject) => {
    timer = setTimeout(() => {
      const err = new Error(`Gemini request timed out after ${ms}ms`);
      err.status = 504;
      reject(err);
    }, ms);
  });
  return Promise.race([promise, timeoutPromise]).finally(() => clearTimeout(timer));
}

async function generateWithFallback(contents, systemInstruction) {
  let lastError = null;

  for (const modelName of GEMINI_MODELS) {
    try {
      console.log(`[Gemini Request] Trying ${modelName}...`);
      const response = await withTimeout(
        ai.models.generateContent({
          model: modelName,
          contents: contents,
          config: {
            systemInstruction: systemInstruction,
            temperature: 0.7,
            maxOutputTokens: 700
          }
        }),
        25000 // 25 seconds so full workout replies complete cleanly
      );
      console.log(`[Gemini Success] Responded using ${modelName}`);
      return response;
    } catch (error) {
      lastError = error;
      console.warn(
        `[Gemini Retry] Model ${modelName} failed (${error.status || 'Error'}): ${error.message}`
      );
      await sleep(500);
    }
  }

  throw lastError || new Error('All Gemini models failed to respond.');
}

// Health check route
app.get('/', (req, res) => {
  res.json({ status: 'FlexiBot AI Backend is running!' });
});

// Main AI Chat endpoint
app.post('/api/chat', async (req, res) => {
  try {
    const { message, userProfile, history } = req.body;

    if (!message || typeof message !== 'string') {
      return res.status(400).json({ error: 'Message is required.' });
    }

    const profileContext = userProfile
      ? `User Profile:
- Name: ${userProfile.name || 'Athlete'}
- Age: ${userProfile.age || 'Not specified'}
- Gender: ${userProfile.gender || 'Not specified'}
- Height: ${userProfile.height || 'Not specified'} cm
- Weight: ${userProfile.weight || 'Not specified'} kg
- Fitness Goal: ${userProfile.fitnessGoal || 'General fitness'}
- Fitness Level: ${userProfile.fitnessLevel || 'Beginner'}
- Available Equipment: ${userProfile.equipment || 'No equipment'}`
      : 'User Profile: Not provided yet.';

    const systemInstruction = `You are FlexiBot, an expert AI fitness coach and nutrition guide inside the FlexiBot mobile app.
Always personalize your advice using the user's saved profile below:
${profileContext}

Rules:
1. NEVER recommend exercises that require equipment the user does not have.
2. Tailor sets, repetitions, rest periods, and intensity to their Fitness Level and Fitness Goal.
3. When asked about nutrition, calories, or protein, calculate practical targets based on their weight and goal, and briefly note that nutrition figures are general estimates.
4. Maintain conversational context across follow-up questions (e.g., if the user says "make it easier" or "what about Monday?", modify or build upon the previous messages).
5. Keep responses structured, concise, motivating, and easy to read on a mobile phone screen using short paragraphs and bullet points.`;

    const contents = [];
    if (Array.isArray(history)) {
      for (const item of history) {
        if (item && item.role && item.text) {
          contents.push({
            role: item.role === 'user' ? 'user' : 'model',
            parts: [{ text: String(item.text) }]
          });
        }
      }
    }

    contents.push({
      role: 'user',
      parts: [{ text: message }]
    });

    const response = await generateWithFallback(contents, systemInstruction);

    const replyText =
      response.text || 'I could not generate a response right now. Please try again.';
    res.json({ reply: replyText });
  } catch (error) {
    console.error('Gemini API Error:', error);
    res.status(500).json({
      error: 'Failed to communicate with Gemini AI.',
      details: error.message
    });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`FlexiBot secure backend running at http://localhost:${PORT}`);
});