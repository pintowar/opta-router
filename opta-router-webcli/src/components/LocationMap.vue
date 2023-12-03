<script lang="ts" setup>
import "leaflet/dist/leaflet.css";
import * as L from "leaflet";
import { LMap, LTileLayer, LMarker, LPopup, LPolyline } from "@vue-leaflet/vue-leaflet";

import { createRainbow } from "rainbow-color";
import { rgbaString } from "color-map";
import { ref, toRefs, computed, watchEffect } from "vue";

import { Customer, Depot, VehicleRoute } from "../api";

const props = defineProps<{
  locations: (Depot | Customer)[];
  routes?: VehicleRoute[];
}>();

const { locations, routes } = toRefs(props);

const formatter = new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 });

const routerMap = ref<typeof LMap | null>(null);
const center = ref<L.PointExpression>([47.41322, -1.219482]);
const zoom = ref(3);
const minZoom = 2;
const maxZoom = 18;

const layerUrl = "https://{s}.tile.osm.org/{z}/{x}/{y}.png";
const layerOptions = { subdomains: ["a", "b", "c"] };

function isDepot(obj: unknown): obj is Depot {
  return Boolean(obj && typeof obj === "object" && !("demand" in obj));
}

const icons = computed(() => {
  return (locations.value || []).map((location) => {
    return L.icon({
      iconUrl: isDepot(location) ? "/industry.svg" : "/building.svg",
      iconSize: [35, 45],
    });
  });
});

const polylines = computed(() => {
  const colors = createRainbow(Math.max(routes?.value?.length || 0, 9)).map((c) => rgbaString(c));

  return (routes?.value || [])?.map((r, idx) => {
    const vCapacity = r.vehicle?.capacity;
    const capacity = vCapacity ? (100 * r.route.totalDemand) / vCapacity : 0;
    return {
      id: `v${idx}`,
      visible: true,
      points: r.route.order.map((p) => [p.lat, p.lng]) as L.LatLngExpression[],
      opacity: 0.0,
      content: `<ul>
        <li>${r.vehicle?.name || "none"}</li>
        <li>Distance: ${r.route.distance}</li>
        <li>Time: ${r.route.time}</li>
        <li>
          <div class="tooltip w-full" data-tip="${formatter.format(capacity)}%">
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
    (locations.value || []).map((e) => new L.Marker([e.lat, e.lng]))
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
      v-if="locations.length > 0"
      ref="routerMap"
      v-model:zoom="zoom"
      :min-zoom="minZoom"
      :max-zoom="maxZoom"
      :center="center"
      :use-global-leaflet="false"
    >
      <l-tile-layer :url="layerUrl" :options="layerOptions" />
      <l-marker v-for="(stop, idx) in locations || []" :key="stop.id" :icon="icons[idx]" :lat-lng="stop">
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
