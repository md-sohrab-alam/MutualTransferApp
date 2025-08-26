import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Chip,
  Alert,
  CircularProgress,
  Autocomplete,
} from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import { db } from '../../firebase/config';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { Teacher } from '../../types';
import { getAllDistricts, getBlocksForDistrict, getPostLevels, getDesignations, getSubjects, getQualifications } from '../../utils/data';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [formData, setFormData] = useState<Partial<Teacher>>({
    name: '',
    gender: '',
    subject: '',
    post: '',
    district: '',
    block: '',
    schoolName: '',
    designation: '',
    qualification: '',
    contactPreference: true,
    willingToMove: false,
    preferredDistricts: [],
    preferredBlocks: [],
    contact: {
      email: '',
      phone: '',
    },
  });

  const [availableBlocks, setAvailableBlocks] = useState<string[]>([]);
  const [availableDesignations, setAvailableDesignations] = useState<string[]>([]);
  const [availableSubjects, setAvailableSubjects] = useState<string[]>([]);

  useEffect(() => {
    if (user) {
      loadProfile();
    }
  }, [user]);

  useEffect(() => {
    if (formData.district) {
      setAvailableBlocks(getBlocksForDistrict(formData.district));
    }
  }, [formData.district]);

  useEffect(() => {
    if (formData.post) {
      setAvailableDesignations(getDesignations(formData.post));
      setAvailableSubjects(getSubjects(formData.post));
    }
  }, [formData.post]);

  const loadProfile = async () => {
    try {
      setLoading(true);
      const teacherDoc = await getDoc(doc(db, 'teachers', user!.uid));
      
      if (teacherDoc.exists()) {
        const teacherData = teacherDoc.data() as Teacher;
        setFormData(teacherData);
      }
    } catch (err) {
      setError('Failed to load profile. Please try again.');
      console.error('Error loading profile:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleContactChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      contact: {
        ...prev.contact!,
        [field]: value,
      },
    }));
  };

  const validateForm = (): string[] => {
    const errors: string[] = [];
    
    if (!formData.name?.trim()) errors.push('Name is required');
    if (!formData.gender) errors.push('Gender is required');
    if (!formData.post) errors.push('Post level is required');
    if (!formData.district) errors.push('District is required');
    if (!formData.block) errors.push('Block is required');
    if (!formData.schoolName?.trim()) errors.push('School name is required');
    if (!formData.designation) errors.push('Designation is required');
    if (!formData.qualification) errors.push('Qualification is required');
    if (!formData.contact?.phone?.trim()) errors.push('Phone number is required');
    
    return errors;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const errors = validateForm();
    if (errors.length > 0) {
      setError(errors.join(', '));
      return;
    }

    try {
      setSaving(true);
      setError(null);
      
      const teacherData: Teacher = {
        uid: user!.uid,
        name: formData.name!.trim(),
        gender: formData.gender!,
        subject: formData.subject?.trim() || '',
        post: formData.post!,
        district: formData.district!,
        block: formData.block!,
        schoolName: formData.schoolName!.trim(),
        designation: formData.designation!,
        qualification: formData.qualification!,
        contactPreference: formData.contactPreference!,
        willingToMove: formData.willingToMove!,
        preferredDistricts: formData.preferredDistricts || [],
        preferredBlocks: formData.preferredBlocks || [],
        contact: {
          email: formData.contact?.email?.trim() || '',
          phone: formData.contact?.phone!.trim(),
        },
        timestamp: Date.now(),
      };

      await setDoc(doc(db, 'teachers', user!.uid), teacherData);
      setSuccess('Profile saved successfully!');
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError('Failed to save profile. Please try again.');
      console.error('Error saving profile:', err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Teacher Profile
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              {/* Basic Information */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Basic Information
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Full Name"
                  value={formData.name || ''}
                  onChange={(e) => handleInputChange('name', e.target.value)}
                  required
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth required>
                  <InputLabel>Gender</InputLabel>
                  <Select
                    value={formData.gender || ''}
                    onChange={(e) => handleInputChange('gender', e.target.value)}
                    label="Gender"
                  >
                    <MenuItem value="Male">Male</MenuItem>
                    <MenuItem value="Female">Female</MenuItem>
                    <MenuItem value="Other">Other</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Phone Number"
                  value={formData.contact?.phone || ''}
                  onChange={(e) => handleContactChange('phone', e.target.value)}
                  required
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Email (Optional)"
                  value={formData.contact?.email || ''}
                  onChange={(e) => handleContactChange('email', e.target.value)}
                  type="email"
                />
              </Grid>

              {/* Professional Information */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Professional Information
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth required>
                  <InputLabel>Post Level</InputLabel>
                  <Select
                    value={formData.post || ''}
                    onChange={(e) => handleInputChange('post', e.target.value)}
                    label="Post Level"
                  >
                    {getPostLevels().map((level) => (
                      <MenuItem key={level} value={level}>
                        {level}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth required>
                  <InputLabel>Designation</InputLabel>
                  <Select
                    value={formData.designation || ''}
                    onChange={(e) => handleInputChange('designation', e.target.value)}
                    label="Designation"
                    disabled={!formData.post}
                  >
                    {availableDesignations.map((designation) => (
                      <MenuItem key={designation} value={designation}>
                        {designation}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <InputLabel>Subject</InputLabel>
                  <Select
                    value={formData.subject || ''}
                    onChange={(e) => handleInputChange('subject', e.target.value)}
                    label="Subject"
                    disabled={!formData.post}
                  >
                    {availableSubjects.map((subject) => (
                      <MenuItem key={subject} value={subject}>
                        {subject}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth required>
                  <InputLabel>Qualification</InputLabel>
                  <Select
                    value={formData.qualification || ''}
                    onChange={(e) => handleInputChange('qualification', e.target.value)}
                    label="Qualification"
                  >
                    {getQualifications().map((qual) => (
                      <MenuItem key={qual} value={qual}>
                        {qual}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              {/* Location Information */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Current Location
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth required>
                  <InputLabel>District</InputLabel>
                  <Select
                    value={formData.district || ''}
                    onChange={(e) => handleInputChange('district', e.target.value)}
                    label="District"
                  >
                    {getAllDistricts().map((district) => (
                      <MenuItem key={district} value={district}>
                        {district}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth required>
                  <InputLabel>Block</InputLabel>
                  <Select
                    value={formData.block || ''}
                    onChange={(e) => handleInputChange('block', e.target.value)}
                    label="Block"
                    disabled={!formData.district}
                  >
                    {availableBlocks.map((block) => (
                      <MenuItem key={block} value={block}>
                        {block}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="School Name"
                  value={formData.schoolName || ''}
                  onChange={(e) => handleInputChange('schoolName', e.target.value)}
                  required
                />
              </Grid>

              {/* Preferences */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Preferences
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.contactPreference || false}
                      onChange={(e) => handleInputChange('contactPreference', e.target.checked)}
                    />
                  }
                  label="Allow Contact"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.willingToMove || false}
                      onChange={(e) => handleInputChange('willingToMove', e.target.checked)}
                    />
                  }
                  label="Willing to Move"
                />
              </Grid>

              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={saving}
                  sx={{ mt: 2 }}
                >
                  {saving ? <CircularProgress size={24} /> : 'Save Profile'}
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}; 