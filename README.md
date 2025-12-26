# FasTravel

FasTravel is a software project designed to manage and optimize travel-related operations. The system is architected as a multi-module application, featuring a backend service and a client application.

> **Note:** For documentation regarding the project's development and theoretical background, please refer to the [Relatorio.pdf](./Relatorio.pdf) included in this repository.

## Project Structure

- **`app/`**: Android client application.
- **`backend/`**: Server-side logic and API handling.

## Prerequisites

Ensure you have the following installed on your machine:

- **Android Studio**
- **Docker** and **Docker Compose**
- **Node.js** and **npm**

## Installation & Setup

Follow these steps in order to get the full system running.

### 1. Infrastructure (Docker)
First, start the required services (database, etc.) using Docker Compose from the root directory of the project.

```bash
docker compose up -d
```

### 2. Backend Setup
Navigate to the backend folder and execute the following commands to install dependencies, set up the database, and start the development server.

```bash
cd backend
npm install
npx prisma generate
npx prisma migrate dev
npx prisma db seed
npm run start:dev
```

### 3. Mobile App

The client application is built for Android and should be run using Android Studio.
1. Open Android Studio.
2. Select Open.
3. Navigate to the app/ folder inside this repository and select it.
5. Select your target device (Emulator or Physical Device).
6. Click the Run button (Green Play icon) to launch the application.

## Acknowledgments
This project was carried out as part of the Mobile Device Programming subject during my studies at Universidade da Beira Interior.

## Authors
- **Tomás Simões** - [GitHub](https://github.com/Tomas-Simoes)
- **Rafael** - [GitHub](https://github.com/Rafasta236)
- **Leonardo** - [GitHub](https://github.com/leorcf)
