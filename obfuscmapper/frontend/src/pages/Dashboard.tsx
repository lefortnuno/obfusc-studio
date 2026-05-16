import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { Empty } from "../components/Empty";
import { StatusBadge } from "../components/StatusBadge";
import { apiGet } from "../lib/api";

type Health = { status: string; service: string };
type Version = { version: string };
type Project = { id: string; name: string };
type Pair = { id: string; name: string; source_project_name: string; target_project_name: string | null };
type Variable = { id: string; validated: boolean; confidence: string };
type Mapping = { id: string; applied_at: string | null; validated: boolean };
type Job = { id: string; status: string; mode?: string; finished_at?: string | null; project_name?: string; pair_name?: string; created_at: string };

export default function Dashboard() {
  const health = useQuery<Health>({ queryKey: ["health"], queryFn: () => apiGet("/health/"), refetchInterval: 30000 });
  const version = useQuery<Version>({ queryKey: ["version"], queryFn: () => apiGet("/version/") });
  const projects = useQuery<Project[]>({ queryKey: ["projects"], queryFn: () => apiGet("/projects/") });
  const pairs = useQuery<Pair[]>({ queryKey: ["pairs"], queryFn: () => apiGet("/project-pairs/") });
  const vars = useQuery<Variable[]>({ queryKey: ["variables"], queryFn: () => apiGet("/variables/") });
  const mappings = useQuery<Mapping[]>({ queryKey: ["mappings"], queryFn: () => apiGet("/mappings/") });
  const analysisJobs = useQuery<Job[]>({ queryKey: ["analysis-jobs"], queryFn: () => apiGet("/analysis-jobs/") });
  const applyJobs = useQuery<Job[]>({ queryKey: ["apply-jobs"], queryFn: () => apiGet("/apply-jobs/") });

  const stats = {
    projects: projects.data?.length ?? 0,
    pairs: pairs.data?.length ?? 0,
    vars_validated: vars.data?.filter(v => v.validated).length ?? 0,
    vars_pending: vars.data?.filter(v => !v.validated).length ?? 0,
    mappings: mappings.data?.length ?? 0,
    mappings_applied: mappings.data?.filter(m => m.applied_at).length ?? 0,
  };

  const recentJobs = [
    ...(analysisJobs.data || []).map(j => ({ ...j, kind: "analyse" as const })),
    ...(applyJobs.data || []).map(j => ({ ...j, kind: j.mode || "apply" as string })),
  ].sort((a, b) => (b.created_at > a.created_at ? 1 : -1)).slice(0, 8);

  return (
    <div>
      <PageHeader
        eyebrow="vue d ensemble"
        title="Dashboard"
        subtitle="Statut de la pipeline d obfuscation et derniere activite des jobs."
        actions={
          <span className="badge badge-ok">
            <span className="dot" /> {health.data?.status || "..."} · {version.data?.version || ""}
          </span>
        }
      />

      <div className="stat-grid">
        <div className="stat">
          <div className="stat-label">Projets</div>
          <div className="stat-value">{stats.projects}</div>
          <div className="stat-delta">{stats.pairs} paire(s) configuree(s)</div>
        </div>
        <div className="stat">
          <div className="stat-label">Variables</div>
          <div className="stat-value">{stats.vars_validated}<span className="muted" style={{ fontSize: 16, fontWeight: 500 }}> / {stats.vars_validated + stats.vars_pending}</span></div>
          <div className="stat-delta">{stats.vars_pending} en attente de validation</div>
        </div>
        <div className="stat">
          <div className="stat-label">Mappings</div>
          <div className="stat-value">{stats.mappings_applied}<span className="muted" style={{ fontSize: 16, fontWeight: 500 }}> / {stats.mappings}</span></div>
          <div className="stat-delta">appliques au projet cible</div>
        </div>
        <div className="stat">
          <div className="stat-label">API</div>
          <div className="stat-value" style={{ fontSize: 18 }}>{health.data?.status || "..."}</div>
          <div className="stat-delta mono">localhost:8000</div>
        </div>
      </div>

      <div className="split-2">
        <Card
          title="Pipeline d obfuscation"
          subtitle="Suivez l ordre des etapes ; chaque etape debloque la suivante."
        >
          <ol style={{ paddingLeft: 0, listStyle: "none", margin: 0 }}>
            {[
              { num: "01", to: "/projects", label: "Declarer les projets source + cible", done: stats.projects >= 2 },
              { num: "02", to: "/pairs", label: "Creer la paire avec la cle XOR", done: stats.pairs >= 1 },
              { num: "03", to: "/projects", label: "Analyser le projet source", done: (analysisJobs.data || []).some(j => j.status === "done") },
              { num: "04", to: "/validation", label: "Valider les variables detectees", done: stats.vars_validated > 0 },
              { num: "05", to: "/mapping", label: "Mapper source -> cible", done: stats.mappings > 0 },
              { num: "06", to: "/apply", label: "Appliquer les injections", done: stats.mappings_applied > 0 },
              { num: "07", to: "/audit", label: "Auditer le resultat", done: false },
            ].map((s, i, arr) => (
              <li key={s.num} style={{ display: "flex", gap: 12, padding: "8px 0", borderBottom: i < arr.length - 1 ? "1px solid var(--border)" : "none" }}>
                <span style={{ fontFamily: "var(--font-mono)", fontSize: 11, color: "var(--muted)", width: 24, paddingTop: 2 }}>{s.num}</span>
                <span style={{ flex: 1 }}>
                  <Link to={s.to} style={{ color: "var(--ink)", fontWeight: 500 }}>{s.label}</Link>
                </span>
                {s.done ? <span className="badge badge-ok">✓ fait</span> : <span className="badge badge-neutral">a faire</span>}
              </li>
            ))}
          </ol>
        </Card>

        <Card title="Jobs recents" subtitle="Analyse, generation, apply, audit confondus.">
          {recentJobs.length === 0 ? (
            <Empty icon="◉" title="Aucun job lance" text="Une fois un projet declare, lance une analyse pour commencer." />
          ) : (
            <div className="stack-sm">
              {recentJobs.map(j => (
                <div key={(j.kind + ":" + j.id)} style={{ display: "flex", alignItems: "center", gap: 12, padding: "8px 0", borderBottom: "1px solid var(--border)" }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 500, fontSize: 13 }}>{j.kind}</div>
                    <div className="text-xs muted">{(j as { project_name?: string; pair_name?: string }).project_name || (j as { pair_name?: string }).pair_name || "—"}</div>
                  </div>
                  <StatusBadge status={j.status} />
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
