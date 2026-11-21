import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { ArrowLeft, ArrowUpRight, Bookmark, Check, CircleAlert, ExternalLink, Lightbulb, LoaderCircle, LockKeyhole, Sparkles } from 'lucide-react'
import { api } from '../lib/api'
import type { QuestionAttempt, QuestionDetail } from '../lib/types'

const difficultyLabel = { BEGINNER: 'Beginner', INTERMEDIATE: 'Intermediate', ADVANCED: 'Advanced' }
type CoachMode = 'HINT'|'APPROACH'|'COMPLEXITY'|'SOLUTION'

export function QuestionDetailPage() {
  const { slug = '' } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [question, setQuestion] = useState<QuestionDetail | null>(null)
  const [attempts, setAttempts] = useState<QuestionAttempt[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [coachError, setCoachError] = useState('')
  const [coachBusy, setCoachBusy] = useState(false)
  const [coachContent, setCoachContent] = useState<Partial<Record<CoachMode,string>>>({})
  const [coachSource, setCoachSource] = useState<Partial<Record<CoachMode,boolean>>>({})
  const [progressBusy, setProgressBusy] = useState(false)
  const returnToParam = params.get('returnTo')
  const returnTo = returnToParam?.startsWith('/questions') && !returnToParam.startsWith('//') ? returnToParam : '/questions'

  async function load() {
    setLoading(true); setError('')
    try {
      const [detail, history] = await Promise.all([
        api<QuestionDetail>(`/api/questions/${encodeURIComponent(slug)}`),
        api<QuestionAttempt[]>(`/api/questions/${encodeURIComponent(slug)}/attempts`),
      ])
      setQuestion(detail); setAttempts(history)
    } catch (e) { setError(e instanceof Error ? e.message : 'Question could not be loaded.') }
    finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [slug])

  async function updateProgress(values: {status?:QuestionDetail['progress']['status'];bookmarked?:boolean}) {
    if (!question) return
    setProgressBusy(true); setError('')
    try {
      const updated = await api<QuestionDetail>(`/api/questions/${encodeURIComponent(slug)}/progress`, {
        method: 'PATCH', body: JSON.stringify({ status: values.status ?? question.progress.status, bookmarked: values.bookmarked ?? question.progress.bookmarked }),
      })
      setQuestion(updated)
      setAttempts(await api<QuestionAttempt[]>(`/api/questions/${encodeURIComponent(slug)}/attempts`))
    } catch (e) { setError(e instanceof Error ? e.message : 'Progress could not be saved.') }
    finally { setProgressBusy(false) }
  }

  async function requestCoach(mode: CoachMode) {
    if (!question) return
    setCoachBusy(true); setCoachError('')
    try {
      const response = await api<{mode:CoachMode;content:string;hintsRevealed:number;aiGenerated:boolean}>(`/api/questions/${encodeURIComponent(slug)}/coach`, {
        method: 'POST', body: JSON.stringify({ mode, hintIndex: mode === 'HINT' ? question.progress.hintsRevealed : undefined }),
      })
      setCoachContent(current => ({ ...current, [mode]: mode === 'HINT' ? `${current.HINT ? `${current.HINT}\n\n` : ''}${response.content}` : response.content }))
      setCoachSource(current => ({ ...current, [mode]: response.aiGenerated }))
      if (mode === 'HINT') setQuestion(current => current ? { ...current, progress: { ...current.progress, hintsRevealed: response.hintsRevealed }, revealedHints: [...current.revealedHints, response.content] } : current)
    } catch (e) { setCoachError(e instanceof Error ? e.message : 'The study coach could not respond.') }
    finally { setCoachBusy(false) }
  }

  if (loading) return <div className="question-state"><LoaderCircle size={20} className="spin"/><span>Loading question…</span></div>
  if (error && !question) return <section className="panel question-state question-error"><CircleAlert size={20}/><b>Question unavailable</b><span>{error}</span><button className="button-secondary" onClick={() => navigate(returnTo)}>Back to questions</button><button className="button-quiet" onClick={() => void load()}>Try again</button></section>
  if (!question) return null
  const nextHint = question.progress.hintsRevealed
  const hintsLeft = nextHint < question.hintCount
  return <>
    <button className="question-back-link" onClick={() => navigate(returnTo)}><ArrowLeft size={15}/> Back to questions</button>
    <div className="question-detail-layout">
      <main className="question-detail-main">
        <section className="panel question-detail-hero">
          <div className="question-detail-meta"><span className={`difficulty difficulty-${question.difficulty.toLowerCase()}`}>{difficultyLabel[question.difficulty]}</span><span className="question-id">{question.id}</span><a href={question.sourceUrl} target="_blank" rel="noreferrer" className="question-source-link">{question.sourceName}<ExternalLink size={13}/></a></div>
          <h1>{question.title}</h1>
          <div className="question-topic-list">{question.topics.map(t => <span className="topic-chip" key={t}>{t}</span>)}</div>
          <p className="question-description">{question.description}</p>
          <div className="question-action-row">
            <label className="question-status-control">My status<select disabled={progressBusy} value={question.progress.status} onChange={e => void updateProgress({status:e.target.value as QuestionDetail['progress']['status']})}><option value="TODO">To do</option><option value="ATTEMPTED">Attempted</option><option value="SOLVED">Solved</option></select></label>
            <button type="button" className={`button-secondary ${question.progress.bookmarked ? 'question-bookmarked' : ''}`} disabled={progressBusy} onClick={() => void updateProgress({bookmarked:!question.progress.bookmarked})}><Bookmark size={15} fill={question.progress.bookmarked ? 'currentColor' : 'none'}/>{question.progress.bookmarked ? 'Bookmarked' : 'Bookmark'}</button>
            <Link className="button-primary" to={`/analyze?question=${encodeURIComponent(question.slug)}`}><Sparkles size={15}/> Review my solution <ArrowUpRight size={14}/></Link>
          </div>
          {error && <div className="inline-error">{error}</div>}
        </section>
        <section className="panel question-content-panel"><span className="section-eyebrow">PROBLEM</span><h2>Examples</h2>
          <div className="question-example-list">{question.examples.map((example, index) => <article className="question-example" key={`${index}-${example.input}`}><b>Example {index + 1}</b><div><span>Input</span><code>{example.input}</code></div><div><span>Output</span><code>{example.output}</code></div>{example.explanation && <p>{example.explanation}</p>}</article>)}</div>
          <h2>Constraints</h2><ul className="question-constraints">{question.constraints.map((line, index) => <li key={index}><code>{line}</code></li>)}</ul>
          <div className="question-source-attribution">Source: <a href={question.sourceUrl} target="_blank" rel="noreferrer">{question.sourceName} <ExternalLink size={12}/></a>. Statement and examples are presented as a concise paraphrase; see the source for the official wording.</div>
        </section>
        <section className="panel question-coach-panel"><div className="panel-heading"><div><span className="section-eyebrow">ASK WHEN YOU’RE READY</span><h2><Lightbulb size={17}/> Study coach</h2></div><span className="coach-private-note"><LockKeyhole size={13}/> Solutions stay hidden until requested</span></div>
          <p>Reveal one hint at a time, or request a specific explanation.</p>
          <div className="coach-actions"><button className="button-secondary" disabled={coachBusy||!hintsLeft} onClick={() => void requestCoach('HINT')}><Lightbulb size={14}/>{coachBusy ? 'Thinking…' : hintsLeft ? `Reveal hint ${nextHint + 1}` : 'No more hints'}</button><button className="button-secondary" disabled={coachBusy} onClick={() => void requestCoach('APPROACH')}>Approach</button><button className="button-secondary" disabled={coachBusy} onClick={() => void requestCoach('COMPLEXITY')}>Complexity</button><button className="button-secondary solution-request" disabled={coachBusy||Boolean(coachContent.SOLUTION)} onClick={() => void requestCoach('SOLUTION')}>Reveal full solution</button></div>
          {coachBusy && <div className="coach-loading"><LoaderCircle size={15} className="spin"/> Preparing that explanation…</div>}
          {coachError && <div className="inline-error">{coachError}</div>}
          {question.revealedHints.length > 0 && <div className="coach-revealed"><b>Hints revealed</b>{question.revealedHints.map((hint,index)=><article key={index}><span>Hint {index + 1}</span><p>{hint}</p></article>)}</div>}
          {(['APPROACH','COMPLEXITY','SOLUTION'] as const).map(mode => coachContent[mode] && <article className={`coach-revealed coach-${mode.toLowerCase()}`} key={mode}><b>{mode === 'APPROACH' ? 'Approach' : mode === 'COMPLEXITY' ? 'Complexity analysis' : 'Solution explanation'}</b><span className="coach-source-label">{coachSource[mode] ? 'AI-assisted' : 'Curated guidance'}</span><p>{coachContent[mode]}</p></article>)}
        </section>
      </main>
      <aside className="panel question-history-panel"><span className="section-eyebrow">YOUR PRACTICE</span><h2>Submission history</h2><span className="question-history-count">{attempts.length} attempt{attempts.length === 1 ? '' : 's'}</span>{attempts.length ? <div className="question-attempt-list">{attempts.map(attempt=><article className="question-attempt" key={attempt.id}><span className={`attempt-outcome ${attempt.outcome.toLowerCase()}`}>{attempt.outcome === 'SOLVED' ? <Check size={13}/> : <Lightbulb size={13}/>} {attempt.outcome === 'SOLVED' ? 'Solved' : 'Attempted'}</span><time>{new Date(attempt.attemptedAt).toLocaleString()}</time>{attempt.score != null && <b>{attempt.score}/100 <small>{attempt.language}</small></b>}{attempt.submissionId && <Link to={`/submissions?id=${attempt.submissionId}`}>Open review <ArrowUpRight size={12}/></Link>}</article>)}</div> : <div className="question-history-empty"><span>Mark an attempt or review your code to start tracking your history.</span></div>}</aside>
    </div>
  </>
}
