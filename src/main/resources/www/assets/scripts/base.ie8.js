$(document).ready(function() {
	//we make use of it in base.js
	if (typeof String.prototype.trim !== 'function') {
		String.prototype.trim = function() {
			return this.replace(/^\s+|\s+$/g, ''); 
		};
	}

	//pressing enter doesn't trigger submit if form has less than two input fields
	$(document).keypress(function(e) {
		if (e.keyCode == 13) { //enter key
			var focused = $(document.activeElement);
			//focus is on a form input in .modal
			if (focused.prop('tagName') === 'INPUT' && focused.closest('.dialog').length !== 0) {
				focused.closest('form').find(':submit').trigger('click');
				return false;
			}
		}
	});
});