import { ReactNode } from "react";

type Props = {
  title?: ReactNode;
  subtitle?: ReactNode;
  actions?: ReactNode;
  footer?: ReactNode;
  flush?: boolean;
  children: ReactNode;
};

export function Card({ title, subtitle, actions, footer, flush, children }: Props) {
  return (
    <section className="card">
      {(title || actions) && (
        <header className="card-header">
          <div>
            {title && <h3>{title}</h3>}
            {subtitle && <div className="card-header-sub">{subtitle}</div>}
          </div>
          {actions && <div className="row-tight">{actions}</div>}
        </header>
      )}
      <div className={flush ? "card-body-flush" : "card-body"}>{children}</div>
      {footer && <div className="card-footer">{footer}</div>}
    </section>
  );
}
