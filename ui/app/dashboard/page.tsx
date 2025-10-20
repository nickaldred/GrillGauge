import { Header } from "../components/header";
import { DashboardComponent } from "./components/dashboardComponent";

export default function Dashboard() {
  return (
    <main className="p-6">
      <Header />
      <DashboardComponent />
    </main>
  );
}
