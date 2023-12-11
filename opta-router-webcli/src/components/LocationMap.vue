<script lang="ts" setup>
import "leaflet/dist/leaflet.css";
import * as L from "leaflet";
import { LMap, LTileLayer, LMarker, LPopup, LPolyline } from "@vue-leaflet/vue-leaflet";

import { createRainbow } from "rainbow-color";
import { rgbaString } from "color-map";
import { ref, toRefs, computed, watchEffect } from "vue";

import { Customer, Depot, VehicleRoute, isDepot } from "../api";

const props = defineProps<{
  locations: (Depot | Customer)[];
  routes?: VehicleRoute[];
  selectedLocation?: Depot | Customer | null;
  editMode?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:selectedLocation", val: Depot | Customer | null | undefined): void;
  (e: "markerClick", val: L.LeafletMouseEvent): void;
}>();

const { locations, routes, selectedLocation, editMode } = toRefs(props);

const componentLocation = computed({
  get: () => selectedLocation?.value,
  set: (val) => emit("update:selectedLocation", val),
});

const formatter = new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 });

const routerMap = ref<typeof LMap | null>(null);

const center = ref<L.PointExpression>([0, 0]);
const zoom = ref(3);
const minZoom = 2;
const maxZoom = 18;
const mapOptions = { attributionControl: false };

const layerUrl = "https://{s}.tile.osm.org/{z}/{x}/{y}.png";
const layerOptions = { subdomains: ["a", "b", "c"] };

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

function getIcon(location: Depot | Customer) {
  return L.icon({
    iconUrl: isDepot(location) ? "/industry.svg" : "/building.svg",
    iconSize: [40, 40],
    className: isHighlighted(location) ? "rounded-full bg-orange-300" : "",
  });
}

function locationKey(location: Depot | Customer): string {
  return `${location.name} (${location.lat}, ${location.lng})`;
}

function markerClickHandler(e: L.LeafletMouseEvent) {
  if (!componentLocation.value) {
    const attribution = JSON.parse(e.target.options.attribution);
    const location = locations.value.find((loc) => loc.id === attribution.locationId);
    componentLocation.value = location;
    emit("markerClick", e);
  }
}

function markerDropHandler(e: L.DragEndEvent) {
  if (componentLocation.value) {
    const actual = componentLocation.value;
    const coord = e.target._latlng;
    const updated = { ...actual, ...{ lat: coord.lat.toFixed(6), lng: coord.lng.toFixed(6) } };
    componentLocation.value = updated;
  }
}

function isHighlighted(location: Depot | Customer) {
  return editMode.value && location.id === componentLocation.value?.id;
}

watchEffect(() => {
  const points = locations.value.concat(componentLocation?.value ? [componentLocation?.value] : []);
  const bounds: L.LatLngBounds = L.featureGroup(points.map((e) => new L.Marker([e.lat, e.lng]))).getBounds();

  if (bounds.isValid()) {
    const tmp = [
      [bounds.getSouthWest().lat, bounds.getSouthWest().lng],
      [bounds.getNorthEast().lat, bounds.getNorthEast().lng],
    ];
    routerMap?.value?.leafletObject?.fitBounds(tmp);
    if (componentLocation?.value) {
      center.value = [componentLocation.value.lat, componentLocation.value.lng];
    }
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
      :options="mapOptions"
    >
      <l-tile-layer :url="layerUrl" :options="layerOptions" />
      <l-marker
        v-for="location in locations || []"
        :key="locationKey(location)"
        :name="locationKey(location)"
        :icon="getIcon(location)"
        :lat-lng="location"
        :draggable="isHighlighted(location)"
        :attribution="`{ &quot;locationId&quot;: ${location.id} }`"
        @click="markerClickHandler"
        @dragend="markerDropHandler"
      >
        <l-popup v-if="!isHighlighted(location)" :content="locationKey(location)" />
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
