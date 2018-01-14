import Rainbow from 'color-rainbow'

export class MapWidget {
    constructor() {
        this.markerColors = {
            blue: this.createIcon('blue'),
            red: this.createIcon('red'),
            green: this.createIcon('green')
        }
        this.mapPathDist = $('#map-path-dist')
        this.map = L.map('mapid').setView([0.0, 0.0], 2);
        L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', { maxZoom: 18 }).addTo(this.map);
        this.markers = []
        this.paths = []
    }

    createIcon(color) {
        return new L.Icon({
            iconUrl: 'https://cdn.rawgit.com/pointhi/leaflet-color-markers/master/img/marker-icon-' + color + '.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        });
    }

    plotPathMap(data) {
        this.clearMap()
        const colors = Rainbow.create(data.length).map( c => `rgb(${c.values.rgb.join(', ')})` )
        data.forEach((route, idx) => {
            for(var i = 0; i < route.length - 1; i++) {
                const path = L.polyline([[route[i].lat, route[i].lon], [route[i+1].lat, route[i+1].lon]], {color: colors[idx], weight: 4});
                this.paths.push(path);
                path.addTo(this.map);
            }
        })
    }

    plotScatterMap(data) {
        const map = this.map
        if(data && data.length > 0) {
            this.clearMap()
            const colors = this.markerColors;
            this.markers = data.map((el, idx) => {
                return L.marker([el.lat, el.lon], { icon: colors.red });
            });
            this.markers.forEach(el => el.addTo(map));
            this.map.fitBounds(new L.featureGroup(this.markers).getBounds());
        }
    }

    plotDots(data) {
        this.plotScatterMap(data.stops);
        this.mapPathDist.text('');
    }

    plotPath(data) {
        this.plotPathMap(data.routes.map(r => r.order));
        const dist = !!data.totalDistance ? data.totalDistance / 1000 : ''
        const time = !!data.totalTime ? data.totalTime / (60 * 1000) : ''
        this.mapPathDist.text(`Total distance: ${dist} Km | Total time: ${time} mins`);
    }

    clearMap() {
        const map = this.map
        this.markers.forEach(el => map.removeLayer(el));
        this.paths.forEach(el => map.removeLayer(el));
    }

    clear() {
        this.plotScatterMap([]);
        this.mapPathDist.text('');
    }
}