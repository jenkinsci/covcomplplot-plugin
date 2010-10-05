YAHOO.util.Event.onDOMReady(function() {
	resizeCloverBar();
});

function resizeCloverBar() {
	if (navigator.appVersion.indexOf(("MSIE")) > -1) {
		var barEmpty = document.getElementsByClassName('barEmpty');
		var barNegative = document.getElementsByClassName('barNegative');
		if (barEmpty.length == 1) {
			document.getElementsByClassName("barEmpty")[0].style.width = "500";
		}
		if (barNegative.length == 1) {
			document.getElementsByClassName("barNegative")[0].style.width = "500";
		}
	}
}