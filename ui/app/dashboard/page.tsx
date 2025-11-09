import { Header } from "./components/dashboardHeader";
import { DashboardPage } from "./components/dashboardPage";

export default function Dashboard() {
  return (
    <main className="p-6">
      <Header />
      <DashboardPage />
    </main>
  );
}
