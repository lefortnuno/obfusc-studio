import { BrowserRouter, Routes, Route, NavLink } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import Dashboard from "./pages/Dashboard";
import Projects from "./pages/Projects";
import Pairs from "./pages/Pairs";
import Tree from "./pages/Tree";
import Variables from "./pages/Variables";
import Validation from "./pages/Validation";
import Mapping from "./pages/Mapping";
import Generate from "./pages/Generate";
import Apply from "./pages/Apply";
import Audit from "./pages/Audit";
import Obfuscate from "./pages/Obfuscate";
import Rotation from "./pages/Rotation";
import "./App.css";

const qc = new QueryClient({
  defaultOptions: { queries: { staleTime: 5_000, refetchOnWindowFocus: false } },
});

type NavItem = { to: string; num: string; label: string; end?: boolean };

const NAV_GROUPS: { title: string; items: NavItem[] }[] = [
  { title: "Vue", items: [
    { to: "/", num: "—", label: "Dashboard", end: true },
  ]},
  { title: "Configuration", items: [
    { to: "/projects", num: "01", label: "Projets" },
    { to: "/pairs", num: "02", label: "Paires" },
  ]},
  { title: "Source", items: [
    { to: "/tree", num: "03", label: "Arborescence" },
    { to: "/variables", num: "04", label: "Variables" },
    { to: "/validation", num: "05", label: "Validation" },
  ]},
  { title: "Pipeline", items: [
    { to: "/mapping", num: "06", label: "Mapping" },
    { to: "/obfuscate", num: "07", label: "Obfusquer (auto)" },
    { to: "/generate", num: "08", label: "Generation" },
    { to: "/apply", num: "09", label: "Apply" },
    { to: "/audit", num: "10", label: "Audit" },
  ]},
  { title: "Securite", items: [
    { to: "/rotation", num: "11", label: "Rotation cle" },
  ]},
];

export default function App() {
  return (
    <QueryClientProvider client={qc}>
      <BrowserRouter>
        <div className="app-shell">
          <aside className="sidebar">
            <div className="brand">
              <span className="brand-logo">obfuscmapper</span>
              <span className="brand-tag">v0.1</span>
            </div>
            {NAV_GROUPS.map(g => (
              <div className="nav-group" key={g.title}>
                <div className="nav-group-title">{g.title}</div>
                {g.items.map(it => (
                  <NavLink key={it.to} to={it.to} end={it.end} className={({ isActive }) => "nav-link" + (isActive ? " active" : "") }>
                    <span className="nav-num">{it.num}</span>
                    <span>{it.label}</span>
                  </NavLink>
                ))}
              </div>
            ))}
            <div className="sidebar-footer">obfusc-studio · localhost:8000</div>
          </aside>
          <main className="main">
            <div className="main-inner">
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/projects" element={<Projects />} />
                <Route path="/pairs" element={<Pairs />} />
                <Route path="/tree" element={<Tree />} />
                <Route path="/variables" element={<Variables />} />
                <Route path="/validation" element={<Validation />} />
                <Route path="/mapping" element={<Mapping />} />
                <Route path="/generate" element={<Generate />} />
                <Route path="/apply" element={<Apply />} />
                <Route path="/audit" element={<Audit />} />
                <Route path="/obfuscate" element={<Obfuscate />} />
                <Route path="/rotation" element={<Rotation />} />
              </Routes>
            </div>
          </main>
        </div>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
