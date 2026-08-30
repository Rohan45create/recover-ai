import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/layout/Layout';
import { Overview } from './pages/Overview';
import { Analytics } from './pages/Analytics';
import { RecoveryLog } from './pages/RecoveryLog';
import { Policies } from './pages/Policies';
import { Settings } from './pages/Settings';
import { Landing } from './pages/Landing';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        
        <Route path="/app" element={<Layout />}>
          <Route index element={<Overview />} />
          <Route path="analytics" element={<Analytics />} />
          <Route path="log" element={<RecoveryLog />} />
          <Route path="policies" element={<Policies />} />
          <Route path="settings" element={<Settings />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
