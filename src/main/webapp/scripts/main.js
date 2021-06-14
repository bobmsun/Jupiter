(function() {
	/**
	 * Variables
	 */
	var user_id = '1111';
	var user_fullname = 'John';
	var lng = -122.08;             // default lat & lon, if failed to get lat & lon
	var lat = 37.38;

	console.log(user_id);
	
	// this function is the entry point
	function init() {
		// register event listeners
		document.querySelector('#login-form-btn').addEventListener('click', onSessionInvalid);   // no ture/fasle., default bubbling
		document.querySelector('#register-form-btn').addEventListener('click', showRegisterForm);
		document.querySelector('#register-btn').addEventListener('click', register);
		document.querySelector('#login-btn').addEventListener('click', login);
		
		document.querySelector('#nearby-btn').addEventListener('click', loadNearbyItems);
		document.querySelector('#fav-btn').addEventListener('click', loadFavoriteItems);
		document.querySelector('#recommend-btn').addEventListener('click', loadRecommendedItems);

		validateSession();
	}
	
	function validateSession() {
		onSessionInvalid();
		
		// The request parameters
		var url = './login';                       // use same API as login, but below is GET instead of POST
		var req = JSON.stringify({});
		
		// display loading message
		showLoadingMessage("Validing session...");
		
		// make AJAX call
		ajax('GET', url, req,
		// session is still valid
		function(res) {
			var result = JSON.parse(res);
			
			if (result.status === 'OK') {
				onSessionValid(result);
			}
		}, function() {
			console.log('login error')
		});
	}
	
	function onSessionValid(result) {
		user_id = result.user_id;
		user_fullname = result.name;
		
		var loginForm = document.querySelector('#login-form');
		var registerForm = document.querySelector('#register-form');
		var itemNav = document.querySelector('#item-nav');
		var itemList = document.querySelector('#item-list');
		var avatar = document.querySelector('#avatar');
		var welcomeMsg = document.querySelector('#welcome-msg');
		var logoutBtn = document.querySelector('#logout-link');
		
		welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;
		
		showElement(itemNav);
		showElement(itemList);
		showElement(avatar);
		showElement(welcomeMsg);
		showElement(logoutBtn, 'inline-block');
		hideElement(loginForm);
		hideElement(registerForm);
		
		initGeoLocation();
	}
	
	function initGeoLocation() {
		if (navigator.geolocation) {                                // here we are checking if navigator.geolocation exists
			navigator.geolocation.getCurrentPosition(onPositionUpdated,
					onLoadPositionFailed, {
						maximumAge : 60000                         // cache: 6 min
					});
			showLoadingMessage('Retrieving your location...');
		} else {
			onLoadPositionFailed();
		}
	}

	function onPositionUpdated(position) {
		lat = position.coords.latitude;
		lng = position.coords.longitude;
		console.log('lat -> ', lat);
		console.log('lng -> ', lng);
		
		loadNearbyItems();
	}

	function onLoadPositionFailed() {
		console.warn('navigator.geolocation is not available');
		getLocationFromIP();
	}

	function getLocationFromIP() {
		// get location from http://ipinfo.io/json
		var url = 'http://ipinfo.io/json'
		var data = null;

		ajax('GET', url, data, function(res) {
			var result = JSON.parse(res);
			if ('loc' in result) {
				var loc = result.loc.split(',');
				lat = loc[0];
				lng = loc[1];
			} else {
				console.warn('Getting location by IP failed.');
			}
			loadNearbyItems();
		});
	}
	
	
	/**
	 * API #1 Load the nearby items API end point: [GET]
	 * /search?user_id=1111&lat=37.38&lon=-122.08
	 */
	function loadNearbyItems() {
		console.log('loadNearbyItems');
		activeBtn('nearby-btn');

		// The request parameters
		var url = './search';
		var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
		var data = null;

		// display loading message
		showLoadingMessage('Loading nearby items...');

		// make AJAX call
		ajax('GET', url + '?' + params, data,
		// successful callback
		function(res) {
			var items = JSON.parse(res);
			if (!items || items.length === 0) {
				showWarningMessage('No nearby item.');
			} else {
				console.log(items);
				listItems(items);                            // display the data returned by backend, display the jobs
			}
		},
		// failed callback
		function() {
			showErrorMessage('Cannot load nearby items.');
		});
	}
	
	
	/**
	 * A helper function that creates a DOM element <tag options...>
	 * 
	 * @param tag
	 * @param options
	 * @returns {Element}
	 */
	function $create(tag, options) {
		var element = document.createElement(tag);
		for ( var key in options) {
			if (options.hasOwnProperty(key)) {
				element[key] = options[key];
			}
		}
		return element;
	}
	
	
	// Create item list
	/**
	 * List recommendation items base on the data received
	 * 
	 * @param items -
	 *            An array of item JSON objects
	 */
	function listItems(items) {
		var itemList = document.querySelector('#item-list');
		itemList.innerHTML = ''; // clear current results

		for (var i = 0; i < items.length; i++) {
			addItem(itemList, items[i]);
		}
	}
	
	
	function addItem(itemList, item) {                       // itemList is the hook 
		var item_id = item.item_id;

		// create the <li> tag and specify the id and class attributes
		var li = $create('li', {
			id : 'item-' + item_id,
			className : 'item'
		});

		// set the data attribute ex. <li data-item_id="G5vYZ4kxGQVCR"
		// data-favorite="true">
		li.dataset.item_id = item_id;
		li.dataset.favorite = item.favorite;

		// item image
		if (item.image_url) {
			li.appendChild($create('img', {                  // appendChild is old, can just use append
				src : item.image_url
			}));
		} else {
			li.appendChild($create('img', {
				src : 'https://via.placeholder.com/100'
			}));
		}
		// section
		var section = $create('div');

		// title
		var title = $create('a', {
			className : 'item-name',
			href : item.url,
			target : '_blank'
		});
		title.innerHTML = item.name;
		section.appendChild(title);

		// keyword
		var keyword = $create('p', {
			className : 'item-keyword'
		});
		keyword.innerHTML = 'Keyword: ' + item.keywords.join(', ');
		section.appendChild(keyword);

		li.appendChild(section);

		// address
		var address = $create('p', {
			className : 'item-address'
		});

		// ',' => '<br/>', '\"' => ''
		address.innerHTML = item.address.replace(/,/g, '<br/>').replace(/\"/g, '');
		li.appendChild(address);

		// favorite link
		var favLink = $create('p', {
			className : 'fav-link'
		});

		favLink.onclick = function() {
			changeFavoriteItem(item);  // click on the heart to favorite, click will call the changeFavoriteItem, it needs to change the backend DB
		};

		favLink.appendChild($create('i', {
			id : 'fav-icon-' + item_id,
			className : item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
		}));

		li.appendChild(favLink);
		itemList.appendChild(li);
	}


	function changeFavoriteItem(item) {
		// check whether this item has been visited or not
		var li = document.querySelector('#item-' + item.item_id);
		var favIcon = document.querySelector('#fav-icon-' + item.item_id);
		var favorite = !(li.dataset.favorite === 'true');

		// request parameters
		var url = './history';
		var req = JSON.stringify({
			user_id : user_id,
			favorite : item
		});
		var method = favorite ? 'POST' : 'DELETE';

		ajax(method, url, req,
		// successful callback
		function(res) {
			var result = JSON.parse(res);
			if (result.status === 'OK' || result.result === 'SUCCESS') {
				li.dataset.favorite = favorite;
				favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
			}
		}, 
		//error
		function(){
			console.log('change favorite failed!')
		}
		);
	}
	
	
	function loadFavoriteItems() {
		activeBtn('fav-btn');

		// request parameters
		var url = './history';
		var params = 'user_id=' + user_id;
		var req = JSON.stringify({});

		// display loading message
		showLoadingMessage('Loading favorite items...');

		// make AJAX call
		ajax('GET', url + '?' + params, req, function(res) {
			var items = JSON.parse(res);
			if (!items || items.length === 0) {
				showWarningMessage('No favorite item.');
			} else {
				listItems(items);
			}
		}, function() {
			showErrorMessage('Cannot load favorite items.');
		});
	}
	
	
	function loadRecommendedItems() {
		activeBtn('recommend-btn');

		// request parameters
		var url = './recommendation' + '?' + 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
		var data = null;

		// display loading message
		showLoadingMessage('Loading recommended items...');

		// make AJAX call
		ajax('GET', url, data,
				// successful callback
				function(res) {
					var items = JSON.parse(res);
					if (!items || items.length === 0) {
						showWarningMessage('No recommended item. Make sure you have favorites.');
					} else {
						listItems(items);
					}
				},
				// failed callback
				function() {
					showErrorMessage('Cannot load recommended items.');
				});
	}

	
	
	/**
	 * A helper function that makes a navigation button active
	 * 
	 * @param btnId -
	 *            The id of the navigation button
	 */
	function activeBtn(btnId) {
		var btns = document.querySelectorAll('.main-nav-btn');

		// deactivate all navigation buttons
		for (var i = 0; i < btns.length; i++) {
			btns[i].className = btns[i].className.replace(/\bactive\b/, '');
		}

		// active the one that has id = btnId
		var btn = document.querySelector('#' + btnId);
		btn.className += ' active';
	}
	
	function showLoadingMessage(msg) {
		var itemList = document.querySelector('#item-list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> '
				+ msg + '</p>';
	}

	function showWarningMessage(msg) {
		var itemList = document.querySelector('#item-list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> '
				+ msg + '</p>';
	}

	function showErrorMessage(msg) {
		var itemList = document.querySelector('#item-list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> '
				+ msg + '</p>';
	}

	
	
	// Only show login form, hide the rest
	function onSessionInvalid() {
		var loginForm = document.querySelector('#login-form');
		var registerForm = document.querySelector('#register-form');
		var itemNav = document.querySelector('#item-nav');
		var itemList = document.querySelector('#item-list');
		var avatar = document.querySelector('#avatar');
		var welcomeMsg = document.querySelector('#welcome-msg');
		var logoutBtn = document.querySelector('#logout-link');

		hideElement(itemNav);
		hideElement(itemList);
		hideElement(avatar);
		hideElement(logoutBtn);
		hideElement(welcomeMsg);
		hideElement(registerForm);

		clearLoginError();
		showElement(loginForm);
	}
	
	
	// Only show register form, hide other forms
	function showRegisterForm() {
		var loginForm = document.querySelector('#login-form');
		var registerForm = document.querySelector('#register-form');
		var itemNav = document.querySelector('#item-nav');
		var itemList = document.querySelector('#item-list');
		var avatar = document.querySelector('#avatar');
		var welcomeMsg = document.querySelector('#welcome-msg');
		var logoutBtn = document.querySelector('#logout-link');

		hideElement(itemNav);
		hideElement(itemList);
		hideElement(avatar);
		hideElement(logoutBtn);
		hideElement(welcomeMsg);
		hideElement(loginForm);

		clearRegisterResult();
		showElement(registerForm);
	}
	
	function clearRegisterResult() {
		document.querySelector('#register-result').innerHTML = '';
	}


	

	function hideElement(element) {
		element.style.display = 'none';
	}
	
	function clearLoginError() {
		document.querySelector('#login-error').innerHTML = '';
	}
	
	function showElement(element, style) {
		// if style is undefined, it is false
		var displayStyle = style ? style : 'block';
		element.style.display = displayStyle;
	}
	
	
	// register
	function register() {
		var username = document.querySelector('#register-username').value;    // get input value through DOM operation;
		var password = document.querySelector('#register-password').value;
		var firstName = document.querySelector('#register-first-name').value;
		var lastName = document.querySelector('#register-last-name').value;

		if (username === "" || password == "" || firstName === "" || lastName === "") {
			showRegisterResult('Please fill in all fields');
			return;
		}

		if (username.match(/^[a-z0-9_]+$/) === null) {
			showRegisterResult('Invalid username');
			return;
		}
		password = md5(username + md5(password));

		// The request parameters
		var url = './register';
		var req = JSON.stringify({               // the data we pass to the Ajax call must be a string
			user_id : username,
			password : password,
			first_name : firstName,
			last_name : lastName,
		});

		
		ajax('POST', url, req,
		// successful callback
		function(res) {
			var result = JSON.parse(res);
			// successfully logged in
			if (result.status === 'OK') {
				showRegisterResult('Succesfully registered');
			} else {
				showRegisterResult('User already existed');
			}
		},
		// error
		function() {
			showRegisterResult('Failed to register');
		});
	}

	function showRegisterResult(registerMessage) {
		document.querySelector('#register-result').innerHTML = registerMessage;
	}

	function clearRegisterResult() {
		document.querySelector('#register-result').innerHTML = '';
	}
	
	
	// Login
	function login() {
		var username = document.querySelector('#username').value;
		var password = document.querySelector('#password').value;
		password = md5(username + md5(password));
		
		// The request parameters
		var url = "./login";                         // url is backend interface
		var req = JSON.stringify({
			user_id : username,
			password : password,
		});
		
		ajax('POST', url, req,
		// successful callback
		function(res) {
			var result = JSON.parse(res);
			// successfully logged in
			if (result.status === 'OK') {
				console.log('login successfully!')
				onSessionValid(result);
			}
		},
		// error
		function() {
			showLoginError();
		});
	}
	
	function showLoginError() {
		document.querySelector('#login-error').innerHTML = 'Invalid username or password';
	}
	
	
	
	
	function ajax(method, url, data, successCallback, errorCallback) {
		var xhr = new XMLHttpRequest();

		xhr.open(method, url, true);

		xhr.onload = function() {
			if (xhr.status === 200) {
				successCallback(xhr.responseText);
			} else {
				errorCallback();
			}
		};

		xhr.onerror = function() {
			console.error("The request couldn't be completed.");
			errorCallback();
		};

		if (data === null) {
			xhr.send();
		} else {
			xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
			xhr.send(data);
		}
	}
	
	
	
	
	
	
	
	
	
	init();


})();