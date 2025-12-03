import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getNotifications, retryNotification, NotificationDto } from '../api';

function NotificationList() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [channelFilter, setChannelFilter] = useState('');
  const queryClient = useQueryClient();

  const { data, isLoading, error } = useQuery({
    queryKey: ['notifications', page, statusFilter, channelFilter],
    queryFn: () =>
      getNotifications(page, 20, statusFilter || undefined, channelFilter || undefined).then(
        (res) => res.data.data
      ),
  });

  const retryMutation = useMutation({
    mutationFn: retryNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
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

  const notifications = data?.content || [];
  const totalPages = data?.totalPages || 0;

  return (
    <div>
      <header className="page-header">
        <h2>📋 Журнал уведомлений</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: 5 }}>
          Всего записей: {data?.totalElements || 0}
        </p>
      </header>

      {/* Filters */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div style={{ display: 'flex', gap: 20, flexWrap: 'wrap' }}>
          <div className="form-group" style={{ margin: 0, minWidth: 150 }}>
            <label>Статус</label>
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
            >
              <option value="">Все</option>
              <option value="PENDING">В обработке</option>
              <option value="SENDING">Отправляется</option>
              <option value="SENT">Отправлено</option>
              <option value="DELIVERED">Доставлено</option>
              <option value="FAILED">Ошибка</option>
              <option value="EXPIRED">Истек срок</option>
            </select>
          </div>

          <div className="form-group" style={{ margin: 0, minWidth: 150 }}>
            <label>Канал</label>
            <select
              value={channelFilter}
              onChange={(e) => {
                setChannelFilter(e.target.value);
                setPage(0);
              }}
            >
              <option value="">Все</option>
              <option value="EMAIL">Email</option>
              <option value="TELEGRAM">Telegram</option>
              <option value="SMS">SMS</option>
              <option value="WHATSAPP">WhatsApp</option>
            </select>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="card">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Дата/Время</th>
                <th>Канал</th>
                <th>Получатель</th>
                <th>Статус</th>
                <th>Попытки</th>
                <th>Ошибка</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {notifications.length > 0 ? (
                notifications.map((notif: NotificationDto) => (
                  <tr key={notif.notificationId}>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      {new Date(notif.createdAt).toLocaleString('ru-RU')}
                    </td>
                    <td>
                      <span className={`channel-badge ${notif.channelType.toLowerCase()}`}>
                        {notif.channelType}
                      </span>
                    </td>
                    <td>{notif.recipient}</td>
                    <td>
                      <span className={`status-badge ${notif.status.toLowerCase()}`}>
                        {notif.status}
                      </span>
                    </td>
                    <td>
                      {notif.retryCount}/{notif.maxRetries}
                    </td>
                    <td style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {notif.errorMessage || '-'}
                    </td>
                    <td>
                      {(notif.status === 'FAILED' || notif.status === 'EXPIRED') && (
                        <button
                          className="btn btn-secondary"
                          style={{ padding: '6px 12px', fontSize: '0.85rem' }}
                          onClick={() => retryMutation.mutate(notif.notificationId)}
                          disabled={retryMutation.isPending}
                        >
                          🔄 Повторить
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: 40 }}>
                    Нет уведомлений
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

export default NotificationList;
