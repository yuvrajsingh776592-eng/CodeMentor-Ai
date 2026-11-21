import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { Activity } from 'lucide-react'
import { StatCard } from '../StatCard'

describe('StatCard', () => {
  it('shows a metric and its supporting context', () => {
    render(<StatCard label="Problems solved" value={12} caption="3 attempted" icon={<Activity />} />)
    expect(screen.getByText('Problems solved')).toBeInTheDocument()
    expect(screen.getByText('12')).toBeInTheDocument()
    expect(screen.getByText('3 attempted')).toBeInTheDocument()
  })
})
