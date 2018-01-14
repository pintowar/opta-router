export class WsWidget {

    constructor(chart, map, form) {
        this.chart = chart;
        this.map = map;
        this.form = form;
        this.eventDefinition()
    }


    eventDefinition() {
        const widgets = [this.chart, this.map]
        const form = this.form

        const socket = new SockJS('/stomp')
        const client = Stomp.over(socket)
        client.debug = null
        let self = this
        client.connect({}, function() {
            client.subscribe("/topic/solution", function(message) {
                const data = JSON.parse(message.body)
                widgets.forEach(it => it.plotPath(data))
            });

            client.subscribe("/topic/status", function(message) {
                form.setStatus(message.body)
            });
        });
    }

}