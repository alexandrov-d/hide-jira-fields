;(function($){
    var debug = function(){
        return console.log.apply(console, arguments);
    };

    console.log("HRELLLL")


    AJS.toInit(function(){
        var baseURL = AJS.params.baseURL,
            serviceUrl = baseURL + "/rest/com.adon.nc.hidefields.hide-fields-for-groups/1.0/rules/",
            fieldsToHide = [];
        $.ajax({
            url: serviceUrl + "get-fields-to-hide/" +  JIRA.Issue.getIssueKey(),
        }).success(function(response, status, jxr){
            fieldsToHide = response;
        });

        JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
            var watchedPanels = [
                "details-module",
                "descriptionmodule",
                "peoplemodule",
                "datesmodule"
            ];

            var contextId = $(context).attr("id");
            if (reason === JIRA.CONTENT_ADDED_REASON.panelRefreshed && watchedPanels.indexOf(contextId) > -1) {
                for(var i = 0; i < fieldsToHide.length; i++){
                    var hideField = "#" + fieldsToHide[i];
                    var elem = $(hideField + "-val")
                    if ( !elem.length){
                        elem = $(hideField + "-date")
                    }
                    if ( !elem.length)
                        continue;

                    var parent = elem.parent();
                    if(parent.prop("tagName") === "DD"){
                        parent = parent.parent();
                    }
                    parent.remove();
                }
            }
        });


    });

}(AJS.$));
