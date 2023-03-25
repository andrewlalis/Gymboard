/**
 * This store keeps track of global state that's only relevant for development
 * purposes, like debug flags and settings.
 */
import {defineStore} from 'pinia';

interface DevState {
  showDebugInfo: boolean;
}

export const useDevStore = defineStore('devStore', {
  state: (): DevState => {
    return { showDebugInfo: !!process.env.DEV };
  }
});

export type DevStoreType = ReturnType<typeof useDevStore>;
