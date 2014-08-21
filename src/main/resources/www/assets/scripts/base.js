function queryStringFirstValue(url, key) {
	var startIndex = url.search(new RegExp('[\?&]' + key, "i"));
	if (startIndex === -1)
		return null;
	startIndex += key.length + 1;
	var endIndex = url.indexOf(startIndex, '&');
	if (endIndex === -1)
		endIndex = url.length;
	if (startIndex >= endIndex || url[startIndex] !== '=')
		return '';
	return url.substring(startIndex + 1, endIndex);
}

function appendToPath(url, directory) {
	var queryStringDelimit = url.indexOf('?');
	if (queryStringDelimit == -1)
		queryStringDelimit = url.length;
	return url.substring(0, queryStringDelimit) + directory + url.substring(queryStringDelimit);
}

function encodeXWwwFormUrlencoded(value) {
	return encodeURIComponent(value).replace(/!/g, '%21').replace(/'/g, '%27').replace(/\(/g, '%28').replace(/\)/g, '%29').replace(/\*/g, '%2A');
}

function setCookie(key, value) {
	var expires = new Date();
	expires.setTime(expires.getTime() + (15 * 24 * 60 * 60 * 1000));
	document.cookie = key + '=' + encodeXWwwFormUrlencoded(value) + ';expires=' + expires.toUTCString() + ';path=/';
}

function getCookie(key) {
	var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
	return keyValue ? decodeURIComponent(keyValue[2]) : null;
}

function discardCookie(key) {
	document.cookie = key + '=;Max-Age=0';
}

function setModalContents(data, onComplete) {
	var close = data.trim().length === 0;
	if (!close) {
		$('.dialog').replaceWith($(data).removeClass('visible'));
		$('.dialog').height(); //trigger reflow
		$('.dialog, .dialog-overlay').addClass('visible'); //trigger animation

		//disable scroll when on desktop layout
		if ($('body').css('overflow') !== 'hidden') {
			$('#main').css('top', $('#main').offset().top - $(document).scrollTop() + 'px');
			$('body').addClass('disablescroll');
			$('.tipsy').css('display', 'none');
		}
	} else {
		$('.dialog, .dialog-overlay').removeClass('visible');

		//reenable scroll
		if ($('body').hasClass('disablescroll')) {
			var oldTop = $('#main').offset().top;
			$('body').removeClass('disablescroll').add('html').scrollTop($('#main').css('top', '').offset().top - oldTop);
			$('.tipsy').css('display', '');
		}
	}
	if (onComplete)
		onComplete();
}

function updateModalContents(targetUrl, onComplete, data, type) {
	if (!type)
		type = 'GET';
	$.ajax({ cache: false, type: type, url: targetUrl, dataType: 'html', data: data }).done(function(data) {
		setModalContents(data, onComplete);
	});
}

function submitModalForm(inputSubmit, path, onComplete) {
	var form = inputSubmit.closest('form');
	inputSubmit.attr('disabled', 'true').attr('value', 'Please wait...');
	updateModalContents(path, onComplete, form.serialize() + '&' + encodeXWwwFormUrlencoded(inputSubmit.attr('name')) + '=' + encodeXWwwFormUrlencoded(inputSubmit.attr('value')), form.attr('method'));
}

function selectModalLink(a, path, onComplete) {
	var linkPath = a.attr('href');
	updateModalContents(path, onComplete, linkPath.substring(linkPath.indexOf('?') + 1));
}

function enhanceDraggableButtons() {
	//some functions needed by sidebar and searchbar stuff
	var translateLeft, translateRight;
	if (typeof window.Modernizr === 'undefined') {
		window.Modernizr = new Object();
		window.Modernizr.csstransitions = false;
	}
	window.Modernizr.cssprefixed = function(str) {  
		return this.prefixed(str).replace(/([A-Z])/g, function(str, m1) {
			return '-' + m1.toLowerCase();
		}).replace(/^vp-/,'-vp-');
	};
	if (window.Modernizr.csstransitions) {
		translateLeft = function(selector, newLeft, milliseconds) {
			selector.css(window.Modernizr.cssprefixed('transition'), milliseconds ? ('left ' + milliseconds / 1000 + 's ease') : 'none').css('left', newLeft + 'px');
		};
		translateRight = function(selector, newRight, milliseconds) {
			selector.css(window.Modernizr.cssprefixed('transition'), milliseconds ? ('right ' + milliseconds / 1000 + 's ease') : 'none').css('right', newRight + 'px');
		};
	} else {
		translateLeft = function(selector, newLeft, milliseconds) {
			if (milliseconds) {
				selector.stop(true, false).animate({
					left: newLeft
				}, milliseconds, 'swing');
			} else {
				selector.css('left', newLeft + 'px');
			}
		};
		translateRight = function(selector, newRight, milliseconds) {
			if (milliseconds) {
				selector.stop(true, false).animate({
					right: newRight
				}, milliseconds, 'swing');
			} else {
				selector.css('right', newRight + 'px');
			}
		};
	}

	function enhanceDraggableButton(button, eventTarget, widthFn, pastOpenThresholdFn, positionFn, moveButton, translateAnimation, pastVelThresholdFn) {
		var barWasOpen, barStartX, barVelX, barLastT;
		var width = widthFn();
		var down = false;
		barWasOpen = pastOpenThresholdFn(true, $(button + '~.offsetable').offset().left, width);
		$(button + '~.unenhanced,' + button).removeClass('unenhanced');
		$(document).on('mousedown.' + eventTarget + ' touchstart.' + eventTarget, button, function(e) {
			e.preventDefault();
			down = true;
			$(document).on('mousemove.' + eventTarget + ' touchmove.' + eventTarget, function(e) {
				if (e.originalEvent.touches) //mobile
					e = e.originalEvent.touches[0];
				else //desktop
					e.preventDefault();

				width = widthFn();
				var nowT = new Date().getTime();
				if (!barStartX)
					barStartX = e.pageX - $(button).offset().left;
				else
					barVelX = (e.pageX - $(button).offset().left - barStartX) / (nowT - barLastT);
				barLastT = nowT;
				if (moveButton || !barWasOpen)
					translateAnimation($(button + '~.offsetable' + (moveButton ? (',' + button) : '')), Math.max(0, Math.min(width, positionFn(e.pageX - barStartX, width))));
			});
		}).on('mouseup.' + eventTarget + ' touchend.' + eventTarget, function(e) {
			if (!down)
				return;

			e.preventDefault();
			$(document).off('mousemove.' + eventTarget + ' touchmove.' + eventTarget);
			width = widthFn();

			var dragged = (barStartX && barVelX);
			// !dragged implies a click was made
			if (!dragged && !barWasOpen || dragged && !barWasOpen === (pastVelThresholdFn(!barWasOpen, barVelX) || pastOpenThresholdFn(!barWasOpen, $(button + '~.offsetable').offset().left, width))) {
				translateAnimation($(button + '~.offsetable'), width, 100);
				if (moveButton)
					translateAnimation($(button), width, 100);
				else
					translateAnimation($(button), 0, 0);
				barWasOpen = true;
			} else {
				translateAnimation($(button + '~.offsetable'), 0, 100);
				if (moveButton)
					translateAnimation($(button), 0, 100);
				else
					translateAnimation($(button), 0, 0);
				barWasOpen = false;
			}
			barStartX = barVelX = null;
			down = false;
		});

		$(window).resize(function() {
			translateAnimation($(button + '~.offsetable,' + button), 0, 0);
			barWasOpen = false;
		});
	};

	enhanceDraggableButton('.showsidebar', 'enhancedsidebar', function() { return $('.nav').width(); }, function(checkOpen, left, width) { return checkOpen === (left > width / 2); }, function(left, width) { return left; }, false, translateLeft, function(checkOpen, barVelX) { return checkOpen && barVelX > 0.25 || !checkOpen && barVelX < -0.25; });
	enhanceDraggableButton('#searchicon', 'enhancedsearchbar', function() { return $('#topbar').width(); }, function(checkOpen, left, width) { return checkOpen === (left < -width / 2); }, function(left, width) { return width - left; }, true, translateRight, function(checkOpen, barVelX) { return checkOpen && barVelX < -0.25 || !checkOpen && barVelX > 0.25; });
}

var overrideAskLoc = true;
var currentPromo;
var locCookie;

function replaceStaticMapWithDynamicMap() {
	var location = getCookie('loc');
	var element = $('#map');
	if (!location || !element.length)
		return;

	element.css('background', 'none');
	var coords = location.substring(0, location.indexOf('(')).split(',');
	var latlon = new google.maps.LatLng(coords[0], coords[1]);
	var map = new google.maps.Map(element[0], {
		center: latlon,
		zoom: 14,
		mapTypeId: google.maps.MapTypeId.ROADMAP,
		mapTypeControl: false,
		navigationControlOptions: {
			style: google.maps.NavigationControlStyle.SMALL
		}
	});
	var marker = new google.maps.Marker({ position: latlon, map: map });
}

function showAskLoc(town, state, zip, lat, lng, error) {
	var params = 'askloc=';
	if (town)
		params += '&town=' + encodeXWwwFormUrlencoded(town);
	if (state)
		params += '&state=' + encodeXWwwFormUrlencoded(state);
	if (zip)
		params += '&zip=' + encodeXWwwFormUrlencoded(zip);
	if (lat)
		params += '&lat=' + encodeXWwwFormUrlencoded(lat);
	if (lng)
		params += '&lng=' + encodeXWwwFormUrlencoded(lng);
	if (error)
		params += '&error=' + encodeXWwwFormUrlencoded(error);
	updateModalContents(LOCATION_MODAL_CONTENTS_PATH, overrideLocationModalContents, params);
}

function findLocation() {
	setModalContents('<div class="dialog visible locationwizard" id="autoloc">'
			+ '<a href="?" class="dialog-close">x</a>'
			+ '<p>Automatically detecting your location...</p>'
			+ '<p>If you do not wish to automatically detect your location,<br>you may manually enter your ZIP code <a href="#" id="askloc">here</a>.</p>'
			+ '</div>');
	$('#askloc').on('click', function() {
		//TODO: this doesn't prevent confirmloc from showing up
		overrideAskLoc = false;
		showAskLoc();
		return false;
	});

	navigator.geolocation.getCurrentPosition(locationFound, findLocationFailed);
}

function locationFound(position) {
	//if user selects no to confirmloc after auto detection, allow user to
	//manually enter a zip code by disabling askloc override
	overrideAskLoc = false;
	showAskLoc(null, null, null, position.coords.latitude, position.coords.longitude);
}

function findLocationFailed(error) {
	switch (error.code) {
		case error.PERMISSION_DENIED:
			overrideAskLoc = false;
			showAskLoc(null, null, null, null, null, 'You denied the request to automatically find your location.');
			break;
		case error.POSITION_UNAVAILABLE:
			overrideAskLoc = false;
			showAskLoc(null, null, null, null, null, 'Location information is unavailable.');
			break;
		case error.TIMEOUT:
			overrideAskLoc = false;
			showAskLoc(null, null, null, null, null, 'It took too long to automatically find your location.');
			break;
		case error.UNKNOWN_ERROR:
			overrideAskLoc = false;
			showAskLoc(null, null, null, null, null, 'An unknown error occurred while automatically finding your location.');
			break;
	}
}

function overrideLocationModalContents(initialLoad) {
	var currentLocCookie = getCookie('loc');
	if (!$('.dialog').hasClass('visible')) {
		//wizard is closed
		if (currentLocCookie != locCookie) {
			if (currentLocCookie) {
				$('.locationdesc').text(currentLocCookie.substring(currentLocCookie.indexOf('(') + 1, currentLocCookie.length - 1));
				//location is set and the location was changed
			} else {
				//location is unset
				$('.locationdesc').text('Your Location');
			}
			if (typeof onLocChanged === 'function' && !initialLoad)
				onLocChanged();
			locCookie = currentLocCookie;
		}
		return;
	}

	switch ($('.dialog').attr('id')) {
		case 'locinfo':
			replaceStaticMapWithDynamicMap();
			break;
		case 'askloc':
			//if browser supports HTML5 geolocation, attempt it first before
			//asking user to enter ZIP code
			if (navigator.geolocation && overrideAskLoc)
				findLocation();
			break;
	}
}

function enhanceRemainingCharactersCounter() {
	$('.charcounter').data('max', parseInt($('.charcounter').text()));
	$('.dialog textarea').on('input propertychange', function(e) {
		var textarea = $(this);
		$('.charcounter').each(function() {
			var counter = $(this);
			counter.text((counter.data('max') - textarea.val().length) + ' more');
		});
	});
}

function overridePromotionModalContents(initialLoad) {
	if (!$('.dialog').hasClass('visible'))
		return;

	switch ($('.dialog').attr('id')) {
		case 'promotemessage':
			enhanceRemainingCharactersCounter();
			break;
	}
}

function enhanceModalDialogs() {
	//Location modal wizard.
	$(document).on('click', '#location', function() {
		overrideAskLoc = true;
	}).on('click', '#location, .locationwizard a:not(.dialog-close, .dialogexternal)', function(e){
		e.preventDefault();

		selectModalLink($(this), LOCATION_MODAL_CONTENTS_PATH, overrideLocationModalContents);
	}).on('click', '.locationwizard form input:submit', function(e) {
		e.preventDefault();

		submitModalForm($(this), LOCATION_MODAL_CONTENTS_PATH, overrideLocationModalContents);
	});

	//Promote post wizard.
	$(document).on('click', '.promotepost', function() {
		currentPromo = SHARE_MODAL_CONTENTS_PATH.replace(SHARE_MODAL_PROMO_ID_REGEX, queryStringFirstValue($(this).prop('href'), 'promote'));
	}).on('click', '.promotepost, .promotionwizard a:not(.dialog-close, .dialogexternal)', function(e){
		e.preventDefault();

		selectModalLink($(this), currentPromo, overridePromotionModalContents);
	}).on('click', '.promotionwizard form input:submit', function(e) {
		e.preventDefault();

		submitModalForm($(this), currentPromo, overridePromotionModalContents);
	});

	//Generic dialog close.
	$(document).on('click', '.dialog-overlay, .dialog-close', function(e) {
		e.preventDefault();

		setModalContents('', $('.locationwizard').length ? overrideLocationModalContents : overridePromotionModalContents);
	});

	overrideLocationModalContents(true);
	overridePromotionModalContents(true);
}

$(document).ready(function(){
	//Tooltips
	$('*').tipsy({gravity: 'n'});

	enhanceDraggableButtons();
	enhanceModalDialogs();
});