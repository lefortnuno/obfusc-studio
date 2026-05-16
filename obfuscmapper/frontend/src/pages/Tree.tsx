import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { apiGet } from "../lib/api";

type Project = { id: string; name: string };

type FileNode = {
  id: string;
  folder_id: string | null;
  name: string;
  obf_name: string;
  relative_path: string;
  language: string;
  is_default: boolean;
  validated: boolean;
};

type FolderNode = {
  id: string;
  parent_id: string | null;
  name: string;
  obf_name: string;
  path: string;
  is_default: boolean;
  validated: boolean;
  children: FolderNode[];
  files: FileNode[];
};

type Tree = { project_id: string; name: string; tree: FolderNode[] };

function NodeView({ node, depth }: { node: FolderNode; depth: number }) {
  return (
    <div>
      <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "4px 0", paddingLeft: depth * 18 }}>
        <span style={{ fontFamily: "var(--font-mono)", color: node.is_default ? "var(--muted-2)" : "var(--accent)", fontSize: 12 }}>{node.is_default ? "■" : "▾"}</span>
        <span style={{ fontWeight: 500, fontSize: 13 }}>{node.name}</span>
        {node.obf_name && node.obf_name !== node.name && (
          <span className="muted text-xs mono">→ {node.obf_name}</span>
        )}
        {node.is_default && <span className="badge badge-neutral">default</span>}
      </div>
      {node.children.map(c => <NodeView key={c.id} node={c} depth={depth + 1} />)}
      {node.files.map(f => (
        <div key={f.id} style={{ display: "flex", alignItems: "center", gap: 8, padding: "3px 0", paddingLeft: (depth + 1) * 18 }}>
          <span style={{ fontFamily: "var(--font-mono)", color: "var(--muted-2)", fontSize: 12 }}>{"·"}</span>
          <span style={{ fontSize: 13 }}>{f.name}</span>
          {f.obf_name && f.obf_name !== f.name && <span className="muted text-xs mono">→ {f.obf_name}</span>}
          {f.is_default && <span className="badge badge-neutral">default</span>}
        </div>
      ))}
    </div>
  );
}

export default function Tree() {
  const projects = useQuery<Project[]>({ queryKey: ["projects"], queryFn: () => apiGet("/projects/") });
  const [selected, setSelected] = useState("");
  const tree = useQuery<Tree>({
    queryKey: ["tree", selected],
    queryFn: () => apiGet("/projects/" + selected + "/tree/"),
    enabled: !!selected,
  });

  return (
    <div>
      <PageHeader
        eyebrow="03 · Source"
        title="Arborescence"
        subtitle="Visualise la structure d un projet apres analyse, y compris les noms obfusques proposes."
      />
      <Card>
        <div className="field" style={{ maxWidth: 360, marginBottom: 0 }}>
          <label className="field-label">Projet</label>
          <select className="select" value={selected} onChange={e => setSelected(e.target.value)}>
            <option value="">Selectionner un projet...</option>
            {projects.data?.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </div>
      </Card>
      {tree.isFetching && <Card><div className="muted">Chargement...</div></Card>}
      {tree.data && (
        tree.data.tree.length === 0 ? (
          <Card>
            <Empty icon="◌" title="Arborescence vide" text="Lance une analyse sur le projet pour peupler l arborescence (page Projets, bouton Analyser)." />
          </Card>
        ) : (
          <Card title={tree.data.name}>
            <div className="stack-sm" style={{ fontFamily: "system-ui" }}>
              {tree.data.tree.map(n => <NodeView key={n.id} node={n} depth={0} />)}
            </div>
          </Card>
        )
      )}
    </div>
  );
}
