import { createApp } from "vue";
import "./app.css";
import App from "./App.vue";
import { createRouter, createWebHashHistory } from "vue-router";
import { OhVueIcon, addIcons } from "oh-vue-icons";
import {
  FaGithub,
  FaSlackHash,
  LaTrashSolid,
  MdAdd,
  MdCancelOutlined,
  MdCheck,
  MdCheckcircleOutlined,
  MdClose,
  MdInfoOutlined,
  MdEditTwotone,
  MdSearch,
  MdWarningamberRound,
  OiGear,
} from "oh-vue-icons/icons";
import VueApexCharts from "vue3-apexcharts";

import { VrpLocations, VrpProblems, VrpProblemEditor, VrpVehicles, VrpSolver, VrpSolverHistory } from "./pages";

addIcons(
  FaGithub,
  FaSlackHash,
  LaTrashSolid,
  MdAdd,
  MdCancelOutlined,
  MdCheck,
  MdCheckcircleOutlined,
  MdClose,
  MdInfoOutlined,
  MdEditTwotone,
  MdSearch,
  MdWarningamberRound,
  OiGear
);

const routes = [
  { path: "/", component: VrpProblems },
  { path: "/solve/:id", component: VrpSolver },
  { path: "/solver-history/:id", component: VrpSolverHistory },
  { path: "/locations", component: VrpLocations },
  { path: "/vehicles", component: VrpVehicles },
  { path: "/problem/new", component: VrpProblemEditor },
  { path: "/problem/:id/edit", component: VrpProblemEditor },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

createApp(App).use(router).use(VueApexCharts).component("v-icon", OhVueIcon).mount("#app");
