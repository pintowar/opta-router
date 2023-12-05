import { createApp } from "vue";
import "./app.css";
import App from "./App.vue";
import { createRouter, createWebHashHistory } from "vue-router";
import { OhVueIcon, addIcons } from "oh-vue-icons";
import {
  BiCheckLg,
  BiX,
  FaGithub,
  FaSlackHash,
  LaTrashSolid,
  MdAdd,
  MdCancelOutlined,
  MdCheckcircleOutlined,
  MdInfoOutlined,
  MdEditTwotone,
  MdWarningamberRound,
  OiGear,
} from "oh-vue-icons/icons";
import VueApexCharts from "vue3-apexcharts";

import VrpProblems from "./pages/VrpProblems.vue";
import VrpSolver from "./pages/VrpSolver.vue";
import VrpSolverHistory from "./pages/VrpSolverHistory.vue";
import VrpProblemEditor from "./pages/VrpProblemEditor.vue";
import VrpLocations from "./pages/VrpLocations.vue";

addIcons(
  BiCheckLg,
  BiX,
  FaGithub,
  FaSlackHash,
  LaTrashSolid,
  MdAdd,
  MdCancelOutlined,
  MdCheckcircleOutlined,
  MdInfoOutlined,
  MdEditTwotone,
  MdWarningamberRound,
  OiGear
);

const routes = [
  { path: "/", component: VrpProblems },
  { path: "/solve/:id", component: VrpSolver },
  { path: "/solver-history/:id", component: VrpSolverHistory },
  { path: "/locations", component: VrpLocations },
  { path: "/problem/new", component: VrpProblemEditor },
  { path: "/problem/:id/edit", component: VrpProblemEditor },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

createApp(App).use(router).use(VueApexCharts).component("v-icon", OhVueIcon).mount("#app");
