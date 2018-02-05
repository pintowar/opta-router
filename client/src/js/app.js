import {WsWidget} from './ws.js'
import {FormWidget} from './form.js'
import {MapWidget} from './map.js'

const map = new MapWidget()

const form = new FormWidget(map)
new WsWidget(map, form)

form.plotSolution()
form.showStatus()