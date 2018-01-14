import Rainbow from 'color-rainbow'

export class ChartWidget {
    
    constructor() {
        this.pathDist = $('#path-dist')
    }

    plotPathCanvas(data) {
        this.plotDataCanvas(data, 'line')
    }

    plotScatterCanvas(data) {
        this.plotDataCanvas(data, 'scatter')
    }

    plotDataCanvas(data, type) {
        const ctx = document.getElementById("canvas")//.getContext("2d");
        new Chart(ctx, {
            type: type,
            data: data,
            options: {
                animation : false,
                scales: {
                    xAxes: [{
                        type: 'linear',
                        position: 'bottom'
                    }]
                }
            }
        });
    }

    changeData(id, routes) {
        const colors = Rainbow.create(routes.length).map( c => `rgb(${c.values.rgb.join(', ')})` )
        return {
            datasets: routes.map((route, idx) => ({
                label: `Vehicle ${idx}`,
                borderColor: colors[idx],
                tension: 0.1,
                fill: false,
                backgroundColor: colors[idx],
                data: route.map(el => ({x: el.lon, y: el.lat}) )
            }))
        }
    }

    plotDots(data) {
        if(!!data.stops) {
            this.plotScatterCanvas(this.changeData(data.id, [data.stops]))
            this.pathDist.text('')
        }
    }

    plotPath(data) {
        this.plotPathCanvas(this.changeData('', data.routes.map(r => r.order)))
        const dist = !!data.totalDistance ? data.totalDistance / 1000 : ''
        const time = !!data.totalTime ? data.totalTime / (60 * 1000) : ''
        this.pathDist.text(`Total distance: ${dist} Km | Total time: ${time} mins`)
    }

    clear() {
        this.plotScatterCanvas([]);
        this.pathDist.text('');
    }
}