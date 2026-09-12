import React from 'react'

export default function EmptyState({ title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-ink-100 py-16 px-6 text-center">
      <h3 className="font-display text-xl text-ink-900">{title}</h3>
      {description && <p className="mt-2 max-w-sm text-sm text-ink-400">{description}</p>}
      {action && <div className="mt-6">{action}</div>}
    </div>
  )
}
