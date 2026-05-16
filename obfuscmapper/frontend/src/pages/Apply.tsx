import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { StatusBadge } from "../components/StatusBadge";
import { apiGet, apiPost } from "../lib/api";

type Pair = { id: string; name: string };
type Job = { id: string; status: string; report: { applied?: number; skipped?: number; errors_count?: number }; files_processed: number; files_total: number; mode: string; pair_name: string; created_at: string };

export default function Apply() {
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
    mutationFn: () => apiPost<Job>("/project-pairs/" + pair + "/apply/", {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["apply-jobs"] }),
  });

  const applyJobs = (jobs.data || []).filter(j => j.mode === "apply");

  return (
    <div>
      <PageHeader
        eyebrow="08 · Pipeline"
        title="Apply"
        subtitle="Injecte les valeurs chiffrees XOR dans les fichiers du projet cible selon les mappings valides."
      />

      <Card title="Lancer une injection">
        <div className="row" style={{ alignItems: "flex-end", gap: 12 }}>
          <div className="field" style={{ flex: 1, marginBottom: 0 }}>
            <label className="field-label">Paire</label>
            <select className="select" value={pair} onChange={e => setPair(e.target.value)}>
              <option value="">Selectionner...</option>
              {pairs.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
          <button className="btn btn-accent" onClick={() => run.mutate()} disabled={!pair || run.isPending}>
            {run.isPending ? <><span className="spinner" /> Lancement...</> : "Lancer apply"}
          </button>
        </div>
      </Card>

      <Card title={"Jobs apply (" + applyJobs.length + ")"} flush>
        {applyJobs.length === 0 ? (
          <Empty icon="↻" title="Aucun job apply" text="Lance une injection pour voir son statut ici." />
        ) : (
          <table className="table">
            <thead><tr><th>Paire</th><th>Statut</th><th>Progression</th><th>Applique / Skip / Err</th><th>Cree</th></tr></thead>
            <tbody>
              {applyJobs.slice(0, 20).map(j => (
                <tr key={j.id}>
                  <td>{j.pair_name}</td>
                  <td><StatusBadge status={j.status} /></td>
                  <td>
                    <div className="row-tight">
                      <div className="progress-bar" style={{ width: 120 }}>
                        <div className="progress-fill" style={{ width: (j.files_total ? (j.files_processed / j.files_total) * 100 : 0) + "%" }} />
                      </div>
                      <span className="text-xs muted">{j.files_processed}/{j.files_total}</span>
                    </div>
                  </td>
                  <td className="text-sm">
                    <span className="badge badge-ok">{j.report?.applied || 0} ok</span>{" "}
                    {(j.report?.skipped || 0) > 0 && <span className="badge badge-warn">{j.report.skipped} skip</span>}{" "}
                    {(j.report?.errors_count || 0) > 0 && <span className="badge badge-err">{j.report.errors_count} err</span>}
                  </td>
                  <td className="text-xs muted">{new Date(j.created_at).toLocaleTimeString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}
