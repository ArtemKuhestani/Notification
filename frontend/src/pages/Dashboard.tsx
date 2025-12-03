import { useQuery } from '@tanstack/react-query';
import { getDashboardStats } from '../api';

function Dashboard() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => getDashboardStats().then(res => res.data.data),
    refetchInterval: 30000, // Refresh every 30 seconds
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

  const stats = data!;

  return (
    <div>
      <header className="page-header">
        <h2>📊 Дашборд</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: 5 }}>
          Статистика за последние 24 часа
        </p>
      </header>

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card success">
          <div className="stat-value">{stats.totalSent || 0}</div>
          <div className="stat-label">Успешно отправлено</div>
        </div>
        <div className="stat-card error">
          <div className="stat-value">{stats.totalFailed || 0}</div>
          <div className="stat-label">Ошибки</div>
        </div>
        <div className="stat-card warning">
          <div className="stat-value">{stats.totalPending || 0}</div>
          <div className="stat-label">В обработке</div>
        </div>
        <div className="stat-card info">
          <div className="stat-value">{stats.successRate?.toFixed(1) || 0}%</div>
          <div className="stat-label">Успешность</div>
        </div>
      </div>

      <div className="grid-2">
        {/* By Channel */}
        <div className="card">
          <h3 className="card-title">📨 По каналам</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {Object.entries(stats.byChannel || {}).map(([channel, count]) => (
              <div key={channel} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span className={`channel-badge ${channel.toLowerCase()}`}>{channel}</span>
                <span style={{ fontWeight: 600 }}>{count}</span>
              </div>
            ))}
            {Object.keys(stats.byChannel || {}).length === 0 && (
              <p style={{ color: 'var(--text-secondary)' }}>Нет данных</p>
            )}
          </div>
        </div>

        {/* By Status */}
        <div className="card">
          <h3 className="card-title">📊 По статусам</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {Object.entries(stats.byStatus || {}).map(([status, count]) => (
              <div key={status} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span className={`status-badge ${status.toLowerCase()}`}>{status}</span>
                <span style={{ fontWeight: 600 }}>{count}</span>
              </div>
            ))}
            {Object.keys(stats.byStatus || {}).length === 0 && (
              <p style={{ color: 'var(--text-secondary)' }}>Нет данных</p>
            )}
          </div>
        </div>
      </div>

      {/* Recent Errors */}
      <div className="card">
        <h3 className="card-title">❌ Последние ошибки</h3>
        {stats.recentErrors && stats.recentErrors.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Время</th>
                  <th>Канал</th>
                  <th>Получатель</th>
                  <th>Ошибка</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentErrors.map((notif) => (
                  <tr key={notif.notificationId}>
                    <td>{new Date(notif.createdAt).toLocaleString('ru-RU')}</td>
                    <td>
                      <span className={`channel-badge ${notif.channelType.toLowerCase()}`}>
                        {notif.channelType}
                      </span>
                    </td>
                    <td>{notif.recipient}</td>
                    <td style={{ color: 'var(--error-color)' }}>{notif.errorMessage || 'Unknown error'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p style={{ color: 'var(--text-secondary)' }}>Нет ошибок за последние 24 часа 🎉</p>
        )}
      </div>

      <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: 20 }}>
        Последнее обновление: {stats.generatedAt ? new Date(stats.generatedAt).toLocaleString('ru-RU') : '-'}
      </p>
    </div>
  );
}

export default Dashboard;
