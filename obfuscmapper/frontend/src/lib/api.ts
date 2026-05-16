const BASE = (import.meta.env.VITE_API_URL as string) || "/api";

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const r = await fetch(BASE + path, {
    headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
    ...init,
  });
  if (!r.ok) {
    const body = await r.text();
    throw new Error(r.status + " " + r.statusText + " - " + body);
  }
  return r.json() as Promise<T>;
}

export const apiGet = <T>(p: string) => api<T>(p);
export const apiPost = <T>(p: string, body: unknown) => api<T>(p, { method: "POST", body: JSON.stringify(body) });
export const apiPatch = <T>(p: string, body: unknown) => api<T>(p, { method: "PATCH", body: JSON.stringify(body) });
export const apiDel = <T>(p: string) => api<T>(p, { method: "DELETE" });
