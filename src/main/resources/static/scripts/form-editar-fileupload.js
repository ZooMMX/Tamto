var FormEditarFileUpload = function () {


    return {
        //main function to initiate the module
        init: function () {
            $(function () {
                $('#editar-fileupload').fileupload({
                    dataType: 'json',
                    /* Agrega primero el elemento a HTML antes de permitir cargarlo
                    add: function (e, data) {
                        data.context = $('<button/>').text('Upload')
                            .appendTo($('#uploadArea'))
                            .click(function () {
                                data.context = $('<p/>').text('Uploading...').replaceAll($(this));
                                data.submit();
                            });
                    }, */

                    done: function (e, data) {
                        $('#fileName').val(data.result.fileName);
                        $('#fileSize').val(data.result.fileSize);
                        $('#tipo')    .html(data.result.tamtoTypeHtml);
                        alert('Archivo actualizado');
                    },

                    progressall: function (e, data) {
                                var progress = parseInt(data.loaded / data.total * 100, 10);
                                $('#progress .progress-bar').css(
                                    'width',
                                    progress + '%'
                                );
                    },

                    //dropZone: $('#dropzone')
                });
            });

        }
    };

}();