var Dashboard = function() {

    return {
        //main function to initiate the module

        init: function(data) {
            Dashboard.initPieCharts(data);
            Metronic.addResizeHandler(function() {
                Dashboard.initPieCharts(data);
            });

        },

        initPieCharts: function(data) {
            //var data = [];
            //var series = Math.floor(Math.random() * 10) + 1;
            //series = series < 5 ? 5 : series;
            var series = 3;
            /*
            for (var i = 0; i < series; i++) {
                data[i] = {
                    label: "Series" + (i + 1),
                    data: Math.floor(Math.random() * 100) + 1
                };
            } */

            // GRAPH
            if ($('#pie_chart').size() !== 0) {
                $.plot($("#pie_chart"), data, {
                    series: {
                        pie: {
                            show: true,
                            radius: 3 / 4,
                            label: {
                                show: true,
                                radius: 3 / 4,
                                formatter: function(label, series) {
                                    return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">' + label + '<br/>' + Math.round(series.percent) + '% ('+series.data[0][1]+')</div>';
                                },
                                background: {
                                    opacity: 0.5,
                                    color: '#000'
                                }
                            }
                        }
                    },
                    legend: {
                        show: false
                    }
                });
            }

            function pieHover(event, pos, obj) {
                if (!obj)
                    return;
                percent = parseFloat(obj.series.percent).toFixed(2);
                $("#hover").html('<span style="font-weight: bold; color: ' + obj.series.color + '">' + obj.series.label + ' (' + percent + '%)</span>');
            }

            function pieClick(event, pos, obj) {
                if (!obj)
                    return;
                percent = parseFloat(obj.series.percent).toFixed(2);
                alert('' + obj.series.label + ': ' + percent + '%');
            }

        }

    };

}();