type Props = { status: string };

const VARIANT: Record<string, { cls: string; label: string }> = {
  pending: { cls: "status-dot-pending", label: "En attente" },
  running: { cls: "status-dot-running", label: "En cours" },
  done: { cls: "status-dot-done", label: "Termine" },
  failed: { cls: "status-dot-failed", label: "Echec" },
  ok: { cls: "status-dot-done", label: "OK" },
  diff: { cls: "status-dot-failed", label: "DIFF" },
  non_found: { cls: "status-dot-pending", label: "Non trouve" },
};

export function StatusBadge({ status }: Props) {
  const key = status.toLowerCase();
  const v = VARIANT[key] || { cls: "status-dot-pending", label: status };
  return <span style={{ display: "inline-flex", alignItems: "center", fontSize: 12 }}><span className={"status-dot " + v.cls} />{v.label}</span>;
}
