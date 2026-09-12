import React from 'react'

export default function ProductCardSkeleton() {
  return (
    <div>
      <div className="skeleton aspect-square rounded-md" />
      <div className="mt-3 space-y-2">
        <div className="skeleton h-3 w-1/3" />
        <div className="skeleton h-4 w-4/5" />
        <div className="skeleton h-4 w-1/4" />
      </div>
    </div>
  )
}
