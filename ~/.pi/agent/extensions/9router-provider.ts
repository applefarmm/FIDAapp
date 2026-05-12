import type { ExtensionAPI } from "@mariozechner/pi-coding-agent";

export default async function (pi: ExtensionAPI) {
  let models: any[] = [];

  try {
    const res = await fetch("http://localhost:20128/v1/models", {
      headers: {
        Authorization: `Bearer ${process.env.NINEROUTER_API_KEY ?? ""}`,
      },
    });
    const payload = await res.json() as {
      data: Array<{ id: string; context_window?: number }>
    };
    models = payload.data.map((m) => ({
      id: m.id,
      name: m.id,
      reasoning: false,
      input: ["text"],
      cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 },
      contextWindow: m.context_window ?? 200000,
      maxTokens: 65536,
    }));
  } catch {
    // Fallback hardcoded models using your FIDA prefix
    models = [
      {
        id: "FIDAappDafi",           // your combo name
        name: "FIDAappDafi Combo",
        reasoning: false,
        input: ["text"],
        cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 },
        contextWindow: 200000,
        maxTokens: 65536,
      },
      {
        id: "FIDA/glm-5.1",          // confirmed working from your test
        name: "SumoPod GLM-5.1",
        reasoning: false,
        input: ["text"],
        cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 },
        contextWindow: 200000,
        maxTokens: 65536,
      },
    ];
  }

  pi.registerProvider("9router", {
    name: "9Router (SumoPod + NVIDIA Fallbacks)",
    baseUrl: "http://localhost:20128/v1",
    apiKey: "NINEROUTER_API_KEY",
    api: "openai-completions",
    models,
  });
}