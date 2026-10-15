import './App.css'
import Routes from './routes/Routes';
import { PomodoroTimersProvider } from './hooks/PomodoroTimersProvider'

function App() {
  return (
    <PomodoroTimersProvider>
      <Routes />
    </PomodoroTimersProvider>
  )
}

export default App
