import React, { useEffect, useState } from 'react'
import { adminApi } from '../../api/adminApi'
import { formatDateTime } from '../../utils/format'
import Spinner from '../../components/Spinner'

export default function AdminAudit() {
  const [result, setResult] = useState(null)
  const [page, setPage] = useState(0)

  useEffect(() => { adminApi.auditLogs(page, 50).then(setResult).catch(() => setResult({ content: [] })) }, [page])

  if (!result) return <Spinner label="Loading audit log" />

  return (
    <div>
      <h2 className="font-medium text-ink-900 mb-4">Audit log</h2>
      <div className="rounded-lg border border-ink-100 overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 text-ink-400 text-xs uppercase">
            <tr><th className="text-left px-4 py-2">Action</th><th className="text-left px-4 py-2">Actor</th><th className="text-left px-4 py-2">Resource</th><th className="text-left px-4 py-2">IP</th><th className="text-left px-4 py-2">When</th></tr>
          </thead>
          <tbody>
            {result.content.map((a) => (
              <tr key={a.id} className="border-t border-ink-100">
                <td className="px-4 py-2 font-medium text-ink-900">{a.action}</td>
                <td className="px-4 py-2 text-ink-400">{a.actorEmail || a.actorUserId || 'system'}</td>
                <td className="px-4 py-2 text-ink-400">{a.resourceType} {a.resourceId}</td>
                <td className="px-4 py-2 text-ink-400">{a.ipAddress}</td>
                <td className="px-4 py-2 text-ink-400">{formatDateTime(a.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {result.totalPages > 1 && (
        <div className="mt-4 flex justify-center gap-2">
          {Array.from({ length: result.totalPages }).map((_, i) => (
            <button key={i} onClick={() => setPage(i)} className={`h-8 w-8 rounded text-sm ${i === page ? 'bg-ink-900 text-linen' : 'border border-ink-100'}`}>{i + 1}</button>
          ))}
        </div>
      )}
    </div>
  )
}
