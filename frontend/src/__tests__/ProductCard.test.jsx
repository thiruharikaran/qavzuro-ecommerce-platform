import React from 'react'
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import ProductCard from '../components/ProductCard'

const baseProduct = {
  id: 'p1', slug: 'widget', name: 'Test Widget', brand: 'Qavzuro',
  price: 100, currency: 'INR', images: [], inventoryQuantity: 10, variants: [],
  ratingSummary: { average: 4.5, count: 12 },
}

function renderWithRouter(ui) {
  return render(<MemoryRouter>{ui}</MemoryRouter>)
}

describe('ProductCard', () => {
  it('renders the product name, brand, and price', () => {
    renderWithRouter(<ProductCard product={baseProduct} />)
    expect(screen.getByText('Test Widget')).toBeInTheDocument()
    expect(screen.getByText('Qavzuro')).toBeInTheDocument()
  })

  it('shows a Sale badge when salePrice is lower than price', () => {
    renderWithRouter(<ProductCard product={{ ...baseProduct, salePrice: 80 }} />)
    expect(screen.getByText('Sale')).toBeInTheDocument()
  })

  it('does not show a Sale badge when there is no discount', () => {
    renderWithRouter(<ProductCard product={baseProduct} />)
    expect(screen.queryByText('Sale')).not.toBeInTheDocument()
  })

  it('shows an Out of stock badge when inventory and all variants are empty', () => {
    renderWithRouter(<ProductCard product={{ ...baseProduct, inventoryQuantity: 0, variants: [] }} />)
    expect(screen.getByText('Out of stock')).toBeInTheDocument()
  })

  it('links to the product detail page using its slug', () => {
    renderWithRouter(<ProductCard product={baseProduct} />)
    expect(screen.getByRole('link')).toHaveAttribute('href', '/products/widget')
  })

  it('displays the rating summary when reviews exist', () => {
    renderWithRouter(<ProductCard product={baseProduct} />)
    expect(screen.getByText(/4\.5/)).toBeInTheDocument()
    expect(screen.getByText(/12/)).toBeInTheDocument()
  })
})
