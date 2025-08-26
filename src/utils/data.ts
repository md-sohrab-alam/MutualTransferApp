export const getAllDistricts = (): string[] => [
  'Patna', 'Gaya', 'Bhagalpur', 'Muzaffarpur', 'Purnia', 'Darbhanga', 
  'Araria', 'Kishanganj', 'Katihar', 'Madhepura', 'Saharsa', 'Supaul',
  'Vaishali', 'Samastipur', 'Begusarai', 'Khagaria', 'Munger', 'Lakhisarai',
  'Sheikhpura', 'Nalanda', 'Buxar', 'Bhojpur', 'Rohtas', 'Kaimur',
  'Aurangabad', 'Gaya', 'Nawada', 'Jamui', 'Banka', 'Saran', 'Siwan',
  'Gopalganj', 'West Champaran', 'East Champaran', 'Sitamarhi', 'Sheohar',
  'Madhubani'
];

export const getBlocksForDistrict = (district: string): string[] => {
  const normalizedDistrict = district.trim().toLowerCase();
  
  const districtBlocks: Record<string, string[]> = {
    'patna': ['Patna Sadar', 'Fatuha', 'Danapur', 'Paliganj', 'Masaurhi', 'Phulwari Sharif'],
    'gaya': ['Gaya Sadar', 'Bodh Gaya', 'Sherghati', 'Tekari', 'Belaganj', 'Wazirganj'],
    'bhagalpur': ['Bhagalpur Sadar', 'Nathnagar', 'Kahalgaon', 'Bihpur', 'Goradih', 'Pirpainti'],
    'muzaffarpur': ['Muzaffarpur Sadar', 'Motihari', 'Sitamarhi', 'Darbhanga', 'Samastipur'],
    'purnia': ['Purnia Sadar', 'Banmankhi', 'Dhamdaha', 'Krityanand Nagar', 'Kasba'],
    'darbhanga': ['Darbhanga Sadar', 'Benipur', 'Biraul', 'Tardih', 'Alinagar'],
    'araria': ['Araria Sadar', 'Forbesganj', 'Araria', 'Raniganj', 'Palasi'],
    'kishanganj': ['Kishanganj Sadar', 'Bahadurganj', 'Thakurganj', 'Pothia'],
    'katihar': ['Katihar Sadar', 'Korha', 'Manihari', 'Barari', 'Amdabad'],
    'madhepura': ['Madhepura Sadar', 'Singheshwar', 'Chausa', 'Alamnagar'],
    'saharsa': ['Saharsa Sadar', 'Simri Bakhtiarpur', 'Mahishi', 'Salkhua'],
    'supaul': ['Supaul Sadar', 'Nirmali', 'Tribeniganj', 'Chhatapur'],
    'vaishali': ['Hajipur', 'Lalganj', 'Mahnar', 'Patepur', 'Raghopur'],
    'samastipur': ['Samastipur Sadar', 'Dalsinghsarai', 'Pusa', 'Tajpur', 'Ujiarpur'],
    'begusarai': ['Begusarai Sadar', 'Manjhaul', 'Bakhri', 'Teghra', 'Cheria Bariarpur'],
    'khagaria': ['Khagaria Sadar', 'Gogri', 'Alauli', 'Parbatta'],
    'munger': ['Munger Sadar', 'Haveli Kharagpur', 'Dharhara', 'Tarapur'],
    'lakhisarai': ['Lakhisarai Sadar', 'Barahiya', 'Pipariya', 'Surygarha'],
    'sheikhpura': ['Sheikhpura Sadar', 'Barbigha', 'Shekhopur Sarai', 'Ariari'],
    'nalanda': ['Bihar Sharif', 'Rajgir', 'Hilsa', 'Islampur', 'Asthawan'],
    'buxar': ['Buxar Sadar', 'Dumraon', 'Rajpur', 'Chausa'],
    'bhojpur': ['Arrah', 'Jagdishpur', 'Piro', 'Sandesh', 'Shahpur'],
    'rohtas': ['Sasaram', 'Dehri', 'Bikramganj', 'Dawath', 'Chenari'],
    'kaimur': ['Bhabua', 'Mohania', 'Adhaura', 'Bhagwanpur', 'Chand'],
    'aurangabad': ['Aurangabad Sadar', 'Daudnagar', 'Deo', 'Barun', 'Nabinagar'],
    'nawada': ['Nawada Sadar', 'Rajauli', 'Hisua', 'Pakribarawan', 'Gobindpur'],
    'jamui': ['Jamui Sadar', 'Sikandra', 'Jhajha', 'Chakai', 'Barhat'],
    'banka': ['Banka Sadar', 'Amarpur', 'Dhoraiya', 'Barahat', 'Katoria'],
    'saran': ['Chhapra', 'Marhaura', 'Sonepur', 'Dighwara', 'Parsa'],
    'siwan': ['Siwan Sadar', 'Ziradei', 'Darauli', 'Raghunathpur', 'Hasanpura'],
    'gopalganj': ['Gopalganj Sadar', 'Kuchaikote', 'Barauli', 'Kateya', 'Bhore'],
    'west champaran': ['Bettiah', 'Motihari', 'Narkatiaganj', 'Bagaha', 'Chanpatia'],
    'east champaran': ['Motihari', 'Raxaul', 'Sugauli', 'Chakia', 'Piprakothi'],
    'sitamarhi': ['Sitamarhi Sadar', 'Belsand', 'Bathnaha', 'Majorganj', 'Parihar'],
    'sheohar': ['Sheohar Sadar', 'Piprarhi', 'Tariyani', 'Dumri Katsari'],
    'madhubani': ['Madhubani Sadar', 'Jhanjharpur', 'Benipatti', 'Khajauli', 'Babubarhi']
  };

  const key = Object.keys(districtBlocks).find(key => 
    key === normalizedDistrict
  );

  return key ? districtBlocks[key] : [
    'Sadar', 'Town Area', 'Rural Area', 'Municipal Area', 'Cantonment Area',
    'Industrial Area', 'Educational Zone', 'Commercial Zone', 'Residential Zone'
  ];
};

