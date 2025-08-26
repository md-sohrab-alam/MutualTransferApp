import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  Chip,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Autocomplete,
} from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import { db } from '../../firebase/config';
import { doc, getDoc, setDoc, deleteDoc } from 'firebase/firestore';
import { Teacher, TransferRequest } from '../../types';
import { getAllDistricts, getBlocksForDistrict, getPostLevels, getDesignations, getSubjects } from '../../utils/data';
import { MatchingService } from '../../utils/matchingService';

export const RequestPage: React.FC = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [currentRequest, setCurrentRequest] = useState<TransferRequest | null>(null);
  const [currentTeacher, setCurrentTeacher] = useState<Teacher | null>(null);
  const [showEditDialog, setShowEditDialog] = useState(false);

  const [formData, setFormData] = useState<Partial<TransferRequest>>({
    preferredDistricts: [],
    preferredBlocks: [],
    post: '',
    designation: '',
    subject: '',
    qualification: '',
    contactPreference: true,
    notes: '',
  });

  const [availableBlocks, setAvailableBlocks] = useState<string[]>([]);

  useEffect(() => {
    if (user) {
      loadData();
    }
  }, [user]);

  const loadData = async () => {
    try {
      setLoading(true);
      
      // Load teacher data
      const teacherDoc = await getDoc(doc(db, 'teachers', user!.uid));
      if (teacherDoc.exists()) {
        const teacherData = teacherDoc.data() as Teacher;
        setCurrentTeacher(teacherData);
      }

      // Load transfer request
      const requestDoc = await getDoc(doc(db, 'transfer_requests', user!.uid));
      if (requestDoc.exists()) {
        const requestData = requestDoc.data() as TransferRequest;
        setCurrentRequest(requestData);
        setFormData(requestData);
      }
    } catch (err) {
      setError('Failed to load data. Please try again.');
      console.error('Error loading data:', err);
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

  const handleDistrictChange = (districts: string[]) => {
    setFormData(prev => ({
      ...prev,
      preferredDistricts: districts,
      preferredBlocks: [], // Reset blocks when districts change
    }));
  };

  const handleBlockChange = (blocks: string[]) => {
    setFormData(prev => ({
      ...prev,
      preferredBlocks: blocks,
    }));
  };

  const validateForm = (): string[] => {
    const errors: string[] = [];
    
    if (!formData.preferredDistricts || formData.preferredDistricts.length === 0) {
      errors.push('At least one preferred district is required');
    }
    if (!formData.post) {
      errors.push('Post level is required');
    }
    if (!formData.designation) {
      errors.push('Designation is required');
    }
    if (['Secondary', 'Senior Secondary'].includes(formData.post || '') && !formData.subject) {
      errors.push('Subject is required for secondary levels');
    }
    
    return errors;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const errors = validateForm();
    if (errors.length > 0) {
      setError(errors.join(', '));
      return;
    }

    if (!currentTeacher) {
      setError('Please complete your profile first.');
      return;
    }

    try {
      setSaving(true);
      setError(null);
      
      const requestData: TransferRequest = {
        teacherId: user!.uid,
        teacherName: currentTeacher.name,
        currentDistrict: currentTeacher.district,
        currentSchool: currentTeacher.schoolName,
        preferredDistricts: formData.preferredDistricts!.map(d => d.trim()),
        preferredBlocks: formData.preferredBlocks!.map(b => b.trim()),
        post: currentTeacher.post, // Use teacher's post level
        designation: currentTeacher.designation,
        subject: currentTeacher.subject,
        qualification: currentTeacher.qualification,
        status: 'PENDING',
        submittedDate: new Date().toISOString().split('T')[0],
        contactPreference: formData.contactPreference!,
        notes: formData.notes?.trim() || '',
      };

      await setDoc(doc(db, 'transfer_requests', user!.uid), requestData);
      setCurrentRequest(requestData);
      setSuccess('Transfer request saved successfully!');
      setShowEditDialog(false);
      
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError('Failed to save request. Please try again.');
      console.error('Error saving request:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleCancelRequest = async () => {
    try {
      setSaving(true);
      await deleteDoc(doc(db, 'transfer_requests', user!.uid));
      setCurrentRequest(null);
      setSuccess('Transfer request cancelled successfully!');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError('Failed to cancel request. Please try again.');
      console.error('Error cancelling request:', err);
    } finally {
      setSaving(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'warning';
      case 'MATCHED': return 'success';
      case 'COMPLETED': return 'info';
      case 'CANCELLED': return 'error';
      default: return 'default';
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
        My Transfer Request
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

      {!currentTeacher ? (
        <Alert severity="info" sx={{ mb: 2 }}>
          Please complete your profile before creating a transfer request.
        </Alert>
      ) : currentRequest ? (
        <Card>
          <CardContent>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">
                Current Request
              </Typography>
              <Chip
                label={currentRequest.status}
                color={getStatusColor(currentRequest.status) as any}
              />
            </Box>

            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  Current Location
                </Typography>
                <Typography variant="body1">
                  {currentRequest.currentDistrict}, {currentRequest.currentSchool}
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  Post Level
                </Typography>
                <Typography variant="body1">
                  {currentRequest.post} - {currentRequest.designation}
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  Preferred Districts
                </Typography>
                <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
                  {currentRequest.preferredDistricts.map((district, index) => (
                    <Chip key={index} label={district} size="small" />
                  ))}
                </Box>
              </Grid>

              <Grid item xs={12} md={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  Preferred Blocks
                </Typography>
                <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
                  {currentRequest.preferredBlocks.map((block, index) => (
                    <Chip key={index} label={block} size="small" />
                  ))}
                </Box>
              </Grid>

              {currentRequest.notes && (
                <Grid item xs={12}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Notes
                  </Typography>
                  <Typography variant="body1">
                    {currentRequest.notes}
                  </Typography>
                </Grid>
              )}

              <Grid item xs={12}>
                <Box display="flex" gap={2} mt={2}>
                  <Button
                    variant="contained"
                    onClick={() => setShowEditDialog(true)}
                  >
                    Edit Request
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    onClick={handleCancelRequest}
                    disabled={saving}
                  >
                    Cancel Request
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Create Transfer Request
            </Typography>
            <Typography variant="body2" color="text.secondary" mb={3}>
              Create a transfer request to find compatible teachers for mutual transfer.
            </Typography>
            <Button
              variant="contained"
              onClick={() => setShowEditDialog(true)}
            >
              Create Request
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Edit/Create Dialog */}
      <Dialog
        open={showEditDialog}
        onClose={() => setShowEditDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          {currentRequest ? 'Edit Transfer Request' : 'Create Transfer Request'}
        </DialogTitle>
        <DialogContent>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Preferred Locations
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <Autocomplete
                  multiple
                  options={getAllDistricts()}
                  value={formData.preferredDistricts || []}
                  onChange={(_, newValue) => handleDistrictChange(newValue)}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Preferred Districts"
                      required
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <Autocomplete
                  multiple
                  options={availableBlocks}
                  value={formData.preferredBlocks || []}
                  onChange={(_, newValue) => handleBlockChange(newValue)}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Preferred Blocks"
                    />
                  )}
                  disabled={!formData.preferredDistricts || formData.preferredDistricts.length === 0}
                />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Additional Information
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={4}
                  label="Notes (Optional)"
                  value={formData.notes || ''}
                  onChange={(e) => handleInputChange('notes', e.target.value)}
                  placeholder="Any additional information about your transfer preferences..."
                />
              </Grid>

              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.contactPreference || false}
                      onChange={(e) => handleInputChange('contactPreference', e.target.checked)}
                    />
                  }
                  label="Allow other teachers to contact me"
                />
              </Grid>
            </Grid>
          </form>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowEditDialog(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={saving}
          >
            {saving ? <CircularProgress size={20} /> : (currentRequest ? 'Update' : 'Create')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}; 