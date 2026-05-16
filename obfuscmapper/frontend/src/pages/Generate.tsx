import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { StatusBadge } from "../components/StatusBadge";
import { apiGet, apiPost } from "../lib/api";

type Pair = { id: string; name: string };
type Job = { id: string; status: string; report: Record<string, number>; errors: unknown[]; files_processed: number; files_total: number; mode: string; pair_name: string; created_at: string };

export default function Generate() {
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
    mutationFn: () => apiPost<Job>("/project-pairs/" + pair + "/generate/", {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["apply-jobs"] }),
  });
  const generateJobs = (jobs.data || []).filter(j => j.mode === "generate");

  return (
    <div>
      <PageHeader
        eyebrow="07 · Pipeline"
        title="Generation"
        subtitle="Cree physiquement la structure obfusquee du projet cible sur disque depuis la structure source enregistree."
      />
      <Card title="Lancer la generation">
        <div className="row" style={{ alignItems: "flex-end", gap: 12 }}>
          <div className="field" style={{ flex: 1, marginBottom: 0 }}>
            <label className="field-label">Paire</label>
            <select className="select" value={pair} onChange={e => setPair(e.target.value)}>
              <option value="">Selectionner...</option>
              {pairs.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
          <button className="btn btn-accent" onClick={() => run.mutate()} disabled={!pair || run.isPending}>
            {run.isPending ? <><span className="spinner" /> Lancement...</> : "Generer"}
          </button>
        </div>
      </Card>
      <Card title={"Jobs generation (" + generateJobs.length + ")"} flush>
        {generateJobs.length === 0 ? <Empty icon="✦" title="Aucun job generation" /> : (
          <table className="table">
            <thead><tr><th>Paire</th><th>Statut</th><th>Rapport</th><th>Cree</th></tr></thead>
            <tbody>
              {generateJobs.slice(0, 20).map(j => (
                <tr key={j.id}>
                  <td>{j.pair_name}</td>
                  <td><StatusBadge status={j.status} /></td>
                  <td className="text-xs mono muted">{JSON.stringify(j.report || {})}</td>
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
