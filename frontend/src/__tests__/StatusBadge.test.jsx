import React from 'react'
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import StatusBadge from '../components/StatusBadge'

describe('StatusBadge', () => {
  it('renders the status text with underscores replaced by spaces', () => {
    render(<StatusBadge status="OUT_FOR_DELIVERY" />)
    expect(screen.getByText('OUT FOR DELIVERY')).toBeInTheDocument()
  })

  it('renders known statuses without throwing', () => {
    const statuses = ['PENDING', 'CONFIRMED', 'DELIVERED', 'CANCELLED', 'REFUNDED']
    statuses.forEach((s) => {
      render(<StatusBadge status={s} />)
    })
    expect(screen.getAllByText(/PENDING|CONFIRMED|DELIVERED|CANCELLED|REFUNDED/).length).toBeGreaterThan(0)
  })
})
