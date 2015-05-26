var FormValidation = function () {

    var handleValidation1 = function() {
            // for more info visit the official plugin documentation:
                // http://docs.jquery.com/Plugins/Validation

                var form1 = $('#formi');
                var error1 = $('.alert-danger', form1);
                var success1 = $('.alert-success', form1);

                form1.validate({
                    errorElement: 'span', //default input error message container
                    errorClass: 'help-block help-block-error', // default input error message class
                    focusInvalid: false, // do not focus the last invalid input
                    ignore: ".ignore",  // validate all fields including form hidden input
                    rules: {
                        titulo: {
                            minlength: 3,
                            maxlength: 255,
                            required: true
                        },
                        codigo: {
                            minlength: 3,
                            maxlength: 20,
                            required: true,
                            validateInputmask: $('#codigo-field')
                        },
                        version: {
                            min: $('#currentVersion').val(),
                            required: true,
                            validateInputmask: $('#version-field')
                        },
                        fechaElaboracion: {
                            dateITA: true,
                            required: true
                        },
                        ultimaAprobacion: {
                            dateITA: true,
                            required: true
                        },
                        proximaRevision: {
                            dateITA: true,
                            required: true
                        },
                        archivo: {
                            required: true,
                            accept: 'pdf'
                        },
                        documento_editable: {
                            required: true
                        }
                    },

                    messages: { // Mensajes personalizados
                        titulo: {
                            required: "Por favor escribe un título",
                            minlength: jQuery.validator.format("Escribe al menos {0} caractéres"),
                            maxlength: jQuery.validator.format("Escribe menos de {0} caractéres"),
                            validateInputmask: "El código no es válido"
                        },
                        codigo: {
                            required: "Por favor escribe un código",
                            minlength: jQuery.validator.format("Escribe al menos {0} caractéres")
                        },
                        version: {
                            min: jQuery.validator.format("Versión debe ser mayor o igual a la actual ({0})"),
                            required: "Campo requerido",
                            validateInputmask: "Una versión válida puede ser una letra o dos dígitos"
                        },
                        fechaElaboracion: {
                            required: "Es necesario que escribas una fecha",
                            dateITA: "Escribe una fecha válida, con el formato dd/mm/aaaa"
                        },
                        ultimaAprobacrion: {
                            required: "Es necesario que escribas una fecha",
                            dateITA: "Escribe una fecha válida, con el formato dd/mm/aaaa"
                        },
                        proximaRevision: {
                            required: "Es necesario que escribas una fecha",
                            dateITA: "Escribe una fecha válida, con el formato dd/mm/aaaa"
                        },
                        archivo: {
                            required: "Es obligatorio cargar un archivo",
                            accept: "Sólo se permiten archivos PDF"
                        },
                        documento_editable: {
                            required: "Es obligatorio cargar un archivo"
                        }
                    },

                    invalidHandler: function (event, validator) { //display error alert on form submit
                        success1.hide();
                        error1.show();
                        Metronic.scrollTo(error1, -200);
                    },

                    errorPlacement: function (error, element) { // render error placement for each input type
                        var icon = $(element).parent('.input-icon').children('i');
                        icon.removeClass('fa-check').addClass("fa-warning");
                        icon.attr("data-original-title", error.text()).tooltip({'container': 'body'});
                    },

                    highlight: function (element) { // hightlight error inputs
                        $(element)
                            .closest('.form-group').addClass('has-error'); // set error class to the control group
                    },

                    unhighlight: function (element) { // revert the change done by hightlight
                        var icon = $(element).parent('.input-icon').children('i');
                        $(element)
                            .closest('.form-group').removeClass('has-error'); // set error class to the control group
                        icon.removeClass("fa-warning");
                    },

                    success: function (label, element) {
                        var icon = $(element).parent('.input-icon').children('i');
                        $(element).closest('.form-group').removeClass('has-error').addClass('has-success'); // set success class to the control group
                        icon.removeClass("fa-warning").addClass("fa-check");
                    },

                    submitHandler: function (form) {
                        success1.show();
                        error1.hide();
                        form.submit();
                    }
                });


        }


    return {
        //main function to initiate the module
        init: function () {
            jQuery.validator.addMethod("validateInputmask", function(value, element, params)
            {
                return params.inputmask("isComplete");
            }, jQuery.validator.format("No tiene el formato válido"));

            handleValidation1();
        }

    };

}();