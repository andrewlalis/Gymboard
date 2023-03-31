export default {
  mainLayout: {
    language: 'Language',
    pages: 'Pages',
    menu: {
      gyms: 'Gyms',
      leaderboard: 'Global Leaderboard',
      users: 'Users',
      adminPanel: 'Admin Panel',
      about: 'About Gymboard'
    }
  },
  registerPage: {
    title: 'Create a Gymboard Account',
    name: 'Name',
    email: 'Email',
    password: 'Password',
    register: 'Register',
    error: 'An error occurred.',
  },
  registrationSuccessPage: {
    title: 'Account Registration Complete!',
    p1: 'Check your email for the link to activate your account.',
    p2: 'You may safely close this page.'
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
    requestedToFollow: 'Requested to follow this user.',
  },
  userSearchPage: {
    searchHint: 'Search for a user'
  },
  userSettingsPage: {
    title: 'Account Settings',
    email: 'Email',
    changeEmail: 'Change your email address',
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
    undo: 'Undo',
    actions: {
      title: 'Actions',
      requestData: 'Request Account Data',
      deleteAccount: 'Delete Account'
    }
  },
  updateEmailPage: {
    title: 'Update Email Address',
    inputHint: 'Enter your new email address here',
    beforeUpdateInfo: "To update your email address, we'll send a secret code to your new address.",
    updateButton: 'Update Email Address',
    resetCodeSent: 'A reset code has been sent to your new email address.',
    resetCodeInputHint: 'Enter your code here',
    emailUpdated: 'Your email has been updated successfully.'
  },
  requestAccountDataPage: {
    title: 'Request Account Data',
    requestButton: 'Request Account Data',
    requestSent: 'Request sent. You will receive an email with a link to download your data in a few days.'
  },
  deleteAccountPage: {
    title: 'Delete Account',
    deleteButton: 'Delete Account',
    confirmTitle: 'Confirm Deletion',
    confirmMessage: 'Are you absolutely certain that you want to delete your Gymboard account? This CANNOT be undone.',
    accountDeleted: 'Account deleted. You will now be logged out. Goodbye 😭'
  },
  submissionPage: {
    confirmDeletion: 'Confirm Deletion',
    confirmDeletionMsg: 'Are you sure you want to delete this submission? It will be removed permanently.',
    deletionSuccessful: 'Submission deleted successfully.'
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
  },
  confirm: {
    title: 'Confirm',
    message: 'Are you sure you want to continue?'
  }
};
