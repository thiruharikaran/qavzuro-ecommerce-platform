import React from 'react'
import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="mx-auto max-w-md px-4 py-24 text-center">
      <h1 className="font-display text-4xl text-ink-900">404</h1>
      <p className="mt-3 text-ink-400">We couldn't find that page.</p>
      <Link to="/" className="mt-6 inline-block rounded-md bg-ink-900 px-6 py-3 text-sm font-medium text-linen">Back to home</Link>
    </div>
  )
}
