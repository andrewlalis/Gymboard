import {i18n} from 'boot/i18n';
import {Notify, Dialog, QDialogOptions} from 'quasar';

/**
 * Sleeps for a given number of milliseconds before resolving.
 * @param ms The milliseconds to sleep for.
 */
export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

/**
 * Shows a confirmation dialog that returns a promise which resolves if the
 * user clicks on the affirmative button choice.
 * @param options Options to supply to the dialog, instead of defaults.
 */
export function confirm(options?: QDialogOptions): Promise<void> {
  const { t } = i18n.global;
  const dialogOpts: QDialogOptions = {
    title: t('confirm.title'),
    message: t('confirm.message'),
    cancel: true
  };
  if (options?.title) {
    dialogOpts.title = options.title;
  }
  if (options?.message) {
    dialogOpts.message = options.message;
  }
  return new Promise((resolve, reject) => {
    Dialog.create(dialogOpts)
      .onOk(resolve)
      .onCancel(reject)
      .onDismiss(reject);
  });
}

function showToast(type: string, messageKey: string) {
  const { t } = i18n.global;
  Notify.create({
    message: t(messageKey),
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
  if (error) {
    console.error(error);
  }
  showToast('danger', 'generalErrors.apiError');
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
