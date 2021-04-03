import { createApp } from 'vue';
import App from './App.vue';

import 'primevue/resources/themes/saga-blue/theme.css';
import 'primevue/resources/primevue.min.css';
import 'primeicons/primeicons.css';
import PrimeVue from 'primevue/config';
import Card from "primevue/card";
const app = createApp(App);

app.use(PrimeVue);
app.component('Card', Card)

app.mount('#app');
