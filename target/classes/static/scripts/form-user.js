var FormUserScripts = function () {

    var handleValidation1 = function() {
            // for more info visit the official plugin documentation:
                // http://docs.jquery.com/Plugins/Validation

                var form1 = $('#formi');
                var error1 = $('.alert-danger', form1);
                var success1 = $('.alert-success', form1);

                var validobj = form1.validate({
                    errorElement: 'span', //default input error message container
                    errorClass: 'help-block help-block-error', // default input error message class
                    focusInvalid: false, // do not focus the last invalid input
                    ignore: "",  // validate all fields including form hidden input
                    rules: {
                        username: {
                            minlength: 3,
                            remote: "/usuarioCheckUsername",
                            required: true
                        },
                        fullName: {
                            minlength: 7,
                            required: true
                        },
                        password: {
                            required: true,
                            pass: true
                        },
                        roles: {
                            required: true
                        }
                    },

                    messages: { // Mensajes personalizados
                        username: {
                            required: "Por favor elige un nombre usuario para iniciar sesión",
                            minlength: jQuery.validator.format("Escribe al menos {0} caractéres"),
                            remote: "Nombre de usuario no disponible, elige otro"
                        },
                        fullName: {
                            required: "Escribe el nombre y apellidos del usuario",
                            minlength: jQuery.validator.format("Escribe al menos {0} caractéres")
                        },
                        password: {
                            required: "Es necesario que escribas una contraseña",
                            pass: "Tu password debe contener al menos 6 caracteres y contener al menos un número y una letra."
                        },
                        roles: {
                            required: "Es obligatorio agregar al menos un rol"
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
                        $(element)
                            .closest('.form-group').removeClass('has-error'); // set error class to the control group
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

                //Validar lista de roles (o cualquier select2) cuando haya cambios
                $(document).on('change', '.select2-offscreen', function () {
                    if (!$.isEmptyObject(validobj.submitted)) {
                        validobj.form();
                    }
                });
        }
    var addPasswordValidator = function () {
        jQuery.validator.addMethod("pass", function (value, element) {
            var result = this.optional(element) || value.length >= 6 && /\d/.test(value) && /[a-z]/i.test(value);

            return result;
        }, "Tu password debe contener al menos 6 caracteres y contener al menos un número y una letra.");
    }

    return {
        //main function to initiate the module
        init: function () {
            addPasswordValidator();
            handleValidation1();
        }

    };

}();