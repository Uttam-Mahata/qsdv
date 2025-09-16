import { useState, useEffect } from 'react'
import { Key, Shield, RefreshCw, Info, CheckCircle, Clock } from 'lucide-react'
import axios from 'axios'

interface PQKey {
  publicKey: string
  keyVersion: string
  algorithm: string
  keySize: number
  generatedAt: string
}

interface PQStatus {
  status: string
  keyVersion: string
  encryptionReady: boolean
  timestamp: string
}

export function KeyManagement() {
  const [pqKey, setPqKey] = useState<PQKey | null>(null)
  const [pqStatus, setPqStatus] = useState<PQStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [testResult, setTestResult] = useState<any>(null)
  const [testing, setTesting] = useState(false)

  useEffect(() => {
    fetchKeyData()
  }, [])

  const fetchKeyData = async () => {
    try {
      setLoading(true)
      const [keyResponse, statusResponse] = await Promise.all([
        axios.get('http://localhost:8880/api/keys/pq/v1'),
        axios.get('http://localhost:8880/api/keys/pq/status')
      ])
      
      setPqKey(keyResponse.data)
      setPqStatus(statusResponse.data)
    } catch (error) {
      console.error('Failed to fetch key data:', error)
    } finally {
      setLoading(false)
    }
  }

  const refreshKeys = async () => {
    setRefreshing(true)
    await fetchKeyData()
    setRefreshing(false)
  }

  const testEncryption = async () => {
    setTesting(true)
    try {
      const response = await axios.post('http://localhost:8880/api/keys/pq/test', {
        data: 'Test encryption with current PQ key'
      })
      setTestResult(response.data)
    } catch (error) {
      console.error('Encryption test failed:', error)
      setTestResult({ success: false, error: 'Test failed' })
    } finally {
      setTesting(false)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  const truncateKey = (key: string, length: number = 100) => {
    return key.length > length ? `${key.substring(0, length)}...` : key
  }

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="bg-white shadow rounded-lg p-8 text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading key management...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Key Management</h1>
        <p className="mt-2 text-gray-600">
          Manage your post-quantum encryption keys and test cryptographic operations
        </p>
      </div>

      {/* Key Status Overview */}
      {pqStatus && (
        <div className="bg-white shadow rounded-lg p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-900">System Status</h2>
            <button
              onClick={refreshKeys}
              disabled={refreshing}
              className="flex items-center space-x-2 px-3 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white rounded-md transition-colors"
            >
              <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
              <span>Refresh</span>
            </button>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-green-50 p-4 rounded-lg">
              <div className="flex items-center space-x-2">
                <CheckCircle className="h-5 w-5 text-green-500" />
                <span className="font-medium text-green-900">Status</span>
              </div>
              <p className="mt-1 text-green-800">{pqStatus.status}</p>
            </div>
            
            <div className="bg-blue-50 p-4 rounded-lg">
              <div className="flex items-center space-x-2">
                <Key className="h-5 w-5 text-blue-500" />
                <span className="font-medium text-blue-900">Key Version</span>
              </div>
              <p className="mt-1 text-blue-800">{pqStatus.keyVersion}</p>
            </div>
            
            <div className="bg-purple-50 p-4 rounded-lg">
              <div className="flex items-center space-x-2">
                <Clock className="h-5 w-5 text-purple-500" />
                <span className="font-medium text-purple-900">Last Updated</span>
              </div>
              <p className="mt-1 text-purple-800">{formatDate(pqStatus.timestamp)}</p>
            </div>
          </div>
        </div>
      )}

      {/* Active Key Details */}
      {pqKey && (
        <div className="bg-white shadow rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Active Post-Quantum Key</h2>
          
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Algorithm</label>
                <p className="mt-1 text-gray-900">{pqKey.algorithm}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Key Size</label>
                <p className="mt-1 text-gray-900">{pqKey.keySize} bits</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Generated At</label>
                <p className="mt-1 text-gray-900">{formatDate(pqKey.generatedAt)}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Version</label>
                <p className="mt-1 text-gray-900">{pqKey.keyVersion}</p>
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700">Public Key</label>
              <div className="mt-1 p-3 bg-gray-50 rounded-md border">
                <code className="text-sm text-gray-700 break-all">
                  {truncateKey(pqKey.publicKey)}
                </code>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Encryption Test */}
      <div className="bg-white shadow rounded-lg p-6 mb-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-900">Encryption Test</h2>
          <button
            onClick={testEncryption}
            disabled={testing}
            className="flex items-center space-x-2 px-4 py-2 bg-green-600 hover:bg-green-700 disabled:bg-green-400 text-white rounded-md transition-colors"
          >
            <Shield className={`h-4 w-4 ${testing ? 'animate-pulse' : ''}`} />
            <span>{testing ? 'Testing...' : 'Test Encryption'}</span>
          </button>
        </div>
        
        <p className="text-gray-600 mb-4">
          Test the post-quantum encryption and decryption functionality with sample data.
        </p>

        {testResult && (
          <div className={`p-4 rounded-lg ${testResult.success ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'}`}>
            {testResult.success ? (
              <div>
                <div className="flex items-center space-x-2 mb-2">
                  <CheckCircle className="h-5 w-5 text-green-500" />
                  <span className="font-medium text-green-900">Encryption Test Successful</span>
                </div>
                <div className="text-sm text-green-800 space-y-1">
                  <p>Original: {testResult.original}</p>
                  <p>Decrypted: {testResult.decrypted}</p>
                  <p>Encryption Time: {testResult.encryptionTimeMs}ms</p>
                  <p>Decryption Time: {testResult.decryptionTimeMs}ms</p>
                  <p>Total Time: {testResult.totalTimeMs}ms</p>
                  <p>Encrypted Size: {testResult.encryptedSize} bytes</p>
                  <p>Size Overhead: {testResult.sizeOverhead} bytes</p>
                </div>
              </div>
            ) : (
              <div>
                <div className="flex items-center space-x-2 mb-2">
                  <Info className="h-5 w-5 text-red-500" />
                  <span className="font-medium text-red-900">Encryption Test Failed</span>
                </div>
                <p className="text-sm text-red-800">{testResult.error || 'Unknown error occurred'}</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Security Information */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start space-x-3">
          <Info className="h-5 w-5 text-blue-500 mt-0.5" />
          <div>
            <h3 className="text-sm font-medium text-blue-900">Post-Quantum Security</h3>
            <div className="mt-2 text-sm text-blue-800 space-y-1">
              <p>• Uses RSA-OAEP simulation of Kyber key encapsulation mechanism (KEM)</p>
              <p>• Hybrid encryption: PQ KEM + AES-GCM for optimal security and performance</p>
              <p>• Keys are quantum-resistant and protect against future quantum attacks</p>
              <p>• Regular key rotation ensures long-term security</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
