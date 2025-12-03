# Notification Service - Сервис Уведомлений

Централизованный микросервис для отправки уведомлений через различные каналы: Email, Telegram, SMS, WhatsApp.

## 🚀 Быстрый старт

### Запуск через Docker Compose (рекомендуется)

```bash
# Клонировать и перейти в директорию проекта
cd notification

# Запустить все сервисы одной командой
docker-compose up --build

# Или запустить в фоновом режиме
docker-compose up --build -d
```

После запуска:
- **Админ-панель:** http://localhost:3000
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API:** http://localhost:8080/api/v1

### Остановка

```bash
docker-compose down

# Для полной очистки (включая данные БД)
docker-compose down -v
```

## 📧 Настройка Email

Для тестирования отправки email, создайте файл `.env` в корне проекта:

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

> **Примечание:** Для Gmail используйте [App Password](https://support.google.com/accounts/answer/185833)

## 🏗️ Архитектура

```
notification/
├── backend/                 # Spring Boot приложение
│   ├── src/main/java/kg/notifications/
│   │   ├── controller/      # REST контроллеры
│   │   ├── service/         # Бизнес-логика
│   │   ├── repository/      # Работа с БД
│   │   ├── entity/          # JPA сущности
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── config/          # Конфигурация
│   │   └── exception/       # Обработка ошибок
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # React приложение
│   ├── src/
│   │   ├── pages/           # Страницы
│   │   ├── api.ts           # API клиент
│   │   └── App.tsx          # Главный компонент
│   ├── Dockerfile
│   └── package.json
├── database/
│   └── init.sql             # Инициализация БД
└── docker-compose.yml       # Оркестрация контейнеров
```

## 📚 API Endpoints

### Отправка уведомлений

```bash
# Отправить email
curl -X POST http://localhost:8080/api/v1/send \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "EMAIL",
    "recipient": "user@example.com",
    "subject": "Тестовое уведомление",
    "message": "Привет! Это тестовое сообщение.",
    "priority": "NORMAL"
  }'
```

### Проверка статуса

```bash
curl http://localhost:8080/api/v1/status/{notification_id}
```

### Статистика

```bash
curl http://localhost:8080/api/v1/admin/stats/dashboard
```

## 🗃️ База данных

### Основные таблицы

| Таблица | Описание |
|---------|----------|
| `notifications` | Журнал всех уведомлений |
| `api_clients` | Внешние системы-клиенты |
| `channel_configs` | Настройки каналов доставки |
| `admins` | Администраторы системы |
| `message_templates` | Шаблоны сообщений |
| `audit_log` | Журнал аудита действий |
| `retry_queue` | Очередь повторных отправок |

## 🔧 Локальная разработка

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## 🛡️ Безопасность

- JWT аутентификация для админ-панели
- API Key аутентификация для внешних систем
- CORS настроен для разрешенных источников
- Пароли хранятся в bcrypt хешах
- API ключи хранятся в SHA-256 хешах

## 📈 Мониторинг

- Health Check: `GET /api/v1/health`
- Actuator: `GET /actuator/health`
- Метрики: `GET /actuator/metrics`

## 🔄 Retry Logic

Система автоматически повторяет отправку при временных ошибках:
- 1 минута → 5 минут → 15 минут → 1 час → 4 часа
- Максимум 5 попыток
- TTL сообщения: 24 часа

## 📝 Лицензия

MIT License
