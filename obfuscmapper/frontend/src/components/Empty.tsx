import { ReactNode } from "react";

type Props = { icon?: ReactNode; title: string; text?: string; action?: ReactNode };

export function Empty({ icon, title, text, action }: Props) {
  return (
    <div className="empty">
      {icon && <div className="empty-icon">{icon}</div>}
      <div className="empty-title">{title}</div>
      {text && <div className="empty-text">{text}</div>}
      {action}
    </div>
  );
}
