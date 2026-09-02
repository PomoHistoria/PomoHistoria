import './App.css'

function App() {
  return (
  <div className="container-login">
        <h1>LOGIN</h1>

        <input
          type="email"
          placeholder="Email"
        />

        <input
          type="password"
          placeholder="Senha"
        />
      
        <a>Esqueceu sua senha?</a>

        <button>ENTRAR</button>
    </div>
  )
}

export default App
