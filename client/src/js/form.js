import {SAMPLES} from './data'

export class FormWidget {
    constructor(map) {
        this.map = map
        this.pathBtn = $('#path-btn')
        this.terminateBtn = $('#terminate-btn')
        this.destroyBtn = $('#destroy-btn')
        this.detailedBtn = $('#detailed-btn')
        this.buttons = [this.pathBtn, this.terminateBtn, this.destroyBtn, this.detailedBtn]

        this.disable()
        this.sampleLoader('belgium')
        this.eventDefinition()
    }

    sampleLoader(sample) {
        $('.alert').alert('close')
        const loader = $('#' + sample + '-sample-loader')
        loader.on('click', () => {
            $('#data-input').val(JSON.stringify(SAMPLES[sample], null, 2))
        })
    }

    showError(msg) {
        const template = $($('#alert-template').html())
        template.find('#alert-msg').text(msg)
        $('#alert-area').append(template)
    }

    eventDefinition() {
        const widgets = [this.map]
        $('#data-form').submit(e => e.preventDefault())

        this.pathBtn.on('click', (evt) => {
            this.solvePath(widgets)
        })

        this.terminateBtn.on('click', (evt => {
            $.ajax({
                url: '/terminate',
                success: (data => console.log(data) ),
            })
        }))

        this.destroyBtn.on('click', (evt => {
            $.ajax({
                url: '/clean',
                success: (data => console.log(data) ),
            })
        }))

        this.detailedBtn.on('click', (evt => {
            const active = $(evt.target).attr('class').indexOf('active') <= 0
            $.ajax({
                url: `/detailed-path/${active}`,
                success: (data => this.plotSolution() ),
                method: 'PUT'
            })
        }))

        $.ajax({
            url: '/instance',
            success: (data => {
                if(!!data) $('#data-input').val(JSON.stringify(data, null, 2))
            })
        })
    }

    setStatus(msg) {
        $("#running-status").text(msg)
    }

    showStatus() {
        $.ajax({
            url: '/status',
            success: (data => {
                this.setStatus(data.status)
                const dtl = $('#detailed-btn')
                if(data['detailed-path'] == 'true' && dtl.attr('class').indexOf('active') <= 0) {
                    dtl.button('toggle')
                }
            })
        })
    }

    plotSolution() {
        $.ajax({
            url: '/solution',
            success: (data => {
                [this.map].forEach(it => it.plotPath(data) )
            })
        })
    }

    solvePath(widgets) {
        $.ajax({
            contentType: 'application/json',
            data: $('#data-input').val(),
            dataType: 'json',
            success: (data => console.log(data)),
            error: (e => {
                // this.showError(e.responseText)
                this.showError(e.responseJSON.message)
                widgets.forEach(it => it.clear())
            }),
            processData: false,
            type: 'POST',
            url: '/solve'
        })
    }

    enable() {
        this.buttons.forEach( btn => btn.removeAttr( "disabled" ) )
    }

    disable() {
        this.buttons.forEach( btn => btn.attr("disabled", "disabled") )
    }
}