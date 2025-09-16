import { useState } from 'react'
import { FileText, Download, Trash2, Shield, Calendar } from 'lucide-react'

interface Document {
  id: string
  name: string
  size: number
  uploadDate: string
  encryptedSize: number
  keyVersion: string
}

export function DocumentList() {
  const [documents] = useState<Document[]>([
    // Mock data for demonstration
    {
      id: '1',
      name: 'sample-document.pdf',
      size: 1024 * 1024 * 2.5, // 2.5MB
      uploadDate: '2025-09-16T10:30:00Z',
      encryptedSize: 1024 * 1024 * 2.6, // Slightly larger due to encryption
      keyVersion: 'v1_20250916_112410'
    }
  ])

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  const handleDownload = (doc: Document) => {
    // TODO: Implement decryption and download
    console.log('Download document:', doc.id)
  }

  const handleDelete = (doc: Document) => {
    // TODO: Implement document deletion
    console.log('Delete document:', doc.id)
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">My Documents</h1>
        <p className="mt-2 text-gray-600">
          Your encrypted documents protected by post-quantum cryptography
        </p>
      </div>

      {documents.length === 0 ? (
        <div className="bg-white shadow rounded-lg p-8 text-center">
          <FileText className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-4 text-lg font-medium text-gray-900">No documents yet</h3>
          <p className="mt-2 text-gray-500">
            Upload your first document to get started with quantum-safe storage
          </p>
          <div className="mt-6">
            <a
              href="/upload"
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium transition-colors"
            >
              Upload Document
            </a>
          </div>
        </div>
      ) : (
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-medium text-gray-900">
                Document Library ({documents.length} files)
              </h2>
              <div className="text-sm text-gray-500">
                Total encrypted size: {formatFileSize(documents.reduce((acc, doc) => acc + doc.encryptedSize, 0))}
              </div>
            </div>
          </div>

          <div className="divide-y divide-gray-200">
            {documents.map((doc) => (
              <div key={doc.id} className="p-6 hover:bg-gray-50">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4 flex-1">
                    <div className="flex-shrink-0">
                      <FileText className="h-10 w-10 text-blue-500" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="text-lg font-medium text-gray-900 truncate">
                        {doc.name}
                      </h3>
                      <div className="mt-1 flex items-center space-x-4 text-sm text-gray-500">
                        <span className="flex items-center">
                          <Calendar className="h-4 w-4 mr-1" />
                          {formatDate(doc.uploadDate)}
                        </span>
                        <span>Original: {formatFileSize(doc.size)}</span>
                        <span>Encrypted: {formatFileSize(doc.encryptedSize)}</span>
                      </div>
                      <div className="mt-1 flex items-center space-x-2">
                        <Shield className="h-4 w-4 text-green-500" />
                        <span className="text-sm text-green-700">
                          Encrypted with key {doc.keyVersion}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => handleDownload(doc)}
                      className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-md transition-colors"
                      title="Download and decrypt"
                    >
                      <Download className="h-5 w-5" />
                    </button>
                    <button
                      onClick={() => handleDelete(doc)}
                      className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-md transition-colors"
                      title="Delete document"
                    >
                      <Trash2 className="h-5 w-5" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Security Status */}
      <div className="mt-8 bg-green-50 border border-green-200 rounded-lg p-4">
        <div className="flex items-start space-x-3">
          <Shield className="h-5 w-5 text-green-500 mt-0.5" />
          <div>
            <h3 className="text-sm font-medium text-green-900">Quantum-Safe Protection Active</h3>
            <p className="mt-1 text-sm text-green-800">
              All documents are encrypted using post-quantum cryptography, ensuring protection 
              against both classical and quantum computer attacks.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
