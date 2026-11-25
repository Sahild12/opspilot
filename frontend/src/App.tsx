import { useCallback, useEffect, useState } from "react";
import {
  type Incident,
  listIncidents,
  getIncident,
  resolveIncident,
  analyzeIncident,
  parseAnalysis,
} from "./api";
import "./App.css";

export default function App() {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [selected, setSelected] = useState<Incident | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const refresh = useCallback(async () => {
    try {
      setIncidents(await listIncidents());
      if (selectedId !== null) setSelected(await getIncident(selectedId));
      setError(null);
    } catch (e) {
      setError((e as Error).message);
    }
  }, [selectedId]);

  useEffect(() => {
    const first = setTimeout(refresh, 0);
    const timer = setInterval(refresh, 5000); // live updates every 5 seconds
    return () => {
      clearTimeout(first);
      clearInterval(timer);
    };
  }, [refresh]);

  async function select(id: number) {
    setSelectedId(id);
    try {
      setSelected(await getIncident(id));
    } catch (e) {
      setError((e as Error).message);
    }
  }

  async function act(fn: (id: number) => Promise<Incident>) {
    if (!selected) return;
    setBusy(true);
    try {
      setSelected(await fn(selected.id));
      setIncidents(await listIncidents());
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  const analysis = parseAnalysis(selected?.summary ?? null);

  return (
    <div className="layout">
      <aside>
        <h1>OpsPilot</h1>
        {error && <p className="error">{error}</p>}
        {incidents.length === 0 && <p className="muted">No incidents. All quiet.</p>}
        {incidents.map((i) => (
          <div
            key={i.id}
            className={`card ${selected?.id === i.id ? "active" : ""}`}
            onClick={() => select(i.id)}
          >
            <div>
              <span className={`badge ${i.status}`}>{i.status}</span>
              <span className={`badge sev-${i.severity}`}>{i.severity}</span>
            </div>
            <strong>{i.title}</strong>
            <small>
              {new Date(i.startedAt).toLocaleString()} · peak {i.peakErrorsPerMin}/min
            </small>
          </div>
        ))}
      </aside>

      <main>
        {!selected ? (
          <p className="muted">Select an incident.</p>
        ) : (
          <>
            <h2>
              {selected.title} <small>#{selected.id}</small>
            </h2>
            <div className="actions">
              <button disabled={busy} onClick={() => act(analyzeIncident)}>
                {busy ? "Working..." : "Analyze with AI"}
              </button>
              {selected.status === "OPEN" && (
                <button disabled={busy} onClick={() => act(resolveIncident)}>
                  Resolve
                </button>
              )}
            </div>

            {analysis && (
              <section className="panel">
                <h3>AI analysis</h3>
                <p>{analysis.summary}</p>
                <h4>Likely causes</h4>
                <ol>
                  {analysis.likelyCauses.map((c, n) => (
                    <li key={n}>{c}</li>
                  ))}
                </ol>
                <h4>What to check</h4>
                <ol>
                  {analysis.steps.map((s, n) => (
                    <li key={n}>{s}</li>
                  ))}
                </ol>
              </section>
            )}

            <section className="panel">
              <h3>Timeline</h3>
              <ul className="timeline">
                {selected.timeline?.map((t, n) => (
                  <li key={n}>
                    <time>{new Date(t.time).toLocaleTimeString()}</time>
                    <span className={`tag ${t.type}`}>{t.type}</span>
                    <span>{t.message}</span>
                  </li>
                ))}
              </ul>
            </section>
          </>
        )}
      </main>
    </div>
  );
}