import { boot } from 'quasar/wrappers';
import { createI18n } from 'vue-i18n';

import messages from 'src/i18n';

export type MessageLanguages = keyof typeof messages;
// Type-define 'en-US' as the master schema for the resource
export type MessageSchema = (typeof messages)['en-US'];

// See https://vue-i18n.intlify.dev/guide/advanced/typescript.html#global-resource-schema-type-definition
/* eslint-disable @typescript-eslint/no-empty-interface */
declare module 'vue-i18n' {
  // define the locale messages schema
  export interface DefineLocaleMessage extends MessageSchema {}

  // define the datetime format schema
  export interface DefineDateTimeFormat {}

  // define the number format schema
  export interface DefineNumberFormat {}
}
/* eslint-enable @typescript-eslint/no-empty-interface */

export const i18n = createI18n({
  locale: 'en-US',
  legacy: false,
  messages,
});

export default boot(({ app }) => {
  // Set the locale to the preferred locale, if possible.
  const userLocale = window.navigator.language;
  if (userLocale === 'nl-NL') {
    i18n.global.locale.value = userLocale;
  } else {
    i18n.global.locale.value = 'en-US';
  }

  // Temporary override if you want to test a particular locale.
  // i18n.global.locale.value = 'nl-NL';

  // Set i18n instance on app
  app.use(i18n);
});
