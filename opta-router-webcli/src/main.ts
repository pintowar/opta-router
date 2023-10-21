import { createApp } from "vue";
import "./app.css";
import App from "./App.vue";
import { createRouter, createWebHashHistory } from "vue-router";
import VueApexCharts from "vue3-apexcharts";

import VrpProblems from "./pages/VrpProblems.vue";
import VrpSolver from "./pages/VrpSolver.vue";
import VrpSolverHistory from "./pages/VrpSolverHistory.vue";
import VrpProblemEditor from "./pages/VrpProblemEditor.vue";

const routes = [
  { path: "/", component: VrpProblems },
  { path: "/solve/:id", component: VrpSolver },
  { path: "/solver-history/:id", component: VrpSolverHistory },
  { path: "/problem/new", component: VrpProblemEditor },
  { path: "/problem/:id/edit", component: VrpProblemEditor },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

createApp(App).use(router).use(VueApexCharts).mount("#app");
