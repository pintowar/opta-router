import { createApp, defineAsyncComponent } from "vue";
import "./app.css";
import App from "./App.vue";
import { createRouter, createWebHashHistory } from "vue-router";
import { OhVueIcon, addIcons } from "oh-vue-icons";
import {
  MdAdd,
  MdCancelOutlined,
  MdChangecircle,
  MdCheck,
  MdCheckcircleOutlined,
  MdClose,
  MdContentcopy,
  MdDeleteoutline,
  MdInfoOutlined,
  MdEditTwotone,
  MdSearch,
  MdWarningamberRound,
} from "oh-vue-icons/icons/md";
import { FaGithub, FaSlackHash } from "oh-vue-icons/icons/fa";
import { OiGear } from "oh-vue-icons/icons/oi";

import { VrpLocations, VrpProblems, VrpProblemEditor, VrpVehicles, VrpSolver, VrpSolverHistory } from "./pages";

const VueApexCharts = defineAsyncComponent(() => import("vue3-apexcharts"));

addIcons(
  FaGithub,
  FaSlackHash,
  MdAdd,
  MdCancelOutlined,
  MdChangecircle,
  MdCheck,
  MdCheckcircleOutlined,
  MdClose,
  MdContentcopy,
  MdDeleteoutline,
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
  { path: "/problem/new", component: VrpProblemEditor, props: { mode: "create" } },
  { path: "/problem/:id/edit", component: VrpProblemEditor, props: { mode: "update" } },
  { path: "/problem/:id/copy", component: VrpProblemEditor, props: { mode: "copy" } },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

createApp(App).use(router).component("apexchart", VueApexCharts).component("v-icon", OhVueIcon).mount("#app");
