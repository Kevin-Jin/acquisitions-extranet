(function($){
	$.fn.expand = function(op){
		var that = $(this);

		var factory = {
			parent: that.parent()
		};

		if(op){
			for(prop in factory){
				if(!op.hasOwnProperty(prop)){
					op[prop] = factory[prop];
				}
			}
		}
		else{
			op = factory;
		}

		function expand(ele){
			var sh = ele.scrollHeight;
			var tb = parseInt(window.getComputedStyle(ele, null).getPropertyValue('border-top-width'));
			var bb = parseInt(window.getComputedStyle(ele, null).getPropertyValue('border-bottom-width'));

			if(navigator.userAgent.toLowerCase().indexOf('firefox') != -1 ){
				//Firefox why you do this to me.
				tb += parseInt(window.getComputedStyle(ele, null)['paddingTop'].replace('px', ''));
				bb += parseInt(window.getComputedStyle(ele, null)['paddingBottom'].replace('px', ''));
			}

			$(ele).css('height', sh + tb + bb + 'px');
		}

		return this.each(function(){
			$(this).css({
				'box-sizing': 'border-box',
				'-moz-box-sizing': 'border-box', //Once again Firefox.
				'max-height': 'none'
			});

			expand($(this)[0]);

			$(this).bind('keyup input', function(){
				var ct = $('html').height() - $(window).scrollTop();
				expand($(this)[0]);
				$(window).scrollTop($('html').height() - ct);
			});
		});
	}
})(jQuery);