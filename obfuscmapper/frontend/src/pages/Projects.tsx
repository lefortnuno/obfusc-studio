import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { Modal } from "../components/Modal";
import { StatusBadge } from "../components/StatusBadge";
import { apiGet, apiPost, apiDel } from "../lib/api";

type Project = {
  id: string;
  name: string;
  description: string;
  root_path: string;
  language: string;
  project_type: string;
};

type Job = {
  id: string;
  project: string;
  project_name: string;
  status: string;
  result_json: { summary?: { folders_created?: number; files_created?: number; variables_created?: number } } | null;
  error_message: string;
  started_at: string | null;
  finished_at: string | null;
};

export default function Projects() {
  const qc = useQueryClient();
  const projects = useQuery<Project[]>({ queryKey: ["projects"], queryFn: () => apiGet("/projects/") });
  const jobs = useQuery<Job[]>({
    queryKey: ["analysis-jobs"],
    queryFn: () => apiGet("/analysis-jobs/"),
    refetchInterval: (q) => {
      const data = q.state.data as Job[] | undefined;
      return data?.some(j => j.status === "pending" || j.status === "running") ? 1500 : false;
    },
  });

  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [rootPath, setRootPath] = useState("");
  const [desc, setDesc] = useState("");
  const [err, setErr] = useState("");

  const create = useMutation({
    mutationFn: () => apiPost<Project>("/projects/", { name, root_path: rootPath, description: desc }),
    onSuccess: () => {
      setOpen(false);
      setName(""); setRootPath(""); setDesc(""); setErr("");
      qc.invalidateQueries({ queryKey: ["projects"] });
    },
    onError: (e: Error) => setErr(e.message),
  });

  const del = useMutation({
    mutationFn: (id: string) => apiDel("/projects/" + id + "/"),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["projects"] }),
  });

  const analyze = useMutation({
    mutationFn: (id: string) => apiPost<Job>("/projects/" + id + "/analyze/", {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["analysis-jobs"] }),
  });

  const lastJobOf = (projectId: string): Job | undefined =>
    jobs.data?.filter(j => j.project === projectId).sort((a, b) => (b.id > a.id ? 1 : -1))[0];

  return (
    <div>
      <PageHeader
        eyebrow="01 · Configuration"
        title="Projets"
        subtitle="Declare un projet source (lisible) et un projet cible (obfusque). Le chemin disque est requis pour l'analyse et l'injection."
        actions={<button className="btn btn-primary" onClick={() => setOpen(true)}>+ Nouveau projet</button>}
      />

      {projects.isLoading && <Card><div className="muted">Chargement...</div></Card>}

      {projects.data && projects.data.length === 0 && (
        <Card>
          <Empty
            icon="◇"
            title="Aucun projet"
            text="Commence par declarer le projet source (Converter-unobf) et le projet cible (Converter-obf)."
            action={<button className="btn btn-accent" onClick={() => setOpen(true)}>Creer le premier projet</button>}
          />
        </Card>
      )}

      {projects.data && projects.data.length > 0 && (
        <Card title={"Projets enregistres (" + projects.data.length + ")"} flush>
          <table className="table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Chemin</th>
                <th>Langue</th>
                <th>Derniere analyse</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {projects.data.map(p => {
                const job = lastJobOf(p.id);
                const running = job?.status === "running" || job?.status === "pending";
                const isAnalyzing = analyze.isPending && analyze.variables === p.id;
                return (
                  <tr key={p.id}>
                    <td>
                      <div style={{ fontWeight: 500 }}>{p.name}</div>
                      {p.description && <div className="muted text-xs" style={{ marginTop: 2 }}>{p.description}</div>}
                    </td>
                    <td className="table-mono truncate" style={{ maxWidth: 280 }}>{p.root_path}</td>
                    <td><span className="badge badge-neutral">{p.language}</span></td>
                    <td>
                      {!job && <span className="muted text-sm">jamais</span>}
                      {job && (
                        <div className="stack-sm">
                          <StatusBadge status={job.status} />
                          {job.status === "done" && job.result_json?.summary && (
                            <div className="text-xs muted">
                              {job.result_json.summary.variables_created} vars · {job.result_json.summary.files_created} fichiers
                            </div>
                          )}
                          {job.status === "failed" && (
                            <div className="text-xs" style={{ color: "var(--err)" }}>{job.error_message?.slice(0, 80)}</div>
                          )}
                        </div>
                      )}
                    </td>
                    <td className="col-actions">
                      <div className="row-tight" style={{ justifyContent: "flex-end" }}>
                        <button
                          className="btn btn-accent btn-sm"
                          onClick={() => analyze.mutate(p.id)}
                          disabled={running || isAnalyzing}
                          title="Lance le JAR JavaParser via une tache Celery"
                        >
                          {running || isAnalyzing ? (<><span className="spinner" /> Analyse...</>) : "Analyser"}
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => { if (confirm("Supprimer " + p.name + " ?")) del.mutate(p.id); }}>Suppr</button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </Card>
      )}

      <Card title="Comment ca marche" subtitle="L analyse extrait toutes les variables String du projet source via JavaParser.">
        <ol className="muted" style={{ paddingLeft: 18, fontSize: 13, lineHeight: 1.7 }}>
          <li>Declare ton projet source avec son <code>root_path</code> absolu.</li>
          <li>Clique <strong>Analyser</strong>. Une tache Celery lance le JAR a <code>parser/target/obfusc-parser.jar</code>.</li>
          <li>Les dossiers, fichiers et variables sont ingeres avec <code>validated=false</code>.</li>
          <li>Va dans <strong>Validation</strong> pour valider les propositions <span className="badge badge-ok">high</span>.</li>
        </ol>
      </Card>

      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title="Nouveau projet"
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setOpen(false)}>Annuler</button>
            <button className="btn btn-primary" onClick={() => create.mutate()} disabled={!name || !rootPath || create.isPending}>
              {create.isPending ? <><span className="spinner" /> Creation...</> : "Creer"}
            </button>
          </>
        }
      >
        <div className="stack">
          <div className="field">
            <label className="field-label">Nom</label>
            <input className="input" value={name} onChange={e => setName(e.target.value)} placeholder="Converter-unobf" autoFocus />
            <div className="field-hint">Identifiant unique du projet.</div>
          </div>
          <div className="field">
            <label className="field-label">Chemin racine (absolu)</label>
            <input className="input input-mono" value={rootPath} onChange={e => setRootPath(e.target.value)} placeholder="C:\Users\rtoma\obfusc-studio\Converter-unobf" />
            <div className="field-hint">Le chemin doit exister sur le disque du serveur ObfuscMapper.</div>
          </div>
          <div className="field">
            <label className="field-label">Description <span className="muted">(optionnel)</span></label>
            <textarea className="textarea" value={desc} onChange={e => setDesc(e.target.value)} rows={2} />
          </div>
          {err && <div className="alert alert-err">{err}</div>}
        </div>
      </Modal>
    </div>
  );
}
