type Props = { confidence: string };
const VARIANT: Record<string, string> = { high: "badge-ok", medium: "badge-warn", low: "badge-err", manual: "badge-neutral" };
export function ConfidenceBadge({ confidence }: Props) {
  return <span className={"badge " + (VARIANT[confidence] || "badge-neutral")}>{confidence}</span>;
}
