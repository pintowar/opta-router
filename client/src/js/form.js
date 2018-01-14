import {SAMPLES} from './data'

export class FormWidget {
    constructor(chart, map) {
        this.chart = chart;
        this.map = map;
        this.sampleLoader('belgium')
        this.eventDefinition()
    }

    sampleLoader(sample) {
        $('.alert').alert('close')
        const loader = $('#' + sample + '-sample-loader')
        loader.popover({trigger: 'hover', content: 'Load ' + sample + ' sample'})
        loader.on('click', () => {
            $('#map-area').val(sample)
            $('#data-input').val(JSON.stringify(SAMPLES[sample], null, 2))
        })
    }

    showError(msg) {
        const template = $($('#alert-template').html());
        template.find('#alert-msg').text(msg)
        $('#alert-area').append(template);
    }

    eventDefinition() {
        const widgets = [this.chart, this.map]
        $('#data-form').submit(e => e.preventDefault())

        $('#path-btn').on('click', (evt) => {
            this.solvePath(widgets)
        })

        $('#points-btn').on('click', (evt => {
            try {
                const data = JSON.parse($('#data-input').val())
                widgets.forEach(it => it.plotDots(data))
            } catch(e) {
                widgets.forEach(it => it.clear())
            }
        }))

        $('#terminate-btn').on('click', (evt => {
            $.ajax({
                url: '/terminate',
                success: (data => console.log(data) ),
            })
        }))

        $('#detailed-btn').on('click', (evt => {
            const active = $(evt.target).attr('class').indexOf('active') <= 0
            $.ajax({
                url: `/detailed-path/${active}`,
                success: (data => this.plotSolution() ),
                method: 'PUT'
            })
        }))
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
                [this.chart, this.map].forEach(it => it.plotPath(data) )
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
}