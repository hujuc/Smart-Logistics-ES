import { Button } from "@/components/ui/button"
import Link from "next/link"
import { MessageCircle, FileText } from "lucide-react"

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center">
      <div className="text-center space-y-8 max-w-2xl">
        <h1 className="text-6xl font-bold text-black mb-4">SLMS</h1>
        <p className="text-xl text-gray-600 mb-12">
          Customer Support Portal
        </p>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Link href="/interfaces/customer-service/tickets">
            <div className="bg-white border border-gray-200 rounded-lg p-8 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
              <MessageCircle className="h-12 w-12 mb-4 mx-auto text-black" />
              <h2 className="text-xl font-semibold text-black mb-2">
                Support Tickets
              </h2>
              <p className="text-gray-600">
                View and manage your support requests
              </p>
              <Button className="mt-4 w-full">Access</Button>
            </div>
          </Link>

          <Link href="/interfaces/customer-service/reports">
            <div className="bg-white border border-gray-200 rounded-lg p-8 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
              <FileText className="h-12 w-12 mb-4 mx-auto text-black" />
              <h2 className="text-xl font-semibold text-black mb-2">
                Reports
              </h2>
              <p className="text-gray-600">
                View your delivery history and reports
              </p>
              <Button className="mt-4 w-full">Access</Button>
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
}
