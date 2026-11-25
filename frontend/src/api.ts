export interface TimelineItem {
  time: string;
  type: string;
  message: string;
}

export interface Incident {
  id: number;
  service: string;
  title: string;
  status: "OPEN" | "RESOLVED";
  severity: "MEDIUM" | "HIGH";
  startedAt: string;
  resolvedAt: string | null;
  peakErrorsPerMin: number;
  summary: string | null;
  timeline: TimelineItem[] | null;
}

export interface Analysis {
  summary: string;
  likelyCauses: string[];
  steps: string[];
}

const BASE = "http://localhost:8080/api/incidents";

async function request<T>(url: string, method = "GET"): Promise<T> {
  const res = await fetch(url, { method });
  if (!res.ok) throw new Error(`Request failed (${res.status})`);
  return res.json();
}

export const listIncidents = () => request<Incident[]>(BASE);
export const getIncident = (id: number) => request<Incident>(`${BASE}/${id}`);
export const resolveIncident = (id: number) => request<Incident>(`${BASE}/${id}/resolve`, "POST");
export const analyzeIncident = (id: number) => request<Incident>(`${BASE}/${id}/analyze`, "POST");

export function parseAnalysis(summary: string | null): Analysis | null {
  if (!summary) return null;
  try {
    return JSON.parse(summary) as Analysis;
  } catch {
    return null;
  }
}