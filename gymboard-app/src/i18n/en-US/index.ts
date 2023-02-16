export default {
  mainLayout: {
    language: 'Language',
    pages: 'Pages',
  },
  registerPage: {
    title: 'Create a Gymboard Account',
    name: 'Name',
    email: 'Email',
    password: 'Password',
    register: 'Register',
    error: 'An error occurred.',
  },
  loginPage: {
    title: 'Login to Gymboard',
    email: 'Email',
    password: 'Password',
    logIn: 'Log in',
    createAccount: 'Create an account',
    authFailed: 'Invalid credentials.'
  },
  indexPage: {
    searchHint: 'Search for a Gym',
  },
  gymPage: {
    home: 'Home',
    submit: 'Submit',
    leaderboard: 'Leaderboard',
    homePage: {
      overview: 'Overview of this gym:',
      recentLifts: 'Recent Lifts',
    },
    submitPage: {
      loginToSubmit: 'Login or register to submit your lift',
      exercise: 'Exercise',
      weight: 'Weight',
      reps: 'Repetitions',
      date: 'Date',
      upload: 'Video File to Upload',
      submit: 'Submit',
      submitUploading: 'Uploading video...',
      submitCreatingSubmission: 'Creating submission...',
      submitVideoProcessing: 'Processing...',
      submitComplete: 'Submission complete!',
      submitFailed: 'Submission processing failed. Please try again later.',
    },
  },
  userPage: {
    notFound: {
      title: 'User Not Found',
      description: 'We couldn\'t find the user you\'re looking for.'
    },
    accountPrivate: 'This account is private.',
    recentLifts: 'Recent Lifts',
  },
  userSearchPage: {
    searchHint: 'Search for a user'
  },
  userSettingsPage: {
    title: 'Account Settings',
    email: 'Email',
    name: 'Name',
    password: 'Password',
    passwordHint: 'Set a new password for your account.',
    updatePassword: 'Update Password',
    passwordUpdated: 'Password updated.',
    passwordInvalid: 'Invalid password.',
    personalDetails: {
      title: 'Personal Details',
      birthDate: 'Date of Birth',
      sex: 'Sex',
      sexMale: 'Male',
      sexFemale: 'Female',
      sexUnknown: 'Prefer not to say',
      currentWeight: 'Current Weight',
      currentWeightUnit: 'Current Weight Unit'
    },
    preferences: {
      title: 'Preferences',
      accountPrivate: 'Private',
      language: 'Language'
    },
    save: 'Save',
    undo: 'Undo'
  },
  accountMenuItem: {
    logIn: 'Login',
    profile: 'Profile',
    settings: 'Settings',
    logOut: 'Log out',
  },
  generalErrors: {
    apiError: 'An API error occurred. Please try again later.'
  },
  weightUnit: {
    kilograms: 'Kilograms',
    pounds: 'Pounds'
  }
};
