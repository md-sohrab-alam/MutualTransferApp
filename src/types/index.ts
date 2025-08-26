export interface Teacher {
  uid: string;
  name: string;
  gender: string;
  subject: string;
  post: string;
  district: string;
  block: string;
  schoolName: string;
  contact: Contact;
  timestamp?: number;
  designation: string;
  contactPreference: boolean;
  willingToMove: boolean;
  preferredDistricts: string[];
  preferredBlocks: string[];
  qualification: string;
}

export interface Contact {
  email: string;
  phone: string;
}

export interface TransferRequest {
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

export interface Notification {
  id: string;
  title: string;
  message: string;
  type: string;
  timestamp: number;
  isRead: boolean;
  data?: Record<string, any>;
}

export interface Match {
  teacher: Teacher;
  request: TransferRequest;
  compatibilityScore: number;
  compatibilityDetails: string[];
}

export interface District {
  name: string;
  blocks: string[];
}

export interface PostLevel {
  name: string;
  requiresSubject: boolean;
}

export interface Subject {
  name: string;
  postLevels: string[];
} 