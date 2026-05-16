import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { StatusBadge } from "../components/StatusBadge";
import { apiGet, apiPost } from "../lib/api";

type Pair = { id: string; name: string };
type Job = {
  id: string;
  status: string;
  report: {
    classes_renamed?: number; classes_preserved?: number;
    packages_renamed?: number; fields_renamed?: number;
    methods_renamed?: number; files?: number; stdout?: string;
  };
  mode: string;
  pair_name: string;
  created_at: string;
  errors: unknown[];
};

export default function Obfuscate() {
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
  const [seed, setSeed] = useState("obfusc");
  const [preservePkg, setPreservePkg] = useState("ma.ac2i");

  const run = useMutation({
    mutationFn: () => apiPost<Job>("/project-pairs/" + pair + "/obfuscate/", { seed, preserve_top_package: preservePkg }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["apply-jobs"] }),
  });

  const obfJobs = (jobs.data || []).filter(j => j.mode === "obfuscate");
  const last = obfJobs[0];

  return (
    <div>
      <PageHeader
        eyebrow="obfuscation complete"
        title="Obfusquer le projet entier"
        subtitle="Transformation source-vers-cible 100% automatique : renommage packages/classes/methodes/champs, chiffrement string literals XOR+Base64, injection du helper de dechiffrement."
      />

      <Card title="Lancer une obfuscation complete">
        <div className="stack">
          <div className="field">
            <label className="field-label">Paire</label>
            <select className="select" value={pair} onChange={e => setPair(e.target.value)}>
              <option value="">Selectionner...</option>
              {pairs.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
            <div className="field-hint">La paire definit la source, la cible (ECRASEE), et la cle XOR.</div>
          </div>
          <div className="form-row">
            <div className="field">
              <label className="field-label">Seed</label>
              <input className="input input-mono" value={seed} onChange={e => setSeed(e.target.value)} />
              <div className="field-hint">Memes seed + source = memes noms obfusques.</div>
            </div>
            <div className="field">
              <label className="field-label">Top package preserve</label>
              <input className="input input-mono" value={preservePkg} onChange={e => setPreservePkg(e.target.value)} placeholder="ma.ac2i" />
              <div className="field-hint">Prefixe non renomme.</div>
            </div>
          </div>
          <div className="alert alert-warn">
            <strong>Attention :</strong> le repertoire cible sera ECRASE. Sauvegarde si necessaire.
          </div>
          <div>
            <button className="btn btn-accent" onClick={() => run.mutate()} disabled={!pair || run.isPending}>
              {run.isPending ? <><span className="spinner" /> Obfuscation en cours...</> : "Lancer l obfuscation complete"}
            </button>
          </div>
        </div>
      </Card>

      {last && (
        <Card
          title={"Dernier job : " + last.status}
          actions={<StatusBadge status={last.status} />}
          subtitle={"Pair: " + last.pair_name}
        >
          {last.status === "done" && last.report && (
            <div>
              <div className="stat-grid" style={{ marginTop: 0, marginBottom: 16 }}>
                <div className="stat">
                  <div className="stat-label">Classes renommees</div>
                  <div className="stat-value">{last.report.classes_renamed ?? "?"}</div>
                  <div className="stat-delta">{last.report.classes_preserved} preservees (Spring)</div>
                </div>
                <div className="stat">
                  <div className="stat-label">Packages</div>
                  <div className="stat-value">{last.report.packages_renamed ?? "?"}</div>
                  <div className="stat-delta">renommes</div>
                </div>
                <div className="stat">
                  <div className="stat-label">Methodes</div>
                  <div className="stat-value">{last.report.methods_renamed ?? "?"}</div>
                  <div className="stat-delta">renommees</div>
                </div>
                <div className="stat">
                  <div className="stat-label">Fichiers</div>
                  <div className="stat-value">{last.report.files ?? "?"}</div>
                  <div className="stat-delta">.java traites</div>
                </div>
              </div>
              {last.report.stdout && (
                <details>
                  <summary className="muted text-sm" style={{ cursor: "pointer" }}>Voir la sortie complete</summary>
                  <div className="pre-block" style={{ marginTop: 8 }}>{last.report.stdout}</div>
                </details>
              )}
            </div>
          )}
          {last.status === "failed" && (
            <div className="alert alert-err">
              <strong>Echec :</strong> {(last.errors as Array<{ error: string }>)?.[0]?.error || "erreur inconnue"}
            </div>
          )}
          {(last.status === "pending" || last.status === "running") && (
            <div className="muted"><span className="spinner" /> JavaParser + transformation en cours (30-60s).</div>
          )}
        </Card>
      )}

      <Card title="Ce qui est fait automatiquement">
        <ul style={{ paddingLeft: 18, fontSize: 13, lineHeight: 1.8 }}>
          <li><strong>Renommage packages</strong> : tous les sous-packages avec hash deterministe.</li>
          <li><strong>Renommage classes</strong> non Spring (@Controller, @Service, @Entity preserves).</li>
          <li><strong>Champs + methodes</strong> renommes sauf classes Spring/JPA/Lombok.</li>
          <li><strong>Imports + references cross-files</strong> auto-recalcules.</li>
          <li><strong>String literals</strong> chiffres XOR+Base64 (sauf annotations, switch case, static final).</li>
          <li><strong>Helper de dechiffrement</strong> : <code>s0o/ObfRuntime__$.java</code> avec methode <code>$dec$</code>.</li>
        </ul>
      </Card>

      <Card title="Limites connues">
        <ul style={{ paddingLeft: 18, fontSize: 13, lineHeight: 1.8 }}>
          <li>Reflection (<code>Class.forName</code>, autowire byName) non gere.</li>
          <li><code>@Qualifier("bean")</code> : valeur string non transformee.</li>
          <li>Spring XML configs avec refs string : non transformes.</li>
        </ul>
      </Card>
    </div>
  );
}
