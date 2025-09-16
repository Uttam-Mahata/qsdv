import { useEffect, useState } from 'react'
import { Shield, Upload, FileText, Key, Activity } from 'lucide-react'
import { Link } from 'react-router-dom'
import axios from 'axios'

interface PQStatus {
  status: string
  keyVersion: string
  encryptionReady: boolean
  timestamp: string
}

export function Dashboard() {
  const [pqStatus, setPqStatus] = useState<PQStatus | null>(null)

  useEffect(() => {
    fetchPQStatus()
  }, [])

  const fetchPQStatus = async () => {
    try {
      const response = await axios.get('http://localhost:8880/api/keys/pq/status')
      setPqStatus(response.data)
    } catch (error) {
      console.error('Failed to fetch PQ status:', error)
    }
  }

  const stats = [
    { name: 'Total Documents', value: '0', icon: FileText },
    { name: 'Encrypted Size', value: '0 MB', icon: Shield },
    { name: 'Active Keys', value: '1', icon: Key },
    { name: 'PQ Status', value: pqStatus?.status || 'Loading...', icon: Activity },
  ]

  const quickActions = [
    {
      name: 'Upload New Document',
      description: 'Securely encrypt and store a new document',
      href: '/upload',
      icon: Upload,
      color: 'bg-blue-500 hover:bg-blue-600',
    },
    {
      name: 'View Documents',
      description: 'Browse your encrypted document library',
      href: '/documents',
      icon: FileText,
      color: 'bg-green-500 hover:bg-green-600',
    },
    {
      name: 'Manage Keys',
      description: 'View and manage post-quantum encryption keys',
      href: '/keys',
      icon: Key,
      color: 'bg-purple-500 hover:bg-purple-600',
    },
  ]

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Quantum-Safe Document Vault
        </h1>
        <p className="mt-2 text-gray-600">
          Secure document storage using post-quantum cryptography
        </p>
      </div>

      {/* Status Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4 mb-8">
        {stats.map((item) => {
          const Icon = item.icon
          return (
            <div
              key={item.name}
              className="bg-white overflow-hidden shadow rounded-lg"
            >
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <Icon className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">
                        {item.name}
                      </dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {item.value}
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          )
        })}
      </div>

      {/* PQ Status */}
      {pqStatus && (
        <div className="bg-green-50 border border-green-200 rounded-md p-4 mb-8">
          <div className="flex">
            <div className="flex-shrink-0">
              <Shield className="h-5 w-5 text-green-400" />
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-green-800">
                Post-Quantum Encryption Active
              </h3>
              <div className="mt-2 text-sm text-green-700">
                <p>
                  Key Version: {pqStatus.keyVersion} | 
                  Encryption Ready: {pqStatus.encryptionReady ? 'Yes' : 'No'} |
                  Last Updated: {new Date(pqStatus.timestamp).toLocaleString()}
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {quickActions.map((action) => {
          const Icon = action.icon
          return (
            <Link
              key={action.name}
              to={action.href}
              className="group relative bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-blue-500 rounded-lg shadow hover:shadow-md transition-shadow"
            >
              <div>
                <span
                  className={`rounded-lg inline-flex p-3 text-white ${action.color}`}
                >
                  <Icon className="h-6 w-6" aria-hidden="true" />
                </span>
              </div>
              <div className="mt-4">
                <h3 className="text-lg font-medium text-gray-900 group-hover:text-blue-600">
                  {action.name}
                </h3>
                <p className="mt-2 text-sm text-gray-500">
                  {action.description}
                </p>
              </div>
            </Link>
          )
        })}
      </div>

      {/* Info Section */}
      <div className="mt-8 bg-blue-50 border border-blue-200 rounded-md p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <Shield className="h-5 w-5 text-blue-400" />
          </div>
          <div className="ml-3">
            <h3 className="text-sm font-medium text-blue-800">
              Post-Quantum Security
            </h3>
            <div className="mt-2 text-sm text-blue-700">
              <p>
                This vault uses advanced post-quantum cryptography to protect your documents
                against both classical and quantum computer attacks. All files are encrypted
                using a hybrid approach with quantum-resistant key encapsulation.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
