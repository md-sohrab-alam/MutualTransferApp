import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  Button,
  CircularProgress,
  Alert,
  Paper,
} from '@mui/material';
import {
  Person as PersonIcon,
  School as SchoolIcon,
  LocationOn as LocationIcon,
  Work as WorkIcon,
  Book as BookIcon,
  ContactPhone as ContactIcon,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { db } from '../../firebase/config';
import { collection, query, where, getDocs, doc, getDoc } from 'firebase/firestore';
import { Teacher, TransferRequest, Match } from '../../types';
import { MatchingService } from '../../utils/matchingService';

export const HomePage: React.FC = () => {
  const { user } = useAuth();
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentTeacher, setCurrentTeacher] = useState<Teacher | null>(null);
  const [currentRequest, setCurrentRequest] = useState<TransferRequest | null>(null);

  useEffect(() => {
    if (user) {
      loadUserData();
    }
  }, [user]);

  const loadUserData = async () => {
    try {
      setLoading(true);
      
      // Load current teacher data
      const teacherDoc = await getDoc(doc(db, 'teachers', user!.uid));
      if (teacherDoc.exists()) {
        const teacherData = teacherDoc.data() as Teacher;
        setCurrentTeacher(teacherData);
      }

      // Load current transfer request
      const requestDoc = await getDoc(doc(db, 'transfer_requests', user!.uid));
      if (requestDoc.exists()) {
        const requestData = requestDoc.data() as TransferRequest;
        setCurrentRequest(requestData);
        
        // Load matches
        await loadMatches(requestData);
      } else {
        setError('No transfer request found. Please create a transfer request first.');
      }
    } catch (err) {
      setError('Failed to load data. Please try again.');
      console.error('Error loading user data:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadMatches = async (currentRequest: TransferRequest) => {
    try {
      // Query for compatible transfer requests
      const requestsQuery = query(
        collection(db, 'transfer_requests'),
        where('preferredDistricts', 'array-contains', currentRequest.currentDistrict),
        where('post', '==', currentRequest.post)
      );

      const requestsSnapshot = await getDocs(requestsQuery);
      const potentialMatches: Match[] = [];

      for (const requestDoc of requestsSnapshot.docs) {
        const requestData = requestDoc.data() as TransferRequest;
        
        // Skip own request
        if (requestData.teacherId === user!.uid) continue;

        // Check if current teacher's district is in the other teacher's preferred districts
        if (currentRequest.preferredDistricts.includes(requestData.currentDistrict)) {
          // Load teacher data
          const teacherDoc = await getDoc(doc(db, 'teachers', requestData.teacherId));
          if (teacherDoc.exists()) {
            const teacherData = teacherDoc.data() as Teacher;
            
            // Check compatibility
            if (MatchingService.isCompatibleMatch(currentRequest, requestData, teacherData)) {
              const score = MatchingService.calculateRelevanceScore(teacherData, currentRequest);
              const compatibilityDetails = MatchingService.getMatchCompatibilityDetails(teacherData, currentRequest);
              
              potentialMatches.push({
                teacher: teacherData,
                request: requestData,
                compatibilityScore: score,
                compatibilityDetails,
              });
            }
          }
        }
      }

      // Sort by compatibility score
      potentialMatches.sort((a, b) => b.compatibilityScore - a.compatibilityScore);
      setMatches(potentialMatches);
    } catch (err) {
      console.error('Error loading matches:', err);
      setError('Failed to load matches. Please try again.');
    }
  };

  const handleContactMatch = (match: Match) => {
    // In a real app, this would open a contact dialog or navigate to contact page
    console.log('Contacting match:', match.teacher.name);
    alert(`Contacting ${match.teacher.name} at ${match.teacher.contact.phone}`);
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        {error}
      </Alert>
    );
  }

  if (!currentRequest) {
    return (
      <Alert severity="info" sx={{ mb: 2 }}>
        Please create a transfer request to see matches.
      </Alert>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Your Matches
      </Typography>
      
      {matches.length === 0 ? (
        <Paper sx={{ p: 3, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary">
            No matches found
          </Typography>
          <Typography variant="body2" color="text.secondary">
            We'll notify you when compatible teachers are found.
          </Typography>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {matches.map((match, index) => (
            <Grid item xs={12} md={6} lg={4} key={index}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" mb={2}>
                    <PersonIcon sx={{ mr: 1 }} />
                    <Typography variant="h6">
                      {match.teacher.name}
                    </Typography>
                    <Chip
                      label={MatchingService.getMatchQualityDescription(match.compatibilityScore)}
                      color="primary"
                      size="small"
                      sx={{ ml: 'auto' }}
                    />
                  </Box>

                  <Box mb={2}>
                    <Box display="flex" alignItems="center" mb={1}>
                      <SchoolIcon sx={{ mr: 1, fontSize: 'small' }} />
                      <Typography variant="body2">
                        {match.teacher.schoolName}
                      </Typography>
                    </Box>
                    
                    <Box display="flex" alignItems="center" mb={1}>
                      <LocationIcon sx={{ mr: 1, fontSize: 'small' }} />
                      <Typography variant="body2">
                        {match.teacher.district}, {match.teacher.block}
                      </Typography>
                    </Box>
                    
                    <Box display="flex" alignItems="center" mb={1}>
                      <WorkIcon sx={{ mr: 1, fontSize: 'small' }} />
                      <Typography variant="body2">
                        {match.teacher.designation} - {match.teacher.post}
                      </Typography>
                    </Box>
                    
                    {match.teacher.subject && (
                      <Box display="flex" alignItems="center" mb={1}>
                        <BookIcon sx={{ mr: 1, fontSize: 'small' }} />
                        <Typography variant="body2">
                          {match.teacher.subject}
                        </Typography>
                      </Box>
                    )}
                  </Box>

                  <Box mb={2}>
                    <Typography variant="subtitle2" gutterBottom>
                      Compatibility:
                    </Typography>
                    {match.compatibilityDetails.map((detail, idx) => (
                      <Typography key={idx} variant="body2" color="text.secondary">
                        {detail}
                      </Typography>
                    ))}
                  </Box>

                  <Box display="flex" gap={1}>
                    <Button
                      variant="contained"
                      size="small"
                      startIcon={<ContactIcon />}
                      onClick={() => handleContactMatch(match)}
                      fullWidth
                    >
                      Contact
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
}; 