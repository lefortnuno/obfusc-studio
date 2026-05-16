import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { StatusBadge } from "../components/StatusBadge";
import { apiGet, apiPost } from "../lib/api";

type Pair = { id: string; name: string };
type Result = { mapping_id: string; target_var_name: string; target_file: string; source_var_name: string; status: string };
type Job = { id: string; status: string; report: { total?: number; ok?: number; results?: Result[] }; mode: string; pair_name: string; created_at: string };

export default function Audit() {
  const qc = useQueryClient();
  const pairs = useQuery<Pair[]>({ queryKey: ["pairs"], queryFn: () => apiGet("/project-pairs/") });
  const jobs = useQuery<Job[]>({
    queryKey: ["apply-jobs"],
    queryFn: () => apiGet("/apply-jobs/"),
    refetchInterval: (q) => {
      const data = q.state.data as Job[] | undefined;
      return data?.some(j => j.status === "pending" || j.status === "running") ? 1500 : false;
    },
  });
  const [pair, setPair] = useState("");
  const run = useMutation({
    mutationFn: () => apiPost<Job>("/project-pairs/" + pair + "/audit/", {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["apply-jobs"] }),
  });
  const last = (jobs.data || []).filter(j => j.mode === "audit")[0];

  return (
    <div>
      <PageHeader
        eyebrow="09 · Pipeline"
        title="Audit"
        subtitle="Decrypte les valeurs presentes dans le projet cible et les compare a la base. Permet de detecter une divergence ou recuperer du contenu si la source est perdue."
      />
      <Card title="Lancer un audit">
        <div className="row" style={{ alignItems: "flex-end", gap: 12 }}>
          <div className="field" style={{ flex: 1, marginBottom: 0 }}>
            <label className="field-label">Paire</label>
            <select className="select" value={pair} onChange={e => setPair(e.target.value)}>
              <option value="">Selectionner...</option>
              {pairs.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
          <button className="btn btn-accent" onClick={() => run.mutate()} disabled={!pair || run.isPending}>
            {run.isPending ? <><span className="spinner" /> Lancement...</> : "Auditer"}
          </button>
        </div>
      </Card>

      {last && (
        <Card
          title={"Dernier audit : " + (last.report?.ok ?? 0) + " / " + (last.report?.total ?? 0) + " OK"}
          actions={<StatusBadge status={last.status} />}
          flush
        >
          {!last.report?.results || last.report.results.length === 0 ? (
            <Empty icon="✓" title="Aucun resultat" />
          ) : (
            <table className="table">
              <thead><tr><th>Variable source</th><th>Variable cible</th><th>Fichier</th><th>Statut</th></tr></thead>
              <tbody>
                {last.report.results.map(r => (
                  <tr key={r.mapping_id}>
                    <td><span style={{ fontWeight: 500 }}>{r.source_var_name}</span></td>
                    <td><code className="mono">{r.target_var_name}</code></td>
                    <td><code className="text-xs muted">{r.target_file}</code></td>
                    <td>
                      {r.status === "OK" ? <span className="badge badge-ok">OK</span> :
                       r.status === "DIFF" ? <span className="badge badge-err">DIFF</span> :
                       <span className="badge badge-warn">{r.status}</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      )}
    </div>
  );
}
