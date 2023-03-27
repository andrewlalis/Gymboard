import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';

/**
 * Sleeps for a given number of milliseconds before resolving.
 * @param ms The milliseconds to sleep for.
 */
export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

function showToast(type: string, messageKey: string) {
  const quasar = useQuasar();
  const i18n = useI18n();
  quasar.notify({
    message: i18n.t(messageKey),
    type: type,
    position: 'top'
  });
}

/**
 * Shows a generic API error message toast notification, for when an API call
 * fails to return any status. This should only be called within the context of
 * a Vue component, as it utilizes some dependencies that are only available
 * then.
 * @param error The error to display.
 */
export function showApiErrorToast(error?: unknown) {
  showToast('danger', 'generalErrors.apiError');
  if (error) {
    console.error(error);
  }
}

export function showInfoToast(messageKey: string) {
  showToast('info', messageKey);
}

export function showSuccessToast(messageKey: string) {
  showToast('positive', messageKey);
}

export function showWarningToast(messageKey: string) {
  showToast('warning', messageKey);
}

export function getDocumentHeight() {
  const d = document;
  return Math.max(
    d.body.scrollHeight, d.documentElement.scrollHeight,
    d.body.offsetHeight, d.documentElement.offsetHeight,
    d.body.clientHeight, d.documentElement.clientHeight
  );
}

export function isScrolledToBottom(margin = 0) {
  return window.scrollY + window.innerHeight + margin >= getDocumentHeight();
}
