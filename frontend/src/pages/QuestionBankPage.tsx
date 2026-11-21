import { useEffect, useState } from 'react'
import { Link, useLocation, useSearchParams } from 'react-router-dom'
import { Bookmark, ChevronLeft, ChevronRight, CircleHelp, ExternalLink, LoaderCircle, Search, SlidersHorizontal } from 'lucide-react'
import { api } from '../lib/api'
import type { QuestionPage, QuestionProgressSummary, QuestionSummary } from '../lib/types'
import './question-bank.css'

const difficultyLabel = { BEGINNER: 'Beginner', INTERMEDIATE: 'Intermediate', ADVANCED: 'Advanced' }

export function QuestionBankPage() {
  const [params, setParams] = useSearchParams()
  const location = useLocation()
  const [result, setResult] = useState<QuestionPage | null>(null)
  const [topics, setTopics] = useState<string[]>([])
  const [topicsError, setTopicsError] = useState('')
  const [progress, setProgress] = useState<QuestionProgressSummary | null>(null)
  const [progressError, setProgressError] = useState('')
  const [refresh, setRefresh] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const q = params.get('q') || ''
  const topic = params.get('topic') || ''
  const difficulty = params.get('difficulty') || ''
  const sort = params.get('sort') || 'title'
  const page = Math.max(1, Number(params.get('page') || '1'))

  useEffect(() => { api<string[]>('/api/questions/topics').then(setTopics).catch(e => setTopicsError(e instanceof Error ? e.message : 'Topics could not load.')) }, [])
  useEffect(() => { api<QuestionProgressSummary>('/api/questions/progress/summary').then(setProgress).catch(e => setProgressError(e instanceof Error ? e.message : 'Progress could not load.')) }, [])
  useEffect(() => {
    const controller = new AbortController()
    const query = new URLSearchParams({ page: String(page), size: '12', sort })
    if (q.trim()) query.set('q', q.trim())
    if (topic) query.set('topic', topic)
    if (difficulty) query.set('difficulty', difficulty)
    setLoading(true); setError('')
    api<QuestionPage>(`/api/questions?${query}`, { signal: controller.signal })
      .then(data => { if (!controller.signal.aborted) setResult(data) })
      .catch(e => { if (!controller.signal.aborted) setError(e instanceof Error ? e.message : 'Question bank could not load.') })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [q, topic, difficulty, sort, page, refresh])

  function change(name: string, value: string) {
    const next = new URLSearchParams(params)
    if (value) next.set(name, value); else next.delete(name)
    if (name !== 'page') next.delete('page')
    setParams(next)
  }

  const returnTo = `${location.pathname}${location.search}`
  return <>
    <div className="page-heading question-bank-heading"><div><span className="eyebrow">CURATED PRACTICE</span><h1>Question bank</h1><p>Browse source-linked DSA problems. Your progress and bookmarks are saved to your account.</p></div><div className="study-heading-icon"><CircleHelp size={20}/></div></div>
    <section className="panel question-bank-panel">
      {progress && <div className="question-progress-summary" aria-label="Question bank progress"><div><b>{progress.solved}</b><span>Solved</span></div><div><b>{progress.attempted}</b><span>Attempted</span></div><div><b>{progress.todo}</b><span>To do</span></div><div><b>{progress.bookmarked}</b><span>Bookmarked</span></div><div className="question-progress-total">{progress.total} questions</div></div>}
      {progressError && <div className="inline-error">Question progress could not load: {progressError}</div>}
      <div className="question-bank-toolbar">
        <label className="question-search"><Search size={16}/><input aria-label="Search questions" value={q} onChange={e => change('q', e.target.value)} placeholder="Search by title or problem ID…"/></label>
        <div className="question-filters"><SlidersHorizontal size={15}/>
          <select aria-label="Filter topic" value={topic} onChange={e => change('topic', e.target.value)}><option value="">All topics</option>{topics.map(t => <option key={t}>{t}</option>)}</select>
          <select aria-label="Filter difficulty" value={difficulty} onChange={e => change('difficulty', e.target.value)}><option value="">All levels</option><option value="BEGINNER">Beginner</option><option value="INTERMEDIATE">Intermediate</option><option value="ADVANCED">Advanced</option></select>
          <select aria-label="Sort questions" value={sort} onChange={e => change('sort', e.target.value)}><option value="title">Title</option><option value="difficulty">Difficulty</option></select>
        </div>
      </div>
      {topicsError && <div className="inline-error">Topics could not load: {topicsError} <button className="button-quiet" onClick={() => {setTopicsError('');api<string[]>('/api/questions/topics').then(setTopics).catch(e => setTopicsError(e instanceof Error ? e.message : 'Topics could not load.'))}}>Retry</button></div>}
      {error && <div className="question-state question-error"><b>Couldn’t load questions</b><span>{error}</span><button className="button-secondary" onClick={() => setRefresh(value => value + 1)}>Try again</button></div>}
      {loading && <div className="question-state"><LoaderCircle className="spin" size={20}/><span>Loading questions…</span></div>}
      {!loading && !error && result && <>
        <div className="question-result-count">{result.totalItems} question{result.totalItems === 1 ? '' : 's'}{q ? ` matching “${q}”` : ''}</div>
        {result.items.length ? <div className="question-card-grid">{result.items.map((item: QuestionSummary) => <Link className="question-card" key={item.id} to={`/questions/${item.slug}?returnTo=${encodeURIComponent(returnTo)}`}>
          <div className="question-card-top"><span className={`difficulty difficulty-${item.difficulty.toLowerCase()}`}>{difficultyLabel[item.difficulty]}</span>{item.bookmarked && <Bookmark size={15} fill="currentColor" aria-label="Bookmarked"/>}</div>
          <h2>{item.title}</h2><span className="question-id">{item.id}</span>
          <div className="question-topic-list">{item.topics.map(t => <span className="topic-chip" key={t}>{t}</span>)}</div>
          <div className="question-card-bottom"><span className={`question-status status-${item.status.toLowerCase()}`}>{item.status === 'TODO' ? 'To do' : item.status === 'ATTEMPTED' ? 'Attempted' : 'Solved'}</span><span className="question-source">{item.sourceName}<ExternalLink size={12}/></span></div>
        </Link>)}</div> : <div className="question-state"><b>No questions match these filters.</b><span>Try another search, topic, or difficulty.</span><button className="button-quiet" onClick={() => setParams(new URLSearchParams())}>Clear filters</button></div>}
        {result.totalPages > 1 && <div className="question-pagination"><button className="button-secondary" disabled={!result.hasPrevious} onClick={() => change('page', String(page - 1))}><ChevronLeft size={15}/> Previous</button><span>Page {result.page} of {result.totalPages}</span><button className="button-secondary" disabled={!result.hasNext} onClick={() => change('page', String(page + 1))}>Next <ChevronRight size={15}/></button></div>}
      </>}
    </section>
  </>
}
