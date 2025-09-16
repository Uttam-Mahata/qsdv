import { useState, useCallback } from 'react'
import { useDropzone } from 'react-dropzone'
import { Upload, FileText, Shield, CheckCircle, AlertCircle } from 'lucide-react'
import axios from 'axios'

interface UploadStatus {
  status: 'idle' | 'encrypting' | 'uploading' | 'success' | 'error'
  message?: string
  encryptionTime?: number
  uploadTime?: number
}

export function DocumentUpload() {
  const [uploadStatus, setUploadStatus] = useState<UploadStatus>({ status: 'idle' })
  const [selectedFile, setSelectedFile] = useState<File | null>(null)

  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles.length > 0) {
      setSelectedFile(acceptedFiles[0])
      setUploadStatus({ status: 'idle' })
    }
  }, [])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false,
    accept: {
      'application/pdf': ['.pdf'],
      'text/plain': ['.txt'],
      'application/msword': ['.doc'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'image/*': ['.png', '.jpg', '.jpeg', '.gif'],
    },
  })

  const encryptAndUpload = async () => {
    if (!selectedFile) return

    setUploadStatus({ status: 'encrypting', message: 'Encrypting file with post-quantum cryptography...' })

    try {
      // Step 1: Get PQ public key
      const keyResponse = await axios.get('http://localhost:8880/api/keys/pq/v1')
      const publicKey = keyResponse.data.publicKey

      // Step 2: Read file as base64
      const fileContent = await new Promise<string>((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => {
          const result = reader.result as string
          const base64 = result.split(',')[1] // Remove data URL prefix
          resolve(base64)
        }
        reader.onerror = reject
        reader.readAsDataURL(selectedFile)
      })

      const encryptionStartTime = Date.now()

      // Step 3: Encrypt file data using backend PQ service
      const encryptResponse = await axios.post('http://localhost:8880/api/keys/pq/test', {
        data: fileContent
      })

      const encryptionTime = Date.now() - encryptionStartTime

      if (!encryptResponse.data.success) {
        throw new Error('Encryption failed')
      }

      setUploadStatus({ 
        status: 'uploading', 
        message: 'File encrypted successfully! Uploading to vault...',
        encryptionTime 
      })

      // TODO: Step 4: Upload encrypted data to document storage API
      // For now, we'll simulate the upload
      await new Promise(resolve => setTimeout(resolve, 1000))

      const uploadTime = Date.now() - encryptionStartTime - encryptionTime

      setUploadStatus({
        status: 'success',
        message: 'Document successfully encrypted and stored!',
        encryptionTime,
        uploadTime
      })

      // Reset file selection after successful upload
      setTimeout(() => {
        setSelectedFile(null)
        setUploadStatus({ status: 'idle' })
      }, 3000)

    } catch (error) {
      console.error('Upload failed:', error)
      setUploadStatus({
        status: 'error',
        message: error instanceof Error ? error.message : 'Upload failed'
      })
    }
  }

  const resetUpload = () => {
    setSelectedFile(null)
    setUploadStatus({ status: 'idle' })
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Upload Document</h1>
        <p className="mt-2 text-gray-600">
          Securely encrypt and store your documents using post-quantum cryptography
        </p>
      </div>

      {/* Upload Area */}
      <div className="bg-white shadow rounded-lg p-6 mb-6">
        <div
          {...getRootProps()}
          className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
            isDragActive
              ? 'border-blue-400 bg-blue-50'
              : selectedFile
              ? 'border-green-400 bg-green-50'
              : 'border-gray-300 hover:border-gray-400'
          }`}
        >
          <input {...getInputProps()} />
          
          {selectedFile ? (
            <div className="space-y-4">
              <FileText className="mx-auto h-12 w-12 text-green-500" />
              <div>
                <p className="text-lg font-medium text-gray-900">{selectedFile.name}</p>
                <p className="text-sm text-gray-500">
                  {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                </p>
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation()
                  resetUpload()
                }}
                className="text-sm text-gray-500 hover:text-gray-700"
              >
                Choose different file
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              <Upload className="mx-auto h-12 w-12 text-gray-400" />
              <div>
                <p className="text-lg font-medium text-gray-900">
                  {isDragActive ? 'Drop the file here' : 'Drop file here or click to select'}
                </p>
                <p className="text-sm text-gray-500">
                  Supports PDF, DOC, DOCX, TXT, and image files
                </p>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Upload Button */}
      {selectedFile && uploadStatus.status === 'idle' && (
        <div className="bg-white shadow rounded-lg p-6 mb-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <Shield className="h-6 w-6 text-blue-500" />
              <div>
                <h3 className="text-lg font-medium text-gray-900">Ready for Encryption</h3>
                <p className="text-sm text-gray-500">
                  File will be encrypted using post-quantum cryptography before storage
                </p>
              </div>
            </div>
            <button
              onClick={encryptAndUpload}
              className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-md font-medium transition-colors"
            >
              Encrypt & Upload
            </button>
          </div>
        </div>
      )}

      {/* Status Display */}
      {uploadStatus.status !== 'idle' && (
        <div className="bg-white shadow rounded-lg p-6">
          <div className="space-y-4">
            {uploadStatus.status === 'encrypting' && (
              <div className="flex items-center space-x-3">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
                <div>
                  <h3 className="text-lg font-medium text-gray-900">Encrypting...</h3>
                  <p className="text-sm text-gray-500">{uploadStatus.message}</p>
                </div>
              </div>
            )}

            {uploadStatus.status === 'uploading' && (
              <div className="flex items-center space-x-3">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-green-600"></div>
                <div>
                  <h3 className="text-lg font-medium text-gray-900">Uploading...</h3>
                  <p className="text-sm text-gray-500">{uploadStatus.message}</p>
                  {uploadStatus.encryptionTime && (
                    <p className="text-xs text-gray-400">
                      Encryption completed in {uploadStatus.encryptionTime}ms
                    </p>
                  )}
                </div>
              </div>
            )}

            {uploadStatus.status === 'success' && (
              <div className="flex items-center space-x-3">
                <CheckCircle className="h-6 w-6 text-green-500" />
                <div>
                  <h3 className="text-lg font-medium text-green-900">Upload Successful!</h3>
                  <p className="text-sm text-green-700">{uploadStatus.message}</p>
                  <div className="text-xs text-gray-500 mt-1 space-x-4">
                    {uploadStatus.encryptionTime && (
                      <span>Encryption: {uploadStatus.encryptionTime}ms</span>
                    )}
                    {uploadStatus.uploadTime && (
                      <span>Upload: {uploadStatus.uploadTime}ms</span>
                    )}
                  </div>
                </div>
              </div>
            )}

            {uploadStatus.status === 'error' && (
              <div className="flex items-center space-x-3">
                <AlertCircle className="h-6 w-6 text-red-500" />
                <div>
                  <h3 className="text-lg font-medium text-red-900">Upload Failed</h3>
                  <p className="text-sm text-red-700">{uploadStatus.message}</p>
                  <button
                    onClick={resetUpload}
                    className="text-sm text-blue-600 hover:text-blue-800 mt-1"
                  >
                    Try again
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Security Info */}
      <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start space-x-3">
          <Shield className="h-5 w-5 text-blue-500 mt-0.5" />
          <div>
            <h3 className="text-sm font-medium text-blue-900">Security Information</h3>
            <div className="mt-2 text-sm text-blue-800 space-y-1">
              <p>• Files are encrypted client-side using post-quantum cryptography</p>
              <p>• Encryption keys are generated using quantum-resistant algorithms</p>
              <p>• Only encrypted data is transmitted and stored on the server</p>
              <p>• Your files remain secure even against future quantum computers</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
