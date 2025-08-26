# Mutual Transfer App - Web Version

A web application for facilitating mutual teacher transfers, built with React, TypeScript, and Firebase.

## Features

### 🔐 Authentication
- Phone number-based authentication using Firebase Auth
- OTP verification for secure login
- Protected routes for authenticated users

### 👤 Profile Management
- Complete teacher profile creation and editing
- Professional information (post level, designation, subject, qualification)
- Current location details (district, block, school)
- Contact preferences and willingness to move

### 📋 Transfer Request System
- Create and manage transfer requests
- Specify preferred districts and blocks
- Add notes and contact preferences
- Real-time status tracking

### 🔍 Smart Matching Algorithm
- District compatibility matching (bi-directional)
- Post level exact matching
- Subject and designation compatibility
- Scoring system for match quality
- Compatibility checklist display

### 📱 Modern UI/UX
- Material-UI components for consistent design
- Responsive layout for desktop and mobile
- Intuitive navigation with sidebar
- Real-time loading states and error handling

## Technology Stack

- **Frontend**: React 18 with TypeScript
- **UI Framework**: Material-UI (MUI)
- **Routing**: React Router DOM
- **Backend**: Firebase
  - Authentication
  - Firestore Database
  - Cloud Messaging (for notifications)
- **State Management**: React Context API
- **Build Tool**: Create React App

## Project Structure

```
src/
├── components/
│   └── Layout/
│       └── AppLayout.tsx          # Main app layout with navigation
├── contexts/
│   └── AuthContext.tsx            # Authentication context
├── firebase/
│   └── config.ts                  # Firebase configuration
├── pages/
│   ├── Auth/
│   │   └── LoginPage.tsx          # Phone authentication
│   ├── Home/
│   │   └── HomePage.tsx           # Matches display
│   ├── Profile/
│   │   └── ProfilePage.tsx        # Profile management
│   └── Request/
│       └── RequestPage.tsx        # Transfer request management
├── types/
│   └── index.ts                   # TypeScript interfaces
├── utils/
│   ├── data.ts                    # Static data (districts, blocks, etc.)
│   └── matchingService.ts         # Core matching algorithm
└── App.tsx                        # Main app component
```

## Getting Started

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Firebase project setup

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd mutual-transfer-app
```

2. Install dependencies:
```bash
npm install
```

3. Configure Firebase:
   - Create a Firebase project
   - Enable Authentication (Phone provider)
   - Enable Firestore Database
   - Update `src/firebase/config.ts` with your Firebase config

4. Start the development server:
```bash
npm start
```

The app will be available at `http://localhost:3000`

### Firebase Setup

1. **Authentication**:
   - Enable Phone Number sign-in method
   - Add your domain to authorized domains

2. **Firestore Database**:
   - Create collections: `teachers`, `transfer_requests`
   - Set up security rules for data access

3. **Indexes** (for efficient queries):
   ```javascript
   // Composite index for transfer requests
   Collection: transfer_requests
   Fields: preferredDistricts (Array), post (String), teacherId (String)
   ```

## Core Features

### Matching Algorithm

The app uses a sophisticated matching algorithm that considers:

1. **Primary Criteria**:
   - District compatibility (both teachers want to move to each other's districts)
   - Post level exact match (Primary, Secondary, Senior Secondary)

2. **Secondary Criteria**:
   - Subject compatibility
   - Designation match
   - Qualification match
   - Block preferences
   - Contact preferences

3. **Scoring System**:
   - Perfect subject match: 100 points
   - Post level match: 50 points
   - Designation match: 30 points
   - Contact preference match: 20 points
   - Willing to move: 10 points
   - Block preference match: 15 points
   - Same district preference: 25 points

### Data Models

#### Teacher Profile
```typescript
interface Teacher {
  uid: string;
  name: string;
  gender: string;
  subject: string;
  post: string;
  district: string;
  block: string;
  schoolName: string;
  designation: string;
  qualification: string;
  contactPreference: boolean;
  willingToMove: boolean;
  preferredDistricts: string[];
  preferredBlocks: string[];
  contact: {
    email: string;
    phone: string;
  };
}
```

#### Transfer Request
```typescript
interface TransferRequest {
  teacherId: string;
  teacherName: string;
  currentDistrict: string;
  currentSchool: string;
  preferredDistricts: string[];
  preferredBlocks: string[];
  post: string;
  designation: string;
  subject: string;
  qualification: string;
  status: 'PENDING' | 'MATCHED' | 'COMPLETED' | 'CANCELLED';
  submittedDate: string;
  contactPreference: boolean;
  notes: string;
}
```

## Available Scripts

- `npm start` - Start development server
- `npm build` - Build for production
- `npm test` - Run tests
- `npm eject` - Eject from Create React App

## Deployment

### Build for Production
```bash
npm run build
```

### Deploy to Firebase Hosting
1. Install Firebase CLI: `npm install -g firebase-tools`
2. Login: `firebase login`
3. Initialize: `firebase init hosting`
4. Deploy: `firebase deploy`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For support and questions, please contact the development team or create an issue in the repository. 