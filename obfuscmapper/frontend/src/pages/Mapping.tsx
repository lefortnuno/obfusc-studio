import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState, useMemo } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { Modal } from "../components/Modal";
import { apiGet, apiPost, apiDel, apiPatch } from "../lib/api";

type Pair = { id: string; name: string; source_project: string; target_project: string | null };
type Variable = { id: string; name: string; file_path: string; confidence: string; validated: boolean };
type SourceFile = { id: string; name: string; relative_path: string; project: string };
type Mapping = {
  id: string;
  project_pair: string;
  source_variable: string;
  source_variable_name: string;
  source_file_path: string;
  target_file: string;
  target_file_path: string;
  target_var_name: string;
  injection_pattern: string;
  validated: boolean;
  applied_at: string | null;
};

const DEFAULT_PATTERN = 'this.{target_var_name} = R04oo.d0x116_("{value}");';

export default function MappingPage() {
  const qc = useQueryClient();
  const pairs = useQuery<Pair[]>({ queryKey: ["pairs"], queryFn: () => apiGet("/project-pairs/") });
  const variables = useQuery<Variable[]>({ queryKey: ["variables"], queryFn: () => apiGet("/variables/") });
  const files = useQuery<SourceFile[]>({ queryKey: ["files"], queryFn: () => apiGet("/source-files/") });
  const mappings = useQuery<Mapping[]>({ queryKey: ["mappings"], queryFn: () => apiGet("/mappings/") });

  const [open, setOpen] = useState(false);
  const [pair, setPair] = useState("");
  const [srcVar, setSrcVar] = useState("");
  const [tgtFile, setTgtFile] = useState("");
  const [tgtName, setTgtName] = useState("");
  const [pattern, setPattern] = useState(DEFAULT_PATTERN);
  const [err, setErr] = useState("");

  const create = useMutation({
    mutationFn: () => apiPost<Mapping>("/mappings/", { project_pair: pair, source_variable: srcVar, target_file: tgtFile, target_var_name: tgtName, injection_pattern: pattern }),
    onSuccess: () => { setOpen(false); setSrcVar(""); setTgtFile(""); setTgtName(""); setErr(""); qc.invalidateQueries({ queryKey: ["mappings"] }); },
    onError: (e: Error) => setErr(e.message),
  });
  const del = useMutation({
    mutationFn: (id: string) => apiDel("/mappings/" + id + "/"),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["mappings"] }),
  });
  const validate = useMutation({
    mutationFn: (id: string) => apiPatch<Mapping>("/mappings/" + id + "/", { validated: true }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["mappings"] }),
  });

  const currentPair = useMemo(() => pairs.data?.find(p => p.id === pair), [pairs.data, pair]);
  const srcVars = useMemo(() => (variables.data || []).filter(v => v.validated), [variables.data]);
  const tgtFiles = useMemo(() => (files.data || []).filter(f => f.project === currentPair?.target_project), [files.data, currentPair]);

  return (
    <div>
      <PageHeader
        eyebrow="06 · Pipeline"
        title="Mapping"
        subtitle="Lie chaque variable source validee a un emplacement dans un fichier cible. Le pattern d injection definit la ligne a remplacer."
        actions={<button className="btn btn-primary" onClick={() => setOpen(true)}>+ Nouveau mapping</button>}
      />

      {mappings.data && mappings.data.length === 0 ? (
        <Card><Empty icon="⤳" title="Aucun mapping" text="Cree un premier mapping pour relier une variable source au fichier cible obfusque." action={<button className="btn btn-accent" onClick={() => setOpen(true)}>Creer un mapping</button>} /></Card>
      ) : (
        <Card title={"Mappings (" + (mappings.data?.length || 0) + ")"} flush>
          <table className="table">
            <thead><tr><th>Source</th><th>Cible</th><th>Pattern</th><th>Statut</th><th className="col-actions">Actions</th></tr></thead>
            <tbody>
              {mappings.data?.map(m => (
                <tr key={m.id}>
                  <td>
                    <div style={{ fontWeight: 500 }}>{m.source_variable_name}</div>
                    <div className="text-xs muted mono">{m.source_file_path}</div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 500 }} className="mono">{m.target_var_name}</div>
                    <div className="text-xs muted mono">{m.target_file_path}</div>
                  </td>
                  <td><code className="text-xs muted">{m.injection_pattern.length > 50 ? m.injection_pattern.slice(0, 50) + "..." : m.injection_pattern}</code></td>
                  <td>
                    {m.applied_at ? <span className="badge badge-ok">applique</span> :
                     m.validated ? <span className="badge badge-info">valide</span> :
                     <span className="badge badge-warn">en attente</span>}
                  </td>
                  <td className="col-actions">
                    <div className="row-tight" style={{ justifyContent: "flex-end" }}>
                      {!m.validated && <button className="btn btn-sm btn-primary" onClick={() => validate.mutate(m.id)}>Valider</button>}
                      <button className="btn btn-sm btn-danger" onClick={() => { if (confirm("Supprimer ?")) del.mutate(m.id); }}>Suppr</button>
                    </div>
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
        title="Nouveau mapping"
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setOpen(false)}>Annuler</button>
            <button className="btn btn-primary" disabled={!pair || !srcVar || !tgtFile || !tgtName || create.isPending} onClick={() => create.mutate()}>
              {create.isPending ? <><span className="spinner" /> Creation...</> : "Creer"}
            </button>
          </>
        }
      >
        <div className="stack">
          <div className="field">
            <label className="field-label">Paire</label>
            <select className="select" value={pair} onChange={e => setPair(e.target.value)}>
              <option value="">Selectionner...</option>
              {pairs.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
          <div className="form-row">
            <div className="field">
              <label className="field-label">Variable source (validee)</label>
              <select className="select" value={srcVar} onChange={e => setSrcVar(e.target.value)}>
                <option value="">Selectionner...</option>
                {srcVars.map(v => <option key={v.id} value={v.id}>{v.name} — {v.file_path}</option>)}
              </select>
            </div>
            <div className="field">
              <label className="field-label">Fichier cible</label>
              <select className="select" value={tgtFile} onChange={e => setTgtFile(e.target.value)} disabled={!currentPair}>
                <option value="">{currentPair ? "Selectionner..." : "Choisir d abord la paire"}</option>
                {tgtFiles.map(f => <option key={f.id} value={f.id}>{f.relative_path}</option>)}
              </select>
            </div>
          </div>
          <div className="field">
            <label className="field-label">Nom variable cible</label>
            <input className="input input-mono" value={tgtName} onChange={e => setTgtName(e.target.value)} placeholder="x0x5F111x5Fx116$" />
          </div>
          <div className="field">
            <label className="field-label">Pattern d injection</label>
            <input className="input input-mono" value={pattern} onChange={e => setPattern(e.target.value)} />
            <div className="field-hint">Placeholders : {"{target_var_name}"} et {"{value}"}. Le defaut convient pour le pilote Converter.</div>
          </div>
          {err && <div className="alert alert-err">{err}</div>}
        </div>
      </Modal>
    </div>
  );
}
