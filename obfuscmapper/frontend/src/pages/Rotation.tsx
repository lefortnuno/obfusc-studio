import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { PageHeader } from "../components/PageHeader";
import { Card } from "../components/Card";
import { apiPost } from "../lib/api";

export default function Rotation() {
  const [oldp, setOldp] = useState("");
  const [newp, setNewp] = useState("");
  const [confirm, setConfirm] = useState("");
  const [result, setResult] = useState("");
  const [err, setErr] = useState("");
  const run = useMutation({
    mutationFn: () => apiPost<{ task_id: string; status: string }>("/master-key/rotate/", { old_password: oldp, new_password: newp, confirm_new: confirm }),
    onSuccess: (d) => { setResult(JSON.stringify(d)); setErr(""); setOldp(""); setNewp(""); setConfirm(""); },
    onError: (e: Error) => setErr(e.message),
  });
  const passwordsMatch = newp && newp === confirm;
  return (
    <div>
      <PageHeader
        eyebrow="10 · Securite"
        title="Rotation du mot de passe maitre"
        subtitle="Le mot de passe maitre derive la cle Fernet qui chiffre toutes les valeurs sensibles. A faire tous les mois."
      />
      <Card>
        <div className="alert alert-warn" style={{ marginBottom: 20 }}>
          <strong>Attention :</strong> la rotation rechiffre toutes les cles XOR, valeurs evaluees et mappings en base. Verifie d abord que l ancien mot de passe est correct (au runserver via env MASTER_PASSWORD).
        </div>
        <div className="stack">
          <div className="field">
            <label className="field-label">Ancien mot de passe</label>
            <input className="input" type="password" value={oldp} onChange={e => setOldp(e.target.value)} />
          </div>
          <div className="form-row">
            <div className="field">
              <label className="field-label">Nouveau mot de passe</label>
              <input className="input" type="password" value={newp} onChange={e => setNewp(e.target.value)} />
            </div>
            <div className="field">
              <label className="field-label">Confirmer</label>
              <input className="input" type="password" value={confirm} onChange={e => setConfirm(e.target.value)} />
              {confirm && !passwordsMatch && <div className="field-hint" style={{ color: "var(--err)" }}>Les mots de passe ne correspondent pas.</div>}
            </div>
          </div>
          <div>
            <button className="btn btn-accent" onClick={() => run.mutate()} disabled={!oldp || !passwordsMatch || run.isPending}>
              {run.isPending ? <><span className="spinner" /> Rotation en cours...</> : "Lancer la rotation"}
            </button>
          </div>
          {err && <div className="alert alert-err">{err}</div>}
          {result && <div className="alert alert-ok">Rotation declenchee : {result}</div>}
        </div>
      </Card>
    </div>
  );
}
