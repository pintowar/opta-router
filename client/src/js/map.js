import Rainbow from 'color-rainbow'

export class MapWidget {
    constructor() {
        this.mapPathDist = $('#map-path-dist')
        this.map = L.map('mapid').setView([0.0, 0.0], 2)
        L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', { maxZoom: 18 }).addTo(this.map)
        this.markers = []
        this.paths = []
    }

    plotPathMap(data) {
        this.clearMap()
        const colors = Rainbow.create(data.length).map( c => `rgb(${c.values.rgb.join(', ')})` )
        const points = []
        data.forEach((route, idx) => {
            for(var i = 0; i < route.length - 1; i++) {
                const path = L.polyline([[route[i].lat, route[i].lon], [route[i+1].lat, route[i+1].lon]], {color: colors[idx], weight: 4})
                this.paths.push(path)
                path.addTo(this.map)
                points.push([route[i].lat, route[i].lon])
            }
        })
        if(points.length > 0) this.map.fitBounds(points)
    }

    plotPath(data) {
        this.plotPathMap(data.routes.map(r => r.order))
        const dist = !!data.totalDistance ? data.totalDistance : ''
        const time = !!data.totalTime ? data.totalTime : ''
        this.mapPathDist.text(`Total distance: ${dist} Km | Max route time: ${time} mins`)
    }

    clearMap() {
        const map = this.map
        this.markers.forEach(el => map.removeLayer(el))
        this.paths.forEach(el => map.removeLayer(el))
    }

    clear() {
        this.mapPathDist.text('')
    }
}