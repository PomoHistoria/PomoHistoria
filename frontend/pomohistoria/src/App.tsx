import './App.css'
import Routes from './routes/Routes';
import { PomodoroTimersProvider } from './hooks/PomodoroTimersProvider'
import { SessaoProvider } from './hooks/SessaoProvider'

function App() {
  return (
    <SessaoProvider>
      <PomodoroTimersProvider>
        <Routes />
      </PomodoroTimersProvider>
    </SessaoProvider>
  )
}

export default App
