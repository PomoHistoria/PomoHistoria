import { Navigate, Route, Routes as RouterRoutes } from "react-router-dom";
import Cadastro from "../components/features/Auth/Cadastro";
import Login from "../components/features/Auth/Login";
import Admin from "../components/Pages/Admin";
import Home from "../components/Pages/Home";
import Profile from "../components/Pages/Profile";
import PomodoroTimer from "../components/Pages/PomodoroTimer";
import Questionnaire from "../components/Pages/Questionnaire";
import RequireAuth, { SomenteVisitante } from "./RequireAuth";

/** Somente login e cadastro são públicos. */
export default function AppRoutes() {
  return (
    <RouterRoutes>
      <Route path="/login" element={<SomenteVisitante><Login /></SomenteVisitante>} />
      <Route path="/cadastro" element={<SomenteVisitante><Cadastro /></SomenteVisitante>} />

      <Route path="/" element={<RequireAuth><Home /></RequireAuth>} />
      <Route path="/temporizador" element={<RequireAuth><PomodoroTimer /></RequireAuth>} />
      <Route path="/profile" element={<RequireAuth><Profile /></RequireAuth>} />
      <Route path="/questionario" element={<RequireAuth><Questionnaire /></RequireAuth>} />
      <Route path="/admin" element={<RequireAuth papel="ADMIN"><Admin /></RequireAuth>} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </RouterRoutes>
  );
}
