var FormFileUpload = function () {


    return {
        //main function to initiate the module
        init: function () {
            $(function () {
                $('#fileupload').fileupload({
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
                        });
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