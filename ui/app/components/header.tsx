import React from 'react'
import { ThermometerIcon } from 'lucide-react'
export function Header() {
  return (
    <header className="bg-white rounded-xl shadow-lg p-5 mb-6 border border-gray-200">
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <ThermometerIcon size={32} className="text-red-500 mr-3" />
          <h1 className="text-2xl font-bold">Meat Thermometer Dashboard</h1>
        </div>
        <div>
          <p className="text-sm text-gray-500">All temperatures shown in °F</p>
        </div>
      </div>
    </header>
  )
}
