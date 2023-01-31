<template>
  <StandardCenteredPage>
    <h3 class="text-center">Login to Gymboard</h3>
    <q-form @submit="tryLogin" @reset="resetLogin">
      <SlimForm>
        <div class="row">
          <q-input
            :label="$t('loginPage.email')"
            v-model="loginModel.email"
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
          <q-btn type="submit" label="Log in" color="primary" class="q-mt-md col-12" no-caps/>
        </div>
        <div class="row">
          <q-btn flat no-caps label="Create an account" color="secondary" class="q-mt-md col-12" style="text-decoration: underline"/>
        </div>
      </SlimForm>
    </q-form>
  </StandardCenteredPage>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'src/components/StandardCenteredPage.vue';
import SlimForm from 'components/SlimForm.vue';
import {ref} from 'vue';
import api from 'src/api/main';
import {useAuthStore} from 'stores/auth-store';
import {useRoute, useRouter} from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const loginModel = ref({
  email: '',
  password: ''
});
const passwordVisible = ref(false);

async function tryLogin() {
  console.log('logging in...');
  try {
    await api.auth.login(authStore, loginModel.value);
    const dest = route.query.next ? decodeURIComponent(route.query.next as string) : '/';
    await router.push(dest);
  } catch (error) {
    console.error(error);
  }
}

function resetLogin() {
  loginModel.value.email = '';
  loginModel.value.password = '';
}

</script>

<style scoped>

</style>
