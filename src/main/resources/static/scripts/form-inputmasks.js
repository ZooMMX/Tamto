var FormInputMasks = function () {

    var handleInputMasks = function () {

        $(".date-picker").inputmask("d/m/y", {
            "placeholder": "dd/mm/aaaa",
            onUnMask: function(maskedValue, unmaskedValue) {
                    return maskedValue;
               }
        }); //multi-char placeholder
    }

    var handleDatePickers = function () {

            if (jQuery().datepicker) {
                $('.date-picker').datepicker({
                    rtl: Metronic.isRTL(),
                    orientation: "left",
                    autoclose: true,
                    format: 'dd/mm/yyyy',
                    language: 'es'
                });
            }
        }

    return {
            //main function to initiate the module
            init: function () {

                handleInputMasks();
                handleDatePickers();

            }
        };

}();