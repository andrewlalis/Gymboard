export default {
  mainLayout: {
    language: 'Taal',
    pages: "Pagina's",
    menu: {
      gyms: 'Sportscholen',
      leaderboard: 'Wereldwijd klassement',
      users: 'Gebruikers',
      adminPanel: 'Adminpaneel',
      about: 'Over Gymboard'
    }
  },
  registerPage: {
    title: 'Maak een nieuwe Gymboard account aan',
    name: 'Naam',
    email: 'E-mail',
    password: 'Wachtwoord',
    register: 'Registreren',
    error: 'Er is een fout opgetreden.',
  },
  loginPage: {
    title: 'Inloggen bij Gymboard',
    email: 'E-mail',
    password: 'Wachtwoord',
    logIn: 'Inloggen',
    createAccount: 'Account aanmaken',
    authFailed: 'Ongeldige inloggegevens.',
  },
  indexPage: {
    searchHint: 'Zoek een sportschool',
  },
  gymPage: {
    home: 'Thuis',
    submit: 'Indienen',
    leaderboard: 'Scorebord',
    homePage: {
      overview: 'Overzicht van dit sportschool:',
      recentLifts: 'Recente liften',
    },
    submitPage: {
      loginToSubmit: 'Log in of meld je aan om je lift te indienen',
      exercise: 'Oefening',
      weight: 'Gewicht',
      reps: 'Repetities',
      date: 'Datum',
      upload: 'Videobestand om te uploaden',
      submit: 'Sturen',
    },
  },
  userPage: {
    notFound: {
      title: 'Gebruiker niet gevonden',
      description: 'Wij konden de gebruiker voor wie jij zoekt niet vinden, helaas.'
    },
    accountPrivate: 'Dit account is privaat.',
    recentLifts: 'Recente liften',
    requestedToFollow: 'Gevraagd om deze gebruiker te volgen.',
  },
  userSearchPage: {
    searchHint: 'Zoek een gebruiker'
  },
  userSettingsPage: {
    title: 'Account instellingen',
    email: 'E-mail',
    name: 'Naam',
    password: 'Wachtwoord',
    passwordHint: 'Stel een nieuw wachtwoord voor je account in.',
    updatePassword: 'Wachtwoord bijwerken',
    passwordUpdated: 'Wachtwoord succesvol bijgewerkt.',
    passwordInvalid: 'Ongeldig wachtwoord.',
    personalDetails: {
      title: 'Persoonlijke gegevens',
      birthDate: 'Geboortedatum',
      sex: 'Geslacht',
      sexMale: 'Mannelijk',
      sexFemale: 'Vrouwelijk',
      sexUnknown: 'Liever niet zeggen',
      currentWeight: 'Huidige gewicht',
      currentWeightUnit: 'Eenheden van huidige gewicht'
    },
    preferences: {
      title: 'Voorkeuren',
      accountPrivate: 'Privaat',
      language: 'Taal'
    },
    save: 'Opslaan',
    undo: 'Terugzetten'
  },
  submissionPage: {
    confirmDeletion: 'Bevestig verwijderen',
    confirmDeletionMsg: 'Ben je zeker dat je dit submissie willen verwijderen? Het zal permanent verwijderd worden.',
    deletionSuccessful: 'Submissie succesvol verwijderd.'
  },
  accountMenuItem: {
    logIn: 'Inloggen',
    profile: 'Profile',
    settings: 'Instellingen',
    logOut: 'Uitloggen',
  },
  generalErrors: {
    apiError: 'Er is een API fout opgetreden. Probeer het nogmals later.'
  },
  weightUnit: {
    kilograms: 'Kilogram',
    pounds: 'Ponden'
  }
};
