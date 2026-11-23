import json
import os
import re

from fastapi import FastAPI

app = FastAPI(title="OpsPilot AI Service")

PROVIDER = os.getenv("LLM_PROVIDER", "mock")   # "mock" or "anthropic"
MODEL = os.getenv("ANTHROPIC_MODEL", "claude-haiku-5-5")


def build_prompt(incident: dict) -> str:
    lines = [f"- [{e.get('type')}] {e.get('message')}"
             for e in (incident.get("timeline") or [])]
    return f"""You are an experienced site reliability engineer.
Service: {incident.get('service')}
Title: {incident.get('title')}
Severity: {incident.get('severity')}
Peak errors per minute: {incident.get('peakErrorsPerMin')}

Timeline and evidence:
{chr(10).join(lines)}

Reply with ONLY valid JSON in exactly this shape:
{{"summary": "2-3 plain-English sentences",
  "likelyCauses": ["most likely first", "..."],
  "steps": ["concrete troubleshooting step", "..."]}}
Use only the evidence above. If unsure, say so."""


def analyze_with_claude(incident: dict) -> dict:
    import anthropic
    client = anthropic.Anthropic()   # reads ANTHROPIC_API_KEY
    msg = client.messages.create(
        model=MODEL, max_tokens=1000,
        messages=[{"role": "user", "content": build_prompt(incident)}],
    )
    text = msg.content[0].text.strip()
    text = re.sub(r"^```(?:json)?|```$", "", text, flags=re.MULTILINE).strip()
    return json.loads(text)


RULES = [
    ("timeout", "A dependency (like the payment gateway) is slow or unreachable",
     "Check the provider's status page and your timeout settings"),
    ("pool exhausted", "The database connection pool is full",
     "Look for slow queries or leaked connections"),
    ("nullpointer", "A recent code change probably introduced a bug",
     "Check the latest deployment and roll back if needed"),
    ("503", "A dependent service is returning 'unavailable'",
     "Check the health and recent restarts of that service"),
]


def analyze_with_rules(incident: dict) -> dict:
    text = " ".join(e.get("message", "") for e in (incident.get("timeline") or [])).lower()
    causes, steps = [], []
    for keyword, cause, step in RULES:
        if keyword in text:
            causes.append(cause)
            steps.append(step)
    if not causes:
        causes = ["Not enough evidence to name a cause"]
        steps = ["Inspect the raw ERROR logs around the start time"]
    steps.append("Compare the start time with recent deployments or config changes")
    return {
        "summary": (f"{incident.get('service')} had an error spike "
                    f"(peak {incident.get('peakErrorsPerMin')} errors/min, "
                    f"severity {incident.get('severity')}). "
                    "This summary came from built-in rules, not an LLM."),
        "likelyCauses": causes,
        "steps": steps,
    }


@app.post("/analyze")
def analyze(incident: dict):
    if PROVIDER == "anthropic":
        try:
            return analyze_with_claude(incident)
        except Exception as e:
            result = analyze_with_rules(incident)
            result["summary"] += f" (LLM failed: {type(e).__name__})"
            return result
    return analyze_with_rules(incident)


@app.get("/health")
def health():
    return {"status": "UP", "provider": PROVIDER}