var TableAjax = function () {

    var initPickers = function () {
        //init date pickers
        $('.date-picker').datepicker({
            rtl: Metronic.isRTL(),
            autoclose: true,
            language: 'es'
        });
    }

    var handleRecords = function () {

        var grid = new Datatable();

        grid.init({
            src: $("#datatable_ajax"),
            onSuccess: function (grid) {
                // execute some code after table records loaded
            },
            onError: function (grid) {
                // execute some code on network or other general error  
            },
            loadingMessage: 'Cargando...',
            dataTable: { // here you can define a typical datatable settings from http://datatables.net/usage/options 

                // Uncomment below line("dom" parameter) to fix the dropdown overflow issue in the datatable cells. The default datatable layout
                // setup uses scrollable div(table-scrollable) with overflow:auto to enable vertical scroll(see: assets/global/scripts/datatable.js). 
                // So when dropdowns used the scrollable div should be removed. 
                //"dom": "<'row'<'col-md-8 col-sm-12'pli><'col-md-4 col-sm-12'<'table-group-actions pull-right'>>r>t<'row'<'col-md-8 col-sm-12'pli><'col-md-4 col-sm-12'>>",
                
                "bStateSave": true, // save datatable state(pagination, sort, etc) in cookie.

                "lengthMenu": [
                    [10, 20, 50, 100, 150, -1],
                    [10, 20, 50, 100, 150, "All"] // change per page values here
                ],
                "pageLength": 10, // default record count per page
                "ajax": {
                    "url": "/calidad/listaMaestraJSON", // ajax source
                    "type": "GET"
                },
                "aoColumns": [
                    { "data": "htmlHelper.htmlCheckbox" },
                    { "data": "nivel"},
                    { "data": "tipo" },
                    { "data": "codigo"},
                    { "data": "titulo"},
                    { "data": "proximaRevisionString"},
                    { "data": "departamento"},
                    { "data": "htmlHelper.htmlAction" }
                ],
                "aDataSort":[]
            }
        });

        // handle group actionsubmit button click
        grid.getTableWrapper().on('click', '.table-group-action-submit', function (e) {
            e.preventDefault();
            var action = $(".table-group-action-input", grid.getTableWrapper());
            if (action.val() != "" && grid.getSelectedRowsCount() > 0
                && confirm("¿Está seguro de archivar? Tome en cuenta que requerirá de soporte técnico para los documentos archivados."))
            {
                grid.setAjaxParam("customActionType", "group_action");
                grid.setAjaxParam("customActionName", action.val());
                grid.setAjaxParam("id", grid.getSelectedRows());
                grid.getDataTable().ajax.reload();
                grid.clearAjaxParams();
            } else if (action.val() == "") {
                Metronic.alert({
                    type: 'danger',
                    icon: 'warning',
                    message: 'Primero selecciona una acción',
                    container: grid.getTableWrapper(),
                    place: 'prepend'
                });
            } else if (grid.getSelectedRowsCount() === 0) {
                Metronic.alert({
                    type: 'danger',
                    icon: 'warning',
                    message: 'No se seleccionaron registros',
                    container: grid.getTableWrapper(),
                    place: 'prepend'
                });
            }
        });

        var tableColumnToggler = $('#sample_4_column_toggler');

        $('.my-col-toggler', tableColumnToggler).click(function () {
            /* Get the DataTables object again - this is not a recreation, just a get of the object */
            var iCol = parseInt($(this).attr("data-column"));
            var bVis = $('#datatable_ajax').dataTable().fnSettings().aoColumns[iCol].bVisible;
            $('#datatable_ajax').dataTable().fnSetColumnVis(iCol, (bVis ? false : true));
        });

    }

    return {

        //main function to initiate the module
        init: function () {

            initPickers();
            handleRecords();

        }

    };

}();