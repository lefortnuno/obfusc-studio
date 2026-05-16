import { useQuery, useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { apiGet, apiPost } from "../lib/api";

type Pair = { id: string; name: string };
type Variable = { id: string; name: string; file_path: string; confidence: string; validated: boolean; is_sensitive: boolean };

export default function Variables() {
  const pairs = useQuery<Pair[]>({ queryKey: ["pairs"], queryFn: () => apiGet("/project-pairs/") });
  const vars = useQuery<Variable[]>({ queryKey: ["variables"], queryFn: () => apiGet("/variables/") });
  const [pair, setPair] = useState("");
  const [value, setValue] = useState("");
  const [preview, setPreview] = useState<{ encrypted: string; verify_ok: boolean; length: number } | null>(null);
  const [err, setErr] = useState("");

  const run = useMutation({
    mutationFn: () => apiPost<{ encrypted: string; verify_ok: boolean; length: number }>("/variables/preview-encryption/", { value, project_pair_id: pair }),
    onSuccess: (d) => { setPreview(d); setErr(""); },
    onError: (e: Error) => { setErr(e.message); setPreview(null); },
  });

  return (
    <div>
      <PageHeader
        eyebrow="04 · Source"
        title="Variables"
        subtitle="Liste des variables ingerees + outil de preview chiffrement pour tester ta cle XOR sur une valeur arbitraire."
      />

      <div className="split-2">
        <Card title="Preview chiffrement" subtitle="La valeur n est jamais loggee ni persistee.">
          <div className="stack">
            <div className="field">
              <label className="field-label">Paire (pour la cle XOR)</label>
              <select className="select" value={pair} onChange={e => setPair(e.target.value)}>
                <option value="">Selectionner...</option>
                {pairs.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
            <div className="field">
              <label className="field-label">Valeur en clair</label>
              <textarea className="textarea" rows={4} value={value} onChange={e => setValue(e.target.value)} placeholder="ADMIN" />
            </div>
            <button className="btn btn-primary" onClick={() => run.mutate()} disabled={!pair || !value || run.isPending}>
              {run.isPending ? <><span className="spinner" /> Calcul...</> : "Chiffrer"}
            </button>
            {err && <div className="alert alert-err">{err}</div>}
            {preview && (
              <div className="stack-sm">
                <div className="row-tight">
                  <span className="badge badge-ok">verify: {preview.verify_ok ? "OK" : "KO"}</span>
                  <span className="muted text-xs">{preview.length} chars</span>
                </div>
                <div className="pre-block">{preview.encrypted}</div>
              </div>
            )}
          </div>
        </Card>

        <Card title={"Variables enregistrees (" + (vars.data?.length || 0) + ")"}>
          {!vars.data || vars.data.length === 0 ? (
            <Empty icon="·" title="Aucune variable" text="Apres une analyse, les variables apparaissent ici. Va dans Validation pour les valider." />
          ) : (
            <div className="stack-sm" style={{ maxHeight: 400, overflowY: "auto" }}>
              {vars.data.slice(0, 50).map(v => (
                <div key={v.id} style={{ display: "flex", alignItems: "center", gap: 8, padding: "6px 0", borderBottom: "1px solid var(--border)" }}>
                  <span style={{ flex: 1, fontWeight: 500, fontSize: 13 }}>{v.name}</span>
                  <span className="text-xs muted mono truncate" style={{ maxWidth: 180 }}>{v.file_path}</span>
                  <span className={"badge badge-" + (v.confidence === "high" ? "ok" : v.confidence === "medium" ? "warn" : v.confidence === "low" ? "err" : "neutral")}>{v.confidence}</span>
                  {v.validated && <span className="badge badge-ok">✓</span>}
                </div>
              ))}
              {vars.data.length > 50 && <div className="muted text-sm">+ {vars.data.length - 50} autres</div>}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
