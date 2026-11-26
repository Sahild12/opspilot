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
      const list = await listIncidents();
      setIncidents(list);
      const id = selectedId ?? list[0]?.id ?? null; // auto-pick newest
      if (id !== null) {
        if (selectedId === null) setSelectedId(id);
        setSelected(await getIncident(id));
      }
      setError(null);
    } catch (e) {
      setError((e as Error).message);
    }
  }, [selectedId]);

  useEffect(() => {
    const first = setTimeout(refresh, 0);
    const timer = setInterval(refresh, 5000);
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
  const open = incidents.filter((i) => i.status === "OPEN").length;
  const resolved = incidents.length - open;
  const highestPeak = incidents.reduce((m, i) => Math.max(m, i.peakErrorsPerMin), 0);

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
        <div className={`banner ${open > 0 ? "bad" : "good"}`}>
          {open > 0
            ? `${open} active incident${open > 1 ? "s" : ""} need attention`
            : "All systems normal"}
        </div>

        <div className="stats">
          <div className="stat"><span>{open}</span><label>Open</label></div>
          <div className="stat"><span>{resolved}</span><label>Resolved</label></div>
          <div className="stat"><span>{incidents.length}</span><label>Total</label></div>
          <div className="stat"><span>{highestPeak}</span><label>Highest peak / min</label></div>
        </div>

        {!selected ? (
          <p className="muted">No incident to show yet.</p>
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
                <button className="secondary" disabled={busy} onClick={() => act(resolveIncident)}>
                  Resolve
                </button>
              )}
            </div>

            {analysis ? (
              <section className="panel">
                <h3>AI analysis</h3>
                <p>{analysis.summary}</p>
                <h4>Likely causes</h4>
                <ol>{analysis.likelyCauses.map((c, n) => <li key={n}>{c}</li>)}</ol>
                <h4>What to check</h4>
                <ol>{analysis.steps.map((s, n) => <li key={n}>{s}</li>)}</ol>
              </section>
            ) : (
              <p className="muted">No AI analysis yet. Click "Analyze with AI".</p>
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