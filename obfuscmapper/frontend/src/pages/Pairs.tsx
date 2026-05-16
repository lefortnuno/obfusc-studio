import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { Modal } from "../components/Modal";
import { apiGet, apiPost, apiDel } from "../lib/api";

type Project = { id: string; name: string };
type Pair = {
  id: string;
  name: string;
  source_project: string;
  source_project_name: string;
  target_project: string | null;
  target_project_name: string | null;
  encryption_key: string;
  algorithm: string;
};

export default function Pairs() {
  const qc = useQueryClient();
  const projects = useQuery<Project[]>({ queryKey: ["projects"], queryFn: () => apiGet("/projects/") });
  const pairs = useQuery<Pair[]>({ queryKey: ["pairs"], queryFn: () => apiGet("/project-pairs/") });

  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [src, setSrc] = useState("");
  const [tgt, setTgt] = useState("");
  const [key, setKey] = useState("");
  const [err, setErr] = useState("");

  const create = useMutation({
    mutationFn: () => apiPost<Pair>("/project-pairs/", {
      name, source_project: src, target_project: tgt || null, encryption_key: key, algorithm: "xor_base64",
    }),
    onSuccess: () => { setOpen(false); setName(""); setSrc(""); setTgt(""); setKey(""); setErr(""); qc.invalidateQueries({ queryKey: ["pairs"] }); },
    onError: (e: Error) => setErr(e.message),
  });

  const del = useMutation({
    mutationFn: (id: string) => apiDel("/project-pairs/" + id + "/"),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["pairs"] }),
  });

  return (
    <div>
      <PageHeader
        eyebrow="02 · Configuration"
        title="Paires de projets"
        subtitle="Une paire lie un projet source a un projet cible avec une cle XOR dediee. La cle est chiffree Fernet en base et jamais renvoyee en clair."
        actions={<button className="btn btn-primary" disabled={!projects.data?.length} onClick={() => setOpen(true)}>+ Nouvelle paire</button>}
      />

      {pairs.data?.length === 0 && (
        <Card>
          <Empty
            icon="↔"
            title="Aucune paire"
            text={projects.data?.length ? "Cree une paire pour lier ton projet source au projet cible." : "Cree au moins 2 projets avant de creer une paire."}
            action={projects.data?.length ? <button className="btn btn-accent" onClick={() => setOpen(true)}>Creer une paire</button> : undefined}
          />
        </Card>
      )}

      {pairs.data && pairs.data.length > 0 && (
        <Card title={"Paires (" + pairs.data.length + ")"} flush>
          <table className="table">
            <thead><tr><th>Nom</th><th>Source</th><th>Cible</th><th>Algorithme</th><th>Cle</th><th className="col-actions">Actions</th></tr></thead>
            <tbody>
              {pairs.data.map(p => (
                <tr key={p.id}>
                  <td><span style={{ fontWeight: 500 }}>{p.name}</span></td>
                  <td>{p.source_project_name}</td>
                  <td>{p.target_project_name || <span className="muted">— non defini</span>}</td>
                  <td><span className="badge badge-neutral">{p.algorithm}</span></td>
                  <td><code className="muted">{p.encryption_key}</code></td>
                  <td className="col-actions">
                    <button className="btn btn-danger btn-sm" onClick={() => { if (confirm("Supprimer cette paire ?")) del.mutate(p.id); }}>Suppr</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title="Nouvelle paire"
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setOpen(false)}>Annuler</button>
            <button className="btn btn-primary" onClick={() => create.mutate()} disabled={!name || !src || !key || create.isPending}>
              {create.isPending ? <><span className="spinner" /> Creation...</> : "Creer"}
            </button>
          </>
        }
      >
        <div className="stack">
          <div className="field">
            <label className="field-label">Nom</label>
            <input className="input" value={name} onChange={e => setName(e.target.value)} placeholder="pilote" autoFocus />
          </div>
          <div className="form-row">
            <div className="field">
              <label className="field-label">Projet source</label>
              <select className="select" value={src} onChange={e => setSrc(e.target.value)}>
                <option value="">Selectionner...</option>
                {projects.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
            <div className="field">
              <label className="field-label">Projet cible <span className="muted">(optionnel)</span></label>
              <select className="select" value={tgt} onChange={e => setTgt(e.target.value)}>
                <option value="">—</option>
                {projects.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
          </div>
          <div className="field">
            <label className="field-label">Cle XOR</label>
            <input className="input input-mono" type="password" value={key} onChange={e => setKey(e.target.value)} placeholder="A0x43x32x49$cwBJAQ==" />
            <div className="field-hint">Chiffree Fernet en base. Apres enregistrement elle ne sera plus jamais affichee.</div>
          </div>
          {err && <div className="alert alert-err">{err}</div>}
        </div>
      </Modal>
    </div>
  );
}
