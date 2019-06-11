import Vue from 'vue'
import './plugins/vuetify'
import App from './App.vue'
import { Icon } from 'leaflet'
import 'leaflet/dist/leaflet.css'

import axios from 'axios'
import VueLogger from 'vuejs-logger'

delete Icon.Default.prototype._getIconUrl;

Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png')
});

Vue.use(VueLogger, {
  isEnabled: true,
  logLevel : 'info',
  stringifyArguments : false,
  showLogLevel : true,
  showMethodName : true,
  separator: '|',
  showConsoleColors: true
})
Vue.config.productionTip = false
Vue.prototype.$http = axios

new Vue({
  render: h => h(App),
}).$mount('#app')
