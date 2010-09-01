function showDescription(img, descriptorID, description) {
	var target = document.getElementById(descriptorID);
	if (target != null) {
		if (target.innerHTML == "") {
			target.innerHTML = "<div style='text-align:left;background-color:#EEEEEE;padding-top:5px;padding-bottom:5px;padding-left:5px;padding-right:5px;line-height:13px !important'>" + description + "</div>";
		} else {
			target.innerHTML = "";
		}
	}
}
function  showCode(obj, url) {
	if (navigator.userAgent.indexOf('Chrome/') > 0) {
	  		if (window.cov_compl_window) {
		   		window.cov_compl_window.close();
		    	window.cov_compl_window = null;
		}
	}
	window.cov_compl_window = window.open(url,'cov_compl_window','width=800, height=500, scrollbars=1');
	if (window.cov_compl_window.focus) {
		window.cov_compl_window.focus();
	}
}