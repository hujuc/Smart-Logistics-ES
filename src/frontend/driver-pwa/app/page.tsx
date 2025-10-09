import Link from 'next/link'

export default function Home() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-white">
      <div className="text-center space-y-8">
        <h1 className="text-6xl font-bold text-black">Driver PWA</h1>
        <Link href="/interfaces/daily-schedule">
          <button className="mt-4 bg-black text-white px-6 py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors">
            Go to Daily Schedule
          </button>
        </Link>
      </div>
    </div>
  )
}