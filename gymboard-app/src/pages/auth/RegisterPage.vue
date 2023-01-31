<template>
  <StandardCenteredPage>
    <h3 class="text-center">{{ $t('registerPage.title') }}</h3>
    <q-form @submit="tryRegister" @reset="resetForm">
      <SlimForm>
        <div class="row">
          <q-input
            :label="$t('registerPage.name')"
            v-model="registerModel.name"
            type="text"
            class="col-12"
          />
        </div>
        <div class="row">
          <q-input
            :label="$t('registerPage.email')"
            v-model="registerModel.email"
            type="email"
            class="col-12"
          />
        </div>
        <div class="row">
          <q-input
            :label="$t('registerPage.password')"
            v-model="registerModel.password"
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
            :label="$t('registerPage.register')"
            color="primary"
            class="q-mt-md col-12"
            no-caps
          />
        </div>
      </SlimForm>
    </q-form>
  </StandardCenteredPage>
</template>

<script setup lang="ts">
import SlimForm from 'components/SlimForm.vue';
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import api from 'src/api/main';
import {useRouter} from 'vue-router';
import {ref} from 'vue';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';

const router = useRouter();

const registerModel = ref({
  name: '',
  email: '',
  password: ''
});
const passwordVisible = ref(false);

const t = useI18n().t;
const quasar = useQuasar();

async function tryRegister() {
  try {
    await api.auth.register(registerModel.value);
    await router.push('/register/success');
  } catch (error) {
    quasar.notify({
      message: t('registerPage.error'),
      type: 'negative'
    });
  }
}

function resetForm() {
  registerModel.value.name = '';
  registerModel.value.email = '';
  registerModel.value.password = '';
}
</script>

<style scoped>

</style>