export const getPostLevels = (): string[] => [
  'Primary',
  'Upper Primary', 
  'Secondary',
  'Senior Secondary'
];

export const getDesignations = (postLevel: string): string[] => {
  switch (postLevel) {
    case 'Primary':
      return ['Assistant Teacher', 'Head Teacher', 'Teacher'];
    case 'Upper Primary':
      return ['Assistant Teacher', 'Head Teacher', 'Teacher'];
    case 'Secondary':
      return ['TGT', 'PGT', 'Head Teacher', 'Teacher'];
    case 'Senior Secondary':
      return ['PGT', 'TGT', 'Head Teacher', 'Teacher'];
    default:
      return ['Teacher', 'Assistant Teacher', 'Head Teacher'];
  }
};

export const getSubjects = (postLevel: string): string[] => {
  switch (postLevel) {
    case 'Primary':
      return ['All Subjects'];
    case 'Upper Primary':
      return ['All Subjects', 'Mathematics', 'Science', 'Social Studies', 'English', 'Hindi'];
    case 'Secondary':
      return ['Mathematics', 'Science', 'English', 'Hindi', 'Social Studies', 'Sanskrit', 'Computer Science'];
    case 'Senior Secondary':
      return ['Mathematics', 'Physics', 'Chemistry', 'Biology', 'English', 'Hindi', 'Computer Science', 'Economics', 'History', 'Geography'];
    default:
      return ['All Subjects'];
  }
};

export const getQualifications = (): string[] => [
  'B.Ed',
  'M.Ed',
  'B.Tech',
  'M.Tech',
  'B.Sc',
  'M.Sc',
  'B.A',
  'M.A',
  'B.Com',
  'M.Com',
  'Ph.D',
  'D.El.Ed',
  'JBT',
  'Other'
]; 