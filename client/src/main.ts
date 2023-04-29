import { createApp } from 'vue'
import './app.css'
import App from './App.vue'
import { createRouter, createWebHashHistory } from "vue-router";

import Home from './pages/Home.vue'
import Solver from "./pages/Solver.vue"

const routes = [
  { path: '/', component: Home },
  { path: '/solve/:id', component: Solver },
]

const router = createRouter({
    history: createWebHashHistory(),
    routes,
  });

createApp(App)
.use(router)
.mount('#app');
