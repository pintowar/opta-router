import { createApp } from "vue";
import "./app.css";
import App from "./App.vue";
import { createRouter, createWebHashHistory } from "vue-router";

import VrpProblems from "./pages/VrpProblems.vue";
import VrpSolver from "./pages/VrpSolver.vue";

const routes = [
  { path: "/", component: VrpProblems },
  { path: "/solve/:id", component: VrpSolver },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

createApp(App).use(router).mount("#app");
