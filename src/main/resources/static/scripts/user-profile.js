var Profile = function () {

	var handleProfile = function () {

         $('#form_profile').validate({
	            errorElement: 'span', //default input error message container
	            errorClass: 'help-block', // default input error message class
	            focusInvalid: false, // do not focus the last invalid input
	            ignore: "",
	            rules: {

	                fullname: {
                        minlength: 7,
	                    required: true
	                },

	                username: {
	                    required: true
	                }
	            },

	            messages: {

	            },

	            invalidHandler: function (event, validator) { //display error alert on form submit

	            },

	            highlight: function (element) { // hightlight error inputs
	                $(element)
	                    .closest('.form-group').addClass('has-error'); // set error class to the control group
	            },

	            success: function (label) {
	                label.closest('.form-group').removeClass('has-error');
	                label.remove();
	            },

	            errorPlacement: function (error, element) {
	                if (element.attr("name") == "tnc") { // insert checkbox errors after the container
	                    error.insertAfter($('#register_tnc_error'));
	                } else if (element.closest('.input-icon').size() === 1) {
	                    error.insertAfter(element.closest('.input-icon'));
	                } else {
	                	error.insertAfter(element);
	                }
	            },

	            submitHandler: function (form) {
	                form.submit();
	            }
	        });

			$('#form_profile input').keypress(function (e) {
	            if (e.which == 13) {
	                if ($('#form_profile').validate().form()) {
	                    $('#form_profile').submit();
	                }
	                return false;
	            }
	        });
	}

    var handlePasswords = function () {


             $('#form_update_password').validate({
    	            errorElement: 'span', //default input error message container
    	            errorClass: 'help-block', // default input error message class
    	            focusInvalid: false, // do not focus the last invalid input
    	            ignore: "",
    	            rules: {
                        oldPass: {
                            required: true
                        },
    	                newPass: {
    	                    required: true,
                            pass: true
    	                },
    	                reNewPass: {
                            required: true,
    	                    equalTo: "#newPass"
    	                }
    	            },

    	            messages: {
                        reNewPass: {
                            equalTo: $("#reNewPass").data("wrong-pass-confirmation")
                        }
    	            },

    	            invalidHandler: function (event, validator) { //display error alert on form submit

    	            },

    	            highlight: function (element) { // hightlight error inputs
    	                $(element)
    	                    .closest('.form-group').addClass('has-error'); // set error class to the control group
    	            },

    	            success: function (label) {
    	                label.closest('.form-group').removeClass('has-error');
    	                label.remove();
    	            },

    	            errorPlacement: function (error, element) {
    	                if (element.attr("name") == "tnc") { // insert checkbox errors after the container
    	                    error.insertAfter($('#register_tnc_error'));
    	                } else if (element.closest('.input-icon').size() === 1) {
    	                    error.insertAfter(element.closest('.input-icon'));
    	                } else {
    	                	error.insertAfter(element);
    	                }
    	            },

    	            submitHandler: function (form) {
    	                form.submit();
    	            }
    	        });

    			$('#form_update_password input').keypress(function (e) {
    	            if (e.which == 13) {
    	                if ($('#form_update_password').validate().form()) {
    	                    $('#form_update_password').submit();
    	                }
    	                return false;
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
            handleProfile();
            handlePasswords();
        }

    };

}();