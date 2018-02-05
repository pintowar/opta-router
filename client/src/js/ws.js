export class WsWidget {

    constructor(map, form) {
        this.map = map;
        this.form = form;
        this.eventDefinition()
    }


    eventDefinition() {
        const widgets = [this.map]
        const form = this.form

        const socket = new SockJS('/stomp')
        const client = Stomp.over(socket)
        client.debug = null
        let self = this
        client.connect({}, function(frame) {
            console.log('Connected: ' + frame)
            client.subscribe("/user/queue/solution", function(message) {
                const data = JSON.parse(message.body)
                widgets.forEach(it => it.plotPath(data))
            });

            client.subscribe("/user/queue/status", function(message) {
                form.setStatus(message.body)
            });

            form.enable()
        });
    }

}