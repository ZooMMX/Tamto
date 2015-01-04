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
                    ignore: "",  // validate all fields including form hidden input
                    rules: {
                        descripcion: {
                            minlength: 3,
                            required: true
                        },
                        nombreSap: {
                            minlength: 3,
                            required: false
                        },
                        universalCode: {
                            maxlength: 20,
                            required: false
                        },
                        workOrderDate: {
                            dateITA: true,
                            required: true
                        },
                        workOrderNo: {
                            number: true,
                            required: true
                        },
                        cliente: {
                            minlength: 3,
                            required: false
                        }
                    },

                    messages: { // Mensajes personalizados
                        descripcion: {
                            required: "Por favor escribe alguna descripción",
                            minlength: jQuery.validator.format("Escribe al menos {0} caractéres")
                        },
                        nombreSap: {
                            required: "Por favor escribe alguna descripción",
                            minlength: jQuery.validator.format("Escribe al menos {0} caractéres")
                        },
                        universalCode: {
                            maxlength: jQuery.validator.format("Pasaste el límite de {0} caractéres")
                        },
                        workOrderDate: {
                            required: "Es necesario que escribas una fecha",
                            dateITA: "Escribe una fecha válida, con el formato dd/mm/aaaa"
                        },
                        workOrderNo: {
                            required: "Es obligatorio escribir algún número de orden de trabajo",
                            number: "Sólo se permiten números"
                        },
                        cliente: {
                            required: "Es necesario especificar un cliente",
                            minlength: jQuery.validator.format("Escribe al menos {0} caractéres")
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
            handleValidation1();
        }

    };

}();