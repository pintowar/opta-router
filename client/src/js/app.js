import {WsWidget} from './ws.js'
import {FormWidget} from './form.js'
import {ChartWidget} from './chart.js'
import {MapWidget} from './map.js'

const chart = new ChartWidget()
const map = new MapWidget()

const form = new FormWidget(chart, map)
new WsWidget(chart, map, form)

form.plotSolution()
form.showStatus()