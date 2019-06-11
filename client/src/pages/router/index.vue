<template>
    <v-layout row wrap>
      <v-flex sm12>
        <v-alert :value="showError" type="error" transition="scale-transition" dismissible @input="showError=false">
          {{errorMsg}}
        </v-alert>
      </v-flex>
      <v-flex sm6>
        <v-card height="100%" min-height="600">
          <v-btn fab dark @click="loadSample('belgium')">
            <v-icon dark>list</v-icon>
          </v-btn>
          <v-chip color="primary" text-color="white">{{status}}</v-chip>
          <vue-json-editor v-model="request" :show-btns="false" :mode="'tree'" :modes="['tree', 'code']"/>
          <div>
            <v-btn round color="success" :disabled="disabled" @click="solve()">Solve</v-btn>
            <v-btn round color="warning" :disabled="disabled" @click="terminate()">Terminate</v-btn>
            <v-btn round color="error" :disabled="disabled" @click="destroy()">Destroy</v-btn>
            <v-switch v-model="detailedPath" :label="`Detailed Path`" :disabled="disabled" @change="showDetailedPath()"></v-switch>
          </div>
          <v-expansion-panel>
            <v-expansion-panel-content>
              <template v-slot:header>
                <div>Instructions </div>
              </template>
              <v-card >
                <v-card-text>
                  <ul class="list-group list-group-flush">
                    <li class="list-group-item"><b>Load Sample: </b>loads a json input with sample data (link on the top of this widget).</li>
                    <li class="list-group-item"><b>Solve: </b>start solving the inputed instance.</li>
                    <li class="list-group-item"><b>Terminate: </b>stops the solver.</li>
                    <li class="list-group-item"><b>Destroy: </b>stops and removes the best solution so far.</li>
                    <li class="list-group-item"><b>Detailed Path: </b>shows a detailed path of the solution (recommended to use on terminated solvers).</li>
                  </ul>
                </v-card-text>
              </v-card>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-card>
      </v-flex>
      <v-flex sm6>
        <v-card height="100%" min-height="600">
          <v-card-text>
            <l-map ref="routerMap" :zoom.sync="map.zoom" :min-zoom="map.minZoom" :max-zoom="map.maxZoom" style="height: 600px" >
              <l-tile-layer :url="map.layerUrl" :options="map.layerOptions"/>
              <l-marker v-for="(item) in request.stops" :key="item.id" :lat-lng="item" :visible="item.visible" >
                <l-popup :content="item.name + ' ('+ item.lat + ', '+ item.lng +')'" />
              </l-marker>
              <l-polyline v-for="item in polylines" :key="item.id" :lat-lngs="item.points" :visible="item.visible"
                          :color="item.color" :fillOpacity="item.opacity">
                <l-popup :content="item.content" />
              </l-polyline>
            </l-map>
          </v-card-text>
          <v-card-actions>
            Total Distance: {{totalDistance}} | Total Time: {{totalTime}}
          </v-card-actions>
        </v-card>
      </v-flex>
    </v-layout>
</template>

<script>
import SAMPLE from '@/samples/index'
import L from 'leaflet'
import { LMap, LTileLayer, LMarker, LPopup, LPolyline } from 'vue2-leaflet'
import vueJsonEditor from 'vue-json-editor'

import Rainbow from 'color-rainbow'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

export default {
  name: 'router',
  components: {
    vueJsonEditor, LMap, LTileLayer, LMarker, LPopup, LPolyline
  },
  created () {
    const self = this
    self.showStatus()
    .then( () => self.showInstance())
    .then( () => {
      const socket = new SockJS('/stomp')
      self.client = Stomp.over(socket)
      self.client.debug = null

      self.client.connect({}, () => {
        self.client.subscribe("/user/queue/solution", message => {
            const data = JSON.parse(message.body)
            self.totalDistance = data.totalDistance
            self.totalTime = data.totalTime
            self.routes = data.routes
        })

        self.client.subscribe("/user/queue/status", message => {
            self.status = message.body
        })
      })
    }).then( () => {
      self.disabled = false
    })
  },
  beforeDestroy () {
    this.client.disconnect()
  },
  data: () => ({
    request: {
      stops: []
    },
    routes: [],
    totalDistance: 0.0,
    totalTime: 0.0,
    detailedPath: false,
    map: {
      zoom: 3,
      minZoom: 2,
      maxZoom: 18,
      layerUrl: 'https://{s}.tile.osm.org/{z}/{x}/{y}.png',
      layerOptions: {
        subdomains: ['a', 'b', 'c']
      }
    },
    errorMsg: "",
    showError: false,
    client: null,
    status: null,
    disabled: true
  }),
  watch: {
    request (newVal) {
      const bounds = L.featureGroup((newVal.stops || [])
        .map(e => new L.Marker([e.lat, e.lng]))).getBounds()
      if (this.$refs.routerMap !== undefined) this.$refs.routerMap.fitBounds(bounds)     
    }
  },
  computed: {
    polylines () {
      const colors = Rainbow.create(this.routes.length).map( c => `rgb(${c.values.rgb.join(', ')})` )
      return this.routes.map((r, idx) => ({
          id: `v${idx}`,
          visible: true,
          points: r.order.map((p) => [p.lat, p.lng]),
          opacity: 0.0,
          content: `<ul><li>ID: v${idx}</li><li>Distance: ${r.distance}</li><li>Time: ${r.time}</li></ul>`,
          color: colors[idx]
        })
      )
    }
  },
  methods: {
    loadSample (key) {
      try {
        this.request = SAMPLE[key]
      } catch (e) {
        this.request = {}
      }
    },
    generalAction(method, url, data, cback) {
      const self = this
      return this.$http({method: method, 
                  url: url, 
                  data: data})
      .then(cback, error => {
        self.errorMsg = error.message
        self.showError = true
      })
    },
    solve () {
      return this.generalAction('post', '/solve', this.request, res => {
        this.$log.info(res.data)
      })
    },
    terminate () {
      return this.generalAction('get', '/terminate', {}, res => {
        this.$log.info(res.data)
      })
    },
    destroy () {
      return this.generalAction('get', '/clean', {}, () => {
        this.routes = []
        this.request = {}
        this.totalDistance = 0.0
        this.totalTime = 0.0
      })
    },
    showDetailedPath () {
      return this.generalAction('put', `/detailed-path/${this.detailedPath}`, {}, res => {
        this.$log.info(res.data)
        this.showSolution()
      })
    },
    showInstance () {
      return this.generalAction('get', '/instance', {}, res => {
        this.request = (res.data || {})
      }) 
    },
    showStatus () {
      return this.generalAction('get', '/status', {}, res => {
        this.status = res.data.status
        this.detailedPath = (res.data['detailed-path'] == 'true')
      })
    },
    showSolution () {
      return this.generalAction('get', '/solution', {}, res => {
        this.routes = (res.data.routes || [])
      }) 
    }
  }
}
</script>

<style>

</style>
