import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { api } from '../lib/api'
import { QuestionBankPage } from './QuestionBankPage'
import { QuestionDetailPage } from './QuestionDetailPage'
import type { QuestionDetail, QuestionPage } from '../lib/types'

vi.mock('../lib/api', () => ({ api: vi.fn() }))
const mockedApi = vi.mocked(api)

const detail: QuestionDetail = {
  id: 'LC-1', slug: 'two-sum', title: 'Two Sum', description: 'Find two distinct values that add to a target.',
  examples: [{ input: 'nums=[2,7], target=9', output: '[0,1]', explanation: 'The values add to 9.' }],
  constraints: ['2 <= n <= 10^4'], difficulty: 'BEGINNER', topics: ['Arrays and Strings', 'Hashing'],
  sourceName: 'LeetCode #1', sourceUrl: 'https://leetcode.com/problems/two-sum/',
  progress: { status: 'TODO', bookmarked: false, hintsRevealed: 0 }, revealedHints: [], hintCount: 2,
}
const page: QuestionPage = { items: [{ id: 'LC-1', slug: 'two-sum', title: 'Two Sum', difficulty: 'BEGINNER', topics: ['Hashing'], sourceName: 'LeetCode #1', sourceUrl: detail.sourceUrl, status: 'TODO', bookmarked: false }], page: 2, size: 12, totalItems: 13, totalPages: 2, hasNext: false, hasPrevious: true }

function renderRoutes(initialEntry = '/questions?q=LC-1&topic=Hashing&page=2') {
  return render(<MemoryRouter initialEntries={[initialEntry]}><Routes>
    <Route path="/questions" element={<QuestionBankPage/>}/>
    <Route path="/questions/:slug" element={<QuestionDetailPage/>}/>
  </Routes></MemoryRouter>)
}

beforeEach(() => {
  mockedApi.mockReset()
  mockedApi.mockImplementation(async (path: string, options?: RequestInit) => {
    if (path === '/api/questions/topics') return ['Hashing', 'Arrays and Strings'] as never
    if (path === '/api/questions/progress/summary') return { total:17,todo:17,attempted:0,solved:0,bookmarked:0 } as never
    if (path.startsWith('/api/questions?')) return page as never
    if (path.endsWith('/attempts')) return [] as never
    if (path.endsWith('/progress')) return { ...detail, progress: { status: 'SOLVED', bookmarked: true, hintsRevealed: 0 } } as never
    if (path.endsWith('/coach')) return { mode: 'HINT', content: 'Check for a complement.', hintsRevealed: 1, aiGenerated: false } as never
    if (path === '/api/questions/two-sum') return detail as never
    return Promise.reject(new Error(`Unexpected request: ${path} ${options?.method || 'GET'}`))
  })
})
afterEach(cleanup)

describe('question bank and detail navigation', () => {
  it('opens a card and returns to the same filtered, paginated URL', async () => {
    renderRoutes()
    const card = await screen.findByRole('link', { name: /Two Sum/ })
    expect(card.getAttribute('href')).toContain('returnTo=%2Fquestions%3Fq%3DLC-1%26topic%3DHashing%26page%3D2')
    fireEvent.click(card)
    expect(await screen.findByRole('heading', { name: 'Two Sum' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: /Back to questions/ }))
    await waitFor(() => expect(screen.getByRole('textbox', { name: /Search questions/ })).toHaveValue('LC-1'))
    expect(mockedApi).toHaveBeenCalledWith(expect.stringContaining('page=2'), expect.anything())
  })

  it('keeps filters in the URL and requests a new search', async () => {
    renderRoutes('/questions')
    const search = await screen.findByRole('textbox', { name: /Search questions/ })
    fireEvent.change(search, { target: { value: 'LC-1' } })
    await waitFor(() => expect(mockedApi).toHaveBeenCalledWith(expect.stringContaining('q=LC-1'), expect.anything()))
    fireEvent.change(screen.getByRole('combobox', { name: 'Filter difficulty' }), { target: { value: 'BEGINNER' } })
    await waitFor(() => expect(mockedApi).toHaveBeenCalledWith(expect.stringContaining('difficulty=BEGINNER'), expect.anything()))
  })

  it('directly loads a detail route, saves bookmark/status, and reveals hints only on request', async () => {
    renderRoutes('/questions/two-sum')
    expect(await screen.findByText('Find two distinct values that add to a target.')).toBeInTheDocument()
    expect(screen.queryByText(/For each value/)).not.toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Reveal hint 1' }))
    expect(await screen.findByText('Check for a complement.')).toBeInTheDocument()
    fireEvent.change(screen.getByRole('combobox', { name: 'My status' }), { target: { value: 'SOLVED' } })
    await waitFor(() => expect(mockedApi).toHaveBeenCalledWith('/api/questions/two-sum/progress', expect.objectContaining({ method: 'PATCH' })))
    const source = screen.getAllByRole('link', { name: /LeetCode #1/ })[0]
    expect(source).toHaveAttribute('href', detail.sourceUrl)
  })

  it('offers a clear not-found state for invalid question slugs', async () => {
    mockedApi.mockImplementation(async (path: string) => {
      if (path.endsWith('/attempts')) return [] as never
      if (path === '/api/questions/no-such-question') throw new Error('Question not found')
      return [] as never
    })
    renderRoutes('/questions/no-such-question')
    const state = await screen.findByText('Question unavailable')
    expect(within(state.parentElement!).getByText('Question not found')).toBeInTheDocument()
  })
})
