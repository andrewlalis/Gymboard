import enUS from './en-US';
import nlNL from './nl-NL';
import de from './de';

export default {
  'en-US': enUS,
  'nl-NL': nlNL,
  de: de,
};

export const supportedLocales = [
  { value: 'en-US', label: 'English' },
  { value: 'nl-NL', label: 'Nederlands' },
  { value: 'de', label: 'Deutsch' },
];

/**
 * Tries to find a locale with the given code, or defaults to the base locale.
 * @param code The locale code.
 * @returns The locale that was resolved.
 */
export function resolveLocale(code?: string) {
  for (const loc of supportedLocales) {
    if (loc.value === code) {
      return loc;
    }
  }
  return supportedLocales[0];
}
