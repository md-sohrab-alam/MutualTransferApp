# Play Console Setup Checklist

## Main Store Listing

### 1. App Title
- Use: `Mutual Teacher Transfer (Bihar) — Unofficial`
- Source: `play-listing/title.txt`

### 2. Short Description
- Use content from `play-listing/short-description.txt` (max 80 characters)
- Must state the app is unofficial / not government-affiliated

### 3. Full Description
- Use content from `play-listing/full-description.txt`
- Must include:
  - Clear DISCLAIMER that the app is not a government entity
  - Working official government source URLs (`.gov` / state portals)
- Paste the same EN text into en-US; translate for hi-IN if you maintain Hindi listing

### 4. Screenshots
- Upload screenshots showing:
  - First-run disclaimer dialog (with official source links)
  - About & Privacy screen (disclaimer + official sources)
  - Profile / Home screens
  - Make sure disclaimer text is visible

### 5. Privacy Policy URL
- Point to hosted `public/privacy-policy.html`
- Must be a non-editable public URL
- Can be hosted on GitHub Pages, your domain, or any static hosting

## Data Safety

### 1. Data Collection
- **Personal Information**: Name (optional), contact method, job/role info, school/district preferences
- **Purpose**: App functionality and Account management

### 2. Data Sharing
- **Third-party services**: Firebase Auth, Firestore, Cloud Messaging, Crashlytics/Analytics
- **Data sales**: No data sold

### 3. Security Practices
- Data encrypted in transit
- Users can request deletion
- Users can update data from within app

### 4. Permissions
- Only use permissions actually required:
  - Internet
  - Network State
  - Wake Lock
  - Vibrate
- Remove any sensitive permissions not required

## Review Notes (Recommended)

```
App is an independent, unofficial utility for teachers. Store listing and in-app About/Disclaimer include a clear non-government disclaimer and clickable links to official Bihar government sources (state.bihar.gov.in, edu-online.bihar.gov.in, education.bih.nic.in, scert.bihar.gov.in). No official government transfer data or services are provided; all listings are user-submitted. Test account not required.
```

## Submit for Review

1. Update store listing text (en-US) from `play-listing/`
2. Upload screenshots showing disclaimer + official sources
3. Verify privacy policy URL is accessible
4. Complete data safety form
5. Upload new AAB (bump versionCode if needed) and submit

## If Rejected Again for Misleading Claims

1. Do not claim government affiliation
2. Ensure every store locale description has disclaimer + official source URLs
3. Confirm in-app Disclaimer and About screens show the same links
4. Resubmit with the review note above
