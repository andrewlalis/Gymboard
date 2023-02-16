<template>
  <StandardCenteredPage>
    <h3 class="text-center">{{ $t('loginPage.title') }}</h3>
    <q-form @submit="tryLogin" @reset="resetLogin">
      <SlimForm>
        <div class="row">
          <q-input
            :label="$t('loginPage.email')"
            v-model="loginModel.email"
            type="email"
            class="col-12"
          />
        </div>
        <div class="row">
          <q-input
            :label="$t('loginPage.password')"
            v-model="loginModel.password"
            :type="passwordVisible ? 'text' : 'password'"
            class="col-12"
          >
            <template v-slot:append>
              <q-icon
                :name="passwordVisible ? 'visibility' : 'visibility_off'"
                class="cursor-pointer"
                @click="passwordVisible = !passwordVisible"
              />
            </template>
          </q-input>
        </div>
        <div class="row">
          <q-btn
            type="submit"
            :label="$t('loginPage.logIn')"
            color="primary"
            class="q-mt-md col-12"
            no-caps
          />
        </div>
        <div class="row">
          <router-link
            :to="{
              path: '/register',
              query: route.query.next ? { next: route.query.next } : {},
            }"
            class="q-mt-md text-primary text-center col-12"
          >
            {{ $t('loginPage.createAccount') }}
          </router-link>
        </div>
      </SlimForm>
    </q-form>
  </StandardCenteredPage>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import SlimForm from 'components/SlimForm.vue';
import { ref } from 'vue';
import api from 'src/api/main';
import { useAuthStore } from 'stores/auth-store';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { resolveLocale } from 'src/i18n';
import { useQuasar } from 'quasar';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const i18n = useI18n({ useScope: 'global' });
const quasar = useQuasar();

const loginModel = ref({
  email: '',
  password: '',
});
const passwordVisible = ref(false);

/**
 * The main login function. It attempts to log in the user, and gracefully
 * handles failures.
 * 
 * Upon successful login, we set the app's locale to the user's preferred
 * locale, and then redirect to the pre-configured next URL (if there is one).
 */
async function tryLogin() {
  try {
    await api.auth.login(authStore, loginModel.value);

    // Set the locale to the user's preferred locale.
    i18n.locale.value = resolveLocale(authStore.user?.preferences?.locale);
    
    // Redirect back to whatever was set as the next URL.
    const dest = route.query.next
      ? decodeURIComponent(route.query.next as string)
      : '/';
    await router.push(dest);
  } catch (error: any) {
    if (error.response && error.response.status === 401) {
      quasar.notify({
        message: i18n.t('loginPage.authFailed'),
        type: 'warning',
        position: 'top',
      });
    } else {
      quasar.notify({
        message: i18n.t('generalErrors.apiError'),
        type: 'negative',
        position: 'top',
      });
      console.error(error);
    }
  }
}

function resetLogin() {
  loginModel.value.email = '';
  loginModel.value.password = '';
}
</script>

<style scoped></style>
