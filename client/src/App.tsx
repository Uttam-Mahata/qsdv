import React from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { Layout } from './components/Layout'
import { Dashboard } from './components/Dashboard'
import { DocumentUpload } from './components/DocumentUpload'
import { DocumentList } from './components/DocumentList'
import { KeyManagement } from './components/KeyManagement'
import './App.css'

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/upload" element={<DocumentUpload />} />
          <Route path="/documents" element={<DocumentList />} />
          <Route path="/keys" element={<KeyManagement />} />
        </Routes>
      </Layout>
    </Router>
  )
}

export default App
