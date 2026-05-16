import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState, useMemo } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { apiGet, apiPost } from "../lib/api";

type Variable = {
  id: string;
  file_path: string;
  name: string;
  var_type: string;
  is_sensitive: boolean;
  confidence: string;
  validated: boolean;
  notes: string;
};

type Filter = "pending" | "all" | "high" | "medium" | "low" | "validated";

export default function Validation() {
  const qc = useQueryClient();
  const vars = useQuery<Variable[]>({ queryKey: ["variables"], queryFn: () => apiGet("/variables/") });
  const [filter, setFilter] = useState<Filter>("pending");
  const [selected, setSelected] = useState<Set<string>>(new Set());

  const bulk = useMutation({
    mutationFn: (payload: { ids: string[]; action: string }) => apiPost<{ updated: number }>("/variables/bulk-validate/", payload),
    onSuccess: () => { setSelected(new Set()); qc.invalidateQueries({ queryKey: ["variables"] }); },
  });

  const filtered = useMemo(() => {
    const list = vars.data || [];
    if (filter === "all") return list;
    if (filter === "pending") return list.filter(v => !v.validated);
    if (filter === "validated") return list.filter(v => v.validated);
    return list.filter(v => v.confidence === filter);
  }, [vars.data, filter]);

  const toggle = (id: string) => {
    const s = new Set(selected);
    s.has(id) ? s.delete(id) : s.add(id);
    setSelected(s);
  };
  const toggleAll = () => {
    if (selected.size === filtered.length) setSelected(new Set());
    else setSelected(new Set(filtered.map(v => v.id)));
  };

  const counts = {
    pending: vars.data?.filter(v => !v.validated).length || 0,
    high: vars.data?.filter(v => v.confidence === "high").length || 0,
    medium: vars.data?.filter(v => v.confidence === "medium").length || 0,
    low: vars.data?.filter(v => v.confidence === "low").length || 0,
    validated: vars.data?.filter(v => v.validated).length || 0,
    all: vars.data?.length || 0,
  };

  const FILTERS: { key: Filter; label: string; count: number }[] = [
    { key: "pending", label: "En attente", count: counts.pending },
    { key: "high", label: "High", count: counts.high },
    { key: "medium", label: "Medium", count: counts.medium },
    { key: "low", label: "Low", count: counts.low },
    { key: "validated", label: "Validees", count: counts.validated },
    { key: "all", label: "Toutes", count: counts.all },
  ];

  return (
    <div>
      <PageHeader
        eyebrow="05 · Source"
        title="Validation"
        subtitle="Valide les variables detectees par l analyse automatique. Les high peuvent etre validees en masse."
        actions={
          <button
            className="btn btn-accent"
            disabled={counts.high === 0 || bulk.isPending}
            onClick={() => bulk.mutate({ ids: (vars.data || []).filter(v => v.confidence === "high" && !v.validated).map(v => v.id), action: "validate" })}
          >
            ✓ Valider tout (high)
          </button>
        }
      />

      <Card flush>
        <div style={{ display: "flex", gap: 4, padding: "12px 20px", borderBottom: "1px solid var(--border)", flexWrap: "wrap", alignItems: "center" }}>
          {FILTERS.map(f => (
            <button
              key={f.key}
              onClick={() => setFilter(f.key)}
              className={"btn btn-sm " + (filter === f.key ? "btn-primary" : "btn-ghost")}
              style={{ borderRadius: 999 }}
            >
              {f.label} <span className="muted" style={{ opacity: filter === f.key ? 0.7 : 1 }}>· {f.count}</span>
            </button>
          ))}
          <div className="spacer" />
          {selected.size > 0 && (
            <div className="row-tight">
              <span className="text-sm muted">{selected.size} selectionnee(s)</span>
              <button className="btn btn-sm btn-primary" onClick={() => bulk.mutate({ ids: [...selected], action: "validate" })}>Valider</button>
              <button className="btn btn-sm btn-ghost" onClick={() => bulk.mutate({ ids: [...selected], action: "unsensitive" })}>Non-sensible</button>
              <button className="btn btn-sm btn-danger" onClick={() => bulk.mutate({ ids: [...selected], action: "delete" })}>Supprimer</button>
            </div>
          )}
        </div>

        {filtered.length === 0 ? (
          <Empty icon="◯" title="Aucune variable dans ce filtre" text="Change de filtre ou lance une analyse depuis Projets." />
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th style={{ width: 32 }}>
                  <input type="checkbox" checked={selected.size === filtered.length && filtered.length > 0} onChange={toggleAll} />
                </th>
                <th>Nom</th>
                <th>Fichier</th>
                <th>Confiance</th>
                <th>Statut</th>
                <th>Sensible</th>
              </tr>
            </thead>
            <tbody>
              {filtered.slice(0, 200).map(v => (
                <tr key={v.id}>
                  <td><input type="checkbox" checked={selected.has(v.id)} onChange={() => toggle(v.id)} /></td>
                  <td><span style={{ fontWeight: 500 }}>{v.name}</span></td>
                  <td><code className="muted text-xs">{v.file_path}</code></td>
                  <td><span className={"badge badge-" + (v.confidence === "high" ? "ok" : v.confidence === "medium" ? "warn" : v.confidence === "low" ? "err" : "neutral")}>{v.confidence}</span></td>
                  <td>{v.validated ? <span className="badge badge-ok">valide</span> : <span className="badge badge-neutral">en attente</span>}</td>
                  <td>{v.is_sensitive ? <span className="badge badge-accent">oui</span> : <span className="badge badge-neutral">non</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {filtered.length > 200 && <div className="card-footer">{filtered.length - 200} variables masquees (limite affichage)</div>}
      </Card>
    </div>
  );
}
