import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { sendNotification, SendNotificationRequest } from '../api';

function SendNotification() {
  const [formData, setFormData] = useState<SendNotificationRequest>({
    channel: 'EMAIL',
    recipient: '',
    subject: '',
    message: '',
    priority: 'NORMAL',
  });

  const [result, setResult] = useState<{
    success: boolean;
    message: string;
    notificationId?: string;
  } | null>(null);

  const mutation = useMutation({
    mutationFn: sendNotification,
    onSuccess: (response) => {
      setResult({
        success: true,
        message: 'Уведомление успешно отправлено!',
        notificationId: response.data.data.notificationId,
      });
    },
    onError: (error: Error) => {
      setResult({
        success: false,
        message: `Ошибка: ${error.message}`,
      });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setResult(null);
    mutation.mutate(formData);
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div>
      <header className="page-header">
        <h2>✉️ Отправить уведомление</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: 5 }}>
          Тестирование отправки уведомлений
        </p>
      </header>

      <div className="grid-2">
        <div className="card">
          <h3 className="card-title">Форма отправки</h3>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="channel">Канал доставки</label>
              <select
                id="channel"
                name="channel"
                value={formData.channel}
                onChange={handleChange}
              >
                <option value="EMAIL">📧 Email</option>
                <option value="TELEGRAM">💬 Telegram</option>
                <option value="SMS">📱 SMS</option>
                <option value="WHATSAPP">📲 WhatsApp</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="recipient">Получатель</label>
              <input
                type="text"
                id="recipient"
                name="recipient"
                value={formData.recipient}
                onChange={handleChange}
                placeholder={
                  formData.channel === 'EMAIL'
                    ? 'email@example.com'
                    : formData.channel === 'TELEGRAM'
                    ? 'Chat ID (например: 123456789)'
                    : '+79001234567'
                }
                required
              />
            </div>

            {formData.channel === 'EMAIL' && (
              <div className="form-group">
                <label htmlFor="subject">Тема письма</label>
                <input
                  type="text"
                  id="subject"
                  name="subject"
                  value={formData.subject}
                  onChange={handleChange}
                  placeholder="Тема уведомления"
                />
              </div>
            )}

            <div className="form-group">
              <label htmlFor="message">Текст сообщения</label>
              <textarea
                id="message"
                name="message"
                value={formData.message}
                onChange={handleChange}
                placeholder="Введите текст сообщения..."
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="priority">Приоритет</label>
              <select
                id="priority"
                name="priority"
                value={formData.priority}
                onChange={handleChange}
              >
                <option value="HIGH">🔴 Высокий</option>
                <option value="NORMAL">🟡 Обычный</option>
                <option value="LOW">🟢 Низкий</option>
              </select>
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={mutation.isPending}
              style={{ width: '100%' }}
            >
              {mutation.isPending ? 'Отправка...' : '📤 Отправить'}
            </button>
          </form>
        </div>

        <div className="card">
          <h3 className="card-title">Результат</h3>

          {result ? (
            <div className={`alert ${result.success ? 'alert-success' : 'alert-error'}`}>
              <p>{result.message}</p>
              {result.notificationId && (
                <p style={{ marginTop: 10, fontSize: '0.9rem' }}>
                  <strong>ID:</strong> {result.notificationId}
                </p>
              )}
            </div>
          ) : (
            <p style={{ color: 'var(--text-secondary)' }}>
              Заполните форму и нажмите "Отправить" для тестирования
            </p>
          )}

          <div style={{ marginTop: 20 }}>
            <h4 style={{ marginBottom: 10, color: 'var(--text-secondary)' }}>
              💡 Подсказки
            </h4>
            <ul style={{ paddingLeft: 20, color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              <li>Для Email укажите валидный email-адрес</li>
              <li>Настройте SMTP в environment variables для отправки email</li>
              <li>Telegram, SMS, WhatsApp пока только сохраняются в БД</li>
              <li>Проверьте статус в журнале уведомлений</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SendNotification;
