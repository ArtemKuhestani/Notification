import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import SendNotification from './pages/SendNotification';
import NotificationList from './pages/NotificationList';
import AuditLogs from './pages/AuditLogs';

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <aside className="sidebar">
          <div className="sidebar-header">
            <h1>🔔 Notification Service</h1>
            <span>Панель администратора</span>
          </div>
          <nav>
            <ul className="nav-menu">
              <li>
                <NavLink to="/" className={({ isActive }) => isActive ? 'active' : ''}>
                  📊 Дашборд
                </NavLink>
              </li>
              <li>
                <NavLink to="/send" className={({ isActive }) => isActive ? 'active' : ''}>
                  ✉️ Отправить уведомление
                </NavLink>
              </li>
              <li>
                <NavLink to="/notifications" className={({ isActive }) => isActive ? 'active' : ''}>
                  📋 Журнал уведомлений
                </NavLink>
              </li>
              <li>
                <NavLink to="/audit" className={({ isActive }) => isActive ? 'active' : ''}>
                  📝 Журнал аудита
                </NavLink>
              </li>
              <li>
                <a href="/swagger-ui.html" target="_blank" rel="noopener noreferrer">
                  📚 API Документация
                </a>
              </li>
            </ul>
          </nav>
        </aside>
        
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/send" element={<SendNotification />} />
            <Route path="/notifications" element={<NotificationList />} />
            <Route path="/audit" element={<AuditLogs />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
