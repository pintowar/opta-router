<script lang="ts" setup>
import "leaflet/dist/leaflet.css";
import * as L from "leaflet";
import { LMap, LTileLayer, LMarker, LPopup, LPolyline } from "@vue-leaflet/vue-leaflet";

import { createRainbow } from "rainbow-color";
import { rgbaString } from "color-map";
import { ref, toRefs, computed, watchEffect } from "vue";

import { VrpSolution } from "../api";

const props = defineProps<{
  solution: VrpSolution | null;
}>();

const { solution } = toRefs(props);

const routerMap = ref<typeof LMap | null>(null);
const center = ref<L.PointExpression>([47.41322, -1.219482]);
const zoom = ref(3);
const minZoom = 2;
const maxZoom = 18;

const layerUrl = "https://{s}.tile.osm.org/{z}/{x}/{y}.png";
const layerOptions = { subdomains: ["a", "b", "c"] };

const polylines = computed(() => {
  const routes = solution.value?.routes || [];
  const colors = createRainbow(Math.max(routes.length, 9)).map((c) => rgbaString(c));
  const problem = solution.value?.problem;

  return routes?.map((r, idx) => {
    const vCapacity = problem?.vehicles[idx]?.capacity;
    const capacity = vCapacity ? (100 * r.totalDemand) / vCapacity : 0;
    return {
      id: `v${idx}`,
      visible: true,
      points: r.order.map((p) => [p.lat, p.lng]) as L.LatLngExpression[],
      opacity: 0.0,
      content: `<ul>
        <li>${problem?.vehicles[idx].name}</li>
        <li>Distance: ${r.distance}</li>
        <li>Time: ${r.time}</li>
        <li>
          <div class="tooltip w-full" data-tip="${capacity}%">
            <progress class="progress progress-primary w-full" value="${capacity}" max="100"></progress>
          </div>
        </li>
      </ul>`,
      color: colors[idx],
    };
  });
});

watchEffect(() => {
  const bounds: L.LatLngBounds = L.featureGroup(
    (solution?.value?.problem?.locations || []).map((e) => new L.Marker([e.lat, e.lng]))
  ).getBounds();

  if (bounds.isValid()) {
    const tmp = [
      [bounds.getSouthWest().lat, bounds.getSouthWest().lng],
      [bounds.getNorthEast().lat, bounds.getNorthEast().lng],
    ];
    routerMap?.value?.leafletObject?.fitBounds(tmp);
  }
});
</script>

<template>
  <div style="height: 100%">
    <l-map
      ref="routerMap"
      v-model:zoom="zoom"
      :min-zoom="minZoom"
      :max-zoom="maxZoom"
      :center="center"
      :use-global-leaflet="false"
    >
      <l-tile-layer :url="layerUrl" :options="layerOptions" />
      <l-marker v-for="stop in solution?.problem?.locations || []" :key="stop.id" :lat-lng="stop" :visible="true">
        <l-popup :content="stop.name + ' (' + stop.lat + ', ' + stop.lng + ')'" />
      </l-marker>
      <l-polyline
        v-for="item in polylines"
        :key="item.id"
        :lat-lngs="item.points"
        :visible="item.visible"
        :color="item.color"
        :fill-opacity="item.opacity"
      >
        <l-popup :content="item.content" />
      </l-polyline>
    </l-map>
  </div>
</template>
