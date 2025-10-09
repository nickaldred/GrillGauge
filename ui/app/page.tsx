import { Header } from './components/header'

export default function Home() {
  return (
    <main className="p-6">
      <Header />
      <p className="text-xl font-semibold text-gray-700 mt-4">
        Welcome to the Meat Thermometer Dashboard!
      </p>
    </main>
  )
}