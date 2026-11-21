import { useEffect, useMemo, useState } from 'react'
import { BookOpenCheck, Check, ExternalLink, ListChecks, Search } from 'lucide-react'
import studySet from '../data/algoforgeStudySet.json'
import { useAuth } from '../lib/AuthContext'

type StudyEntry = {
  id: string
  sheetRow: number
  section: string
  title: string
  links: string[]
  resourceLabel: string
  searchUrl: string
  note: string
}

const entries = studySet as StudyEntry[]
const sections = [...new Set(entries.map(item => item.section))]
const mergedDuplicateIds: Record<string, string> = {
  'algoforge-140':'algoforge-139', 'algoforge-655':'algoforge-278', 'algoforge-656':'algoforge-279',
  'algoforge-657':'algoforge-280', 'algoforge-658':'algoforge-281', 'algoforge-659':'algoforge-282',
  'algoforge-661':'algoforge-283', 'algoforge-662':'algoforge-284', 'algoforge-387':'algoforge-323',
}
const allTopics = 'ALL'

export function StudyPage() {
  const { user } = useAuth()
  const storageKey = `codementor-ai:algoforge-study:${user?.id ?? 'local'}`
  const [completed, setCompleted] = useState<Set<string>>(() => new Set())
  const [loadedKey, setLoadedKey] = useState('')
  const [search, setSearch] = useState('')
  const [topic, setTopic] = useState(allTopics)
  const [remainingOnly, setRemainingOnly] = useState(false)
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({ [sections[0]]: true })

  useEffect(() => {
    try {
      const saved = localStorage.getItem(storageKey)
      const ids = saved ? JSON.parse(saved) as string[] : []
      const canonicalIds = ids.map(id => mergedDuplicateIds[id] || id)
      setCompleted(new Set(canonicalIds))
    } catch {
      setCompleted(new Set())
    }
    setLoadedKey(storageKey)
  }, [storageKey])

  useEffect(() => {
    if (loadedKey !== storageKey) return
    try {
      localStorage.setItem(storageKey, JSON.stringify([...completed]))
    } catch {
      // The checklist remains usable for this session if browser storage is unavailable.
    }
  }, [completed, loadedKey, storageKey])

  const doneCount = entries.reduce((count, item) => count + Number(completed.has(item.id)), 0)
  const visible = useMemo(() => {
    const query = search.trim().toLowerCase()
    return entries.filter(item =>
      (topic === allTopics || item.section === topic) &&
      (!remainingOnly || !completed.has(item.id)) &&
      (!query || `${item.title} ${item.section} ${item.resourceLabel}`.toLowerCase().includes(query)),
    )
  }, [completed, remainingOnly, search, topic])

  const grouped = useMemo(() => {
    const map = new Map<string, StudyEntry[]>()
    for (const item of visible) {
      const group = map.get(item.section) ?? []
      group.push(item)
      map.set(item.section, group)
    }
    return [...map.entries()]
  }, [visible])

  function toggleDone(id: string) {
    setCompleted(current => {
      const next = new Set(current)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  function renderRows(items: StudyEntry[]) {
    return <div className="study-rows">{items.map(item => {
      const isDone = completed.has(item.id)
      return <article className={`study-row ${isDone ? 'study-row-done' : ''}`} key={item.id}>
        <div className="study-row-number">{item.sheetRow}</div>
        <div className="study-row-copy">
          <b>{item.title}</b>
          {item.note && <small>{item.note}</small>}
          {(item.links.length > 0 || item.searchUrl) && <div className="study-resources">
            {item.links.map((url, index) => <a href={url} target="_blank" rel="noreferrer" key={`${item.id}-link-${index}`}>
              {item.links.length > 1 ? `Resource ${index + 1}` : 'Open resource'} <ExternalLink size={12}/>
            </a>)}
            {item.searchUrl && <a href={item.searchUrl} target="_blank" rel="noreferrer">Find resource <ExternalLink size={12}/></a>}
          </div>}
        </div>
        <label className="study-check">
          <input type="checkbox" checked={isDone} onChange={() => toggleDone(item.id)} aria-label={`Mark ${item.title} as ${isDone ? 'not done' : 'done'}`}/>
          <span className="study-check-box"><Check size={13}/></span>
          <span>{isDone ? 'Done' : 'Mark done'}</span>
        </label>
      </article>
    })}</div>
  }

  return <>
    <div className="page-heading study-page-heading">
      <div><span className="eyebrow">ALGOFORGE DSA SHEET</span><h1>Study plan</h1><p>{entries.length} study items across {sections.length} topics. Check each one off as you finish.</p></div>
      <div className="study-heading-icon"><BookOpenCheck size={20}/></div>
    </div>

    <section className="study-progress panel">
      <div className="study-progress-icon"><ListChecks size={19}/></div>
      <div className="study-progress-copy"><span>YOUR SHEET PROGRESS</span><b>{doneCount} <small>of {entries.length} complete</small></b>
        <div className="study-progress-track"><i style={{ width: `${entries.length ? doneCount / entries.length * 100 : 0}%` }}/></div>
      </div>
      <div className="study-progress-percent">{Math.round(doneCount / entries.length * 100)}%</div>
    </section>

    <section className="panel study-panel">
      <div className="study-toolbar">
        <div className="study-list-title"><span className="section-eyebrow">YOUR CHECKLIST</span><h2>Study items <span className="count-tag">{visible.length}</span></h2></div>
        <div className="study-filters">
          <label className="study-search"><Search size={15}/><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Search topics or questions…"/></label>
          <select value={topic} onChange={event => setTopic(event.target.value)} aria-label="Filter by topic">
            <option value={allTopics}>All topics</option>
            {sections.map(section => <option key={section} value={section}>{section}</option>)}
          </select>
          <label className="study-remaining"><input type="checkbox" checked={remainingOnly} onChange={event => setRemainingOnly(event.target.checked)}/> Not done</label>
        </div>
      </div>
      <p className="study-link-note">Direct sheet links open directly. If the pasted sheet only included a link title, “Find resource” searches that title.</p>

      {visible.length ? topic === allTopics ? <div className="study-groups">{grouped.map(([section, items]) => {
        const fullSection = entries.filter(item => item.section === section)
        const sectionDone = fullSection.reduce((count, item) => count + Number(completed.has(item.id)), 0)
        const expanded = Boolean(search.trim()) || Boolean(openSections[section])
        return <section className="study-group" key={section}>
          <button className="study-group-toggle" type="button" aria-expanded={expanded} onClick={() => setOpenSections(current => ({ ...current, [section]: !current[section] }))}>
            <span><b>{section}</b><small>{sectionDone} of {fullSection.length} done</small></span>
            <span className="study-group-count">{items.length}</span>
            <span className={`study-chevron ${expanded ? 'study-chevron-open' : ''}`}/>
          </button>
          {expanded && renderRows(items)}
        </section>
      })}</div> : renderRows(visible) : <div className="empty-table"><b>No study items match.</b><span>Try another search or topic.</span></div>}
    </section>
  </>
}
