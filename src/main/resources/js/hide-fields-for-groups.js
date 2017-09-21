;(function($) {

    var debug = function(){
        //return console.log.apply(console, arguments);
    };
    var log = function(){
        return console.log.apply(console, arguments);
    };

    /**
     * Change temporary bg of element
     * @param tag element to animate
     * @param color for animation
     */
    function bgAnimation(tag, color) {
        if (color === "red"){
            color = "#d04437";
        }else if (color === "green"){
            color = "#14892c";
        }
        var defaultBg = tag.css('background-color');
        tag.css({
            backgroundColor: color,
            transition: 'all 0.3s ease-out'
        });
        setTimeout(function() {
            tag.css({
                'background-color': defaultBg,
            });
        }, 300);
    }

    AJS.toInit(function () {

        var baseURL = AJS.params.baseURL,
            serviceUrl = baseURL + "/rest/com.adon.nc.hidefields.hide-fields-for-groups/1.0/rules/";

        $('#hide-fields-for-groups form select').select2();
        $('#hide-fields-for-groups table select:not([id^=cf-options])').select2();

        //Remove Rule
        $(".remove").click(function() {

            var row = $(this).parents('tr');
            var id = $(this).data('ruleid');
            if( id > 0) {
                $.ajax({
                    url: serviceUrl + "remove/" + id,
                })
                .done(function(response, status, xhr){
                    if(xhr.status === 204){
                        row.remove();
                    }
                });
            }
        });

        //Update Rule
        $(".update").click(function() {
            var elem = $(this),
                row = elem.parents('tr'),
                id = elem.data('ruleid');

            if (id < 1) return;
            var ruleObj = {
                id: id,
                title: row.find('[name="ruleTitle' + id + '"]').val(),
                inverse: row.find('#inverse' + id).prop("checked"),
                groups: [],
                fieldsToHide: [],
                jqlFilter: row.find("#jql-filter" + id + " :selected").val()
            }

            $.each( $("#ruleGroups" + id + " :selected"), function(){
                debug($(this))
                ruleObj.groups.push($(this).val());
            });
            $.each( $("#fieldsToHide" + id + " :selected"), function(){
                ruleObj.fieldsToHide.push($(this).val());
            });
            $.ajax({
                url : serviceUrl + "update",
                type: "POST",
                data : ruleObj
            })
            .done(function (response, status, jqXhr) {
                debug(response);
                debug(status);
                if (jqXhr.status == 204){
                    bgAnimation(elem, "green");
                }else{
                    bgAnimation(elem, "red");
                }
            }).error(function(){
                bgAnimation(elem, "red");
            });
        });


    });
})(AJS.$);