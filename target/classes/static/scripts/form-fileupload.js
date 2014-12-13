var FormFileUpload = function () {


    return {
        //main function to initiate the module
        init: function () {
            $(function () {
                $('#fileupload').fileupload({
                    dataType: 'json',

                    done: function (e, data) {
                        $("tr:has(td)").remove();
                        var i = 0;
                        $.each(data.result, function (index, file) {
                            i++;
                            $("#uploaded-files").append(
                                    $('<tr/>')
                                    .append($('<td/>').text(i))
                                    .append($('<td/>').text(file.fileName))
                                    .append($('<td/>').html(file.fileTypeHtml))
                                    .append($('<td/>').text(file.fileSize))
                                    .append($('<td/>').html(file.fileActionsHtml))
                                    )//end $("#uploaded-files").append()
                        });
                    },

                    progressall: function (e, data) {
                        var progress = parseInt(data.loaded / data.total * 100, 10);
                        $('#progress .bar').css(
                            'width',
                            progress + '%'
                        );
                    },

                    dropZone: $('#dropzone')
                });
            });

        }
    };

}();