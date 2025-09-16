# QSDV: Post-Quantum Secure Document Vault

This project is a full-stack web application demonstrating a secure document management system that uses Post-Quantum Cryptography (PQC) to protect files from the threat of future quantum computers.

## Overview

Traditional cryptographic algorithms like RSA and ECC are vulnerable to attacks by quantum computers. QSDV (Quantum-Safe Document Vault) is a forward-looking solution that integrates PQC algorithms for key encapsulation and digital signatures, ensuring that documents stored today remain secure in a post-quantum world.

The application features a modern web interface for uploading, managing, and viewing documents, all backed by a robust Java server that handles the cryptographic operations.

## Features

- **Secure Document Upload:** Upload files through a user-friendly web interface.
- **Post-Quantum Encryption:** Documents are encrypted at rest using CRYSTALS-Kyber, a PQC Key Encapsulation Mechanism (KEM) standardized by NIST.
- **Key Management:** Secure generation, storage, and management of post-quantum cryptographic keys.
- **User-Friendly Dashboard:** A simple interface to view and manage encrypted documents.
- **Audit Logging:** Tracks key events and actions within the system for security and compliance.
- **Modern Tech Stack:** Built with a decoupled frontend and backend for scalability and maintainability.

## Technology Stack

### Backend (Server)

- **Java 17+**
- **Spring Boot:** For building the robust REST API.
- **Spring Security:** To handle authentication and authorization.
- **Maven:** For dependency management.
- **OQS (Open Quantum Safe):** Java wrapper for the `liboqs` C library to perform PQC operations.
- **JPA/Hibernate:** For database interaction.

### Frontend (Client)

- **React:** For building the user interface.
- **TypeScript:** For type-safe JavaScript development.
- **Vite:** As the frontend build tool and development server.
- **CSS:** For styling the application.

## Project Structure

The project is organized into two main directories:

- **/server**: Contains the Java/Spring Boot backend application.
  - `src/main/java`: Core application source code.
  - `pom.xml`: Maven project configuration.
- **/client**: Contains the React/TypeScript frontend application.
  - `src/components`: Reusable React components.
  - `package.json`: Node.js project configuration.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17 or later**
- **Apache Maven**
- **Node.js and npm**
- **Git**

## Getting Started

Follow these steps to get the application running locally.

### 1. Clone the Repository

```bash
git clone <repository-url>
cd qsdv
```

### 2. Run the Backend Server

1.  **Navigate to the server directory:**
    ```bash
    cd server/app
    ```

2.  **Build the project using Maven:**
    This will download dependencies and compile the source code.
    ```bash
    mvn clean install
    ```

3.  **Run the Spring Boot application:**
    ```bash
    mvn spring-boot:run
    ```

The server will start on `http://localhost:8080`.

### 3. Run the Frontend Client

1.  **In a new terminal, navigate to the client directory:**
    ```bash
    cd client
    ```

2.  **Install Node.js dependencies:**
    ```bash
    npm install
    ```

3.  **Start the Vite development server:**
    ```bash
    npm run dev
    ```

The client application will be available at `http://localhost:5173`.

## How to Use

1.  Open your web browser and navigate to `http://localhost:5173`.
2.  Use the **Document Upload** component to select and upload a file.
3.  The backend will generate a post-quantum key pair, encrypt the document, and store it securely.
4.  The **Document List** will display all the encrypted documents you have uploaded.
5.  The **Key Management** section shows the public keys used for encryption.

---

This README provides a comprehensive starting point. You can add more sections as the project evolves, such as API documentation, contribution guidelines, and license information.
