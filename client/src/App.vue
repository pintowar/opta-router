<template>
  <v-app>
    <v-toolbar color="indigo" dark fixed app>
      <v-toolbar-title>Opta Router</v-toolbar-title>
    </v-toolbar>
    <v-content>
      <Router/>
    </v-content>
    <v-footer color="indigo" app inset>
      <p class="text-md-center white--text">Copyright &copy; VRP Viewer 2018</p>
    </v-footer>
  </v-app>
</template>

<script>
import Router from './pages/router/index'

export default {
  name: 'App',
  components: {
    Router
  }
}
</script>
