import {QVueGlobals} from 'quasar';

export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export function showApiErrorToast(i18n: any, quasar: QVueGlobals) {
  quasar.notify({
    message: i18n.t('generalErrors.apiError'),
    type: 'danger',
    position: 'top'
  });
}

export function showInfoToast(quasar: QVueGlobals, translatedMessage: string) {
  quasar.notify({
    message: translatedMessage,
    type: 'info',
    position: 'top'
  });
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
