import { Header } from "./components/dashboardHeader";
import { DashboardComponent } from "./components/dashboardComponent";

export default function Dashboard() {
  return (
    <main className="p-6">
      <Header />
      <DashboardComponent />
    </main>
  );
}
