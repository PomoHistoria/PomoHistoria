import { Navigate, Route, Routes as RouterRoutes } from "react-router-dom";
import Cadastro from "../components/features/Auth/Cadastro";
import Login from "../components/features/Auth/Login";
import Home from "../components/Pages/Home";
import Profile from "../components/Pages/Profile";
import PomodoroTimer from "../components/Pages/PomodoroTimer";

export default function AppRoutes() {
  return (
    <RouterRoutes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/cadastro" element={<Cadastro />} />
      <Route path="/profile" element={<Profile />} />
      <Route path="/temporizador" element={<PomodoroTimer />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </RouterRoutes>
  );
}
