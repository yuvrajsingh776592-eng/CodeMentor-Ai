import type { ReactNode } from 'react'
export function StatCard({ label, value, caption, icon, tint='violet' }: { label:string;value:string|number;caption:string;icon:ReactNode;tint?:string }) { return <article className="stat-card"><div className={`stat-icon ${tint}`}>{icon}</div><div className="stat-copy"><span>{label}</span><b>{value}</b><small>{caption}</small></div></article> }
