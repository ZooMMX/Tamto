var FormFileUpload = function () {


    return {
        //main function to initiate the module
        init: function () {
            $(function () {
                $('#fileupload').fileupload({

                    done: function (e, data) {

                        bootbox.alert("Importación completada correctamente.");
                        /*
                        $("tr:has(td)").remove();
                        var i = 0;
                        $.each(data.result, function (index, file) {
                            i++;
                            $("#uploaded-files").append(

                                    $('<tr/>')
                                    .append($('<td/>').text(i))
                                    .append($('<td/>').text(file.fileName))
                                    .append($('<td/>').html(file.tamtoTypeHtml))
                                    .append($('<td/>').text(file.fileSize))
                                    .append($('<td/>').html(file.fileActionsHtml))
                                    )//end $("#uploaded-files").append()
                        });*/
                    },
                    fail: function(e, data) {
                        bootbox.alert("Formato de importación es incorrecto, no se pudo concluir la importación, todos los cambios fueron revertidos.");
                    },
                    always: function(e, data) {
                        Metronic.stopPageLoading();
                    },
                    start: function(e) {
                        Metronic.startPageLoading({ message: "Ten paciencia, cargando y convirtiendo..." });
                    }
                });
            });

        }
    };

}();