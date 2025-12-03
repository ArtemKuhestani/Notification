import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getAuditLogs, AuditLogDto } from '../api';

function AuditLogs() {
  const [page, setPage] = useState(0);

  const { data, isLoading, error } = useQuery({
    queryKey: ['audit-logs', page],
    queryFn: () => getAuditLogs(page, 50).then((res) => res.data.data),
  });

  if (isLoading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="alert alert-error">
        Ошибка загрузки данных: {(error as Error).message}
      </div>
    );
  }

  const logs = data?.content || [];
  const totalPages = data?.totalPages || 0;

  return (
    <div>
      <header className="page-header">
        <h2>📝 Журнал аудита</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: 5 }}>
          История всех действий в системе. Всего записей: {data?.totalElements || 0}
        </p>
      </header>

      <div className="card">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Дата/Время</th>
                <th>Действие</th>
                <th>Тип</th>
                <th>ID объекта</th>
                <th>IP адрес</th>
                <th>Детали</th>
              </tr>
            </thead>
            <tbody>
              {logs.length > 0 ? (
                logs.map((log: AuditLogDto) => (
                  <tr key={log.logId}>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      {new Date(log.createdAt).toLocaleString('ru-RU')}
                    </td>
                    <td>
                      <span
                        style={{
                          padding: '4px 8px',
                          borderRadius: 4,
                          background: getActionColor(log.actionType),
                          color: 'white',
                          fontSize: '0.75rem',
                          fontWeight: 600,
                        }}
                      >
                        {log.actionType}
                      </span>
                    </td>
                    <td>{log.entityType}</td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>
                      {log.entityId ? log.entityId.substring(0, 8) + '...' : '-'}
                    </td>
                    <td style={{ fontFamily: 'monospace' }}>{log.ipAddress}</td>
                    <td>
                      {log.newValue && (
                        <details>
                          <summary style={{ cursor: 'pointer', color: 'var(--primary-color)' }}>
                            Показать
                          </summary>
                          <pre
                            style={{
                              marginTop: 10,
                              padding: 10,
                              background: '#f5f5f5',
                              borderRadius: 4,
                              fontSize: '0.75rem',
                              overflow: 'auto',
                              maxWidth: 300,
                            }}
                          >
                            {JSON.stringify(log.newValue, null, 2)}
                          </pre>
                        </details>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: 40 }}>
                    Нет записей аудита
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="pagination">
            <button onClick={() => setPage(0)} disabled={page === 0}>
              ⏮️ Начало
            </button>
            <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
              ◀️ Назад
            </button>
            <span style={{ padding: '8px 16px' }}>
              Страница {page + 1} из {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
            >
              Вперед ▶️
            </button>
            <button onClick={() => setPage(totalPages - 1)} disabled={page >= totalPages - 1}>
              Конец ⏭️
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function getActionColor(action: string): string {
  switch (action) {
    case 'SEND_NOTIFICATION':
      return '#1976d2';
    case 'STATUS_CHANGE':
      return '#7b1fa2';
    case 'CREATE':
      return '#2e7d32';
    case 'UPDATE':
      return '#ed6c02';
    case 'DELETE':
      return '#d32f2f';
    case 'LOGIN':
      return '#0288d1';
    case 'LOGOUT':
      return '#616161';
    default:
      return '#757575';
  }
}

export default AuditLogs;
