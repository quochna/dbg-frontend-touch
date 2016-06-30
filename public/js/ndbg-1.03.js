var _n_px = "udbg://";

function urldecode(str) {
    return decodeURIComponent((str + '').replace(/\+/g, '%20'))
}

function closePayment() {
    callbackWhenBacked()
}

function callbackWhenBacked() {
    callbackPaymentHelper(_n_state, _n_result, _n_tranxid, _n_apptranxid, _n_netamount, _n_pmc, _n_grossamount)
}

function callbackPaymentHelper(state, result, tranxid, apptranxid, netamount, channel, grossamount) {
    if (state == "billed") 
    {
        if (result <= 0) 
	{
            var _url = "";
            if (_n_url_redirect == undefined || _n_url_redirect == "" || _n_url_redirect == _n_px) {
                _url = _n_px + "failed?"
            } else {
                _url = _n_url_redirect + "?state=failed&"
            }     
                        
            var error_code = (_n_error_code != undefined ? _n_error_code : 0);
            var error_msg = (_n_error_msg != undefined ? _n_error_msg : "");
            _url += "error_code=" + error_code + "&error_msg=" + urldecode(error_msg) + "&tranxid=" + tranxid + "&apptranxid=" + apptranxid;
            if (_n_platform != undefined && _n_platform == "wp") 
            {
                _url = "http://" + _url;
            }
             _url += "&channel=" + channel;
            action(_url);
            return
        } 
	else if (result == 2 || result == 3||result == 4||result == 5||result == 6||result == 7)
	{
		/*  WAIT_FOR_CHARGE(7)
		    COMPLETE_SUBMIT_TRANS(6)
		    INIT(5)
		    IN_VALIDATION_QUEUE(4)
		    IN_NOTIFIER_QUEUE(3)
		    IN_NOTIFIER_QUEUE_RETRY(2)
		*/		
		var _url = "";
		if(_n_url_redirect == undefined || _n_url_redirect == "" || _n_url_redirect == _n_px) {
			 _url = _n_px + "waiting?";
		}
		else {
			_url = _n_url_redirect + "?state=waiting&";
		}
                            
		var error_code = (_n_error_code != undefined ? _n_error_code : 0);
		var error_msg = (_n_error_msg != undefined ? _n_error_msg : "");			
		_url += "error_code=" + error_code + "&error_msg=" + urldecode(error_msg) + "&tranxid=" + tranxid + 					"&apptranxid=" + apptranxid;
		if(_n_platform != undefined && _n_platform == "wp") {
			_url = "http://" + _url;
		}
               _url += "&channel=" + channel;
		action(_url);
		return;		
	}
	else 
	{
            if (_n_url_redirect == undefined || _n_url_redirect == "" || _n_url_redirect == _n_px) {
                _url = _n_px + "success?"
            } else {
                _url = _n_url_redirect + "?state=success&"
            }
         
            _url += "tranxid=" + tranxid + "&apptranxid=" + apptranxid + "&netamount=" + netamount + "&channel=" + channel + 				"&grossamount=" + grossamount;
            if (_n_platform != undefined && _n_platform == "wp") {
                _url = "http://" + _url
            }
          
            action(_url);
            return
        }
    } else {
        if (_n_url_redirect == undefined || _n_url_redirect == "" || _n_url_redirect == _n_px) {
            _url = _n_px + "cancel?"
        } else {
            _url = _n_url_redirect + "?state=cancel&"
        }
        _url += "tranxid=" + tranxid;
    
        if (_n_platform != undefined && _n_platform == "wp") {
            _url = "http://" + _url
        }
       _url += "&channel=" + channel;
        action(_url);
        return
    }
}

function action(u) {
    if (_n_db != undefined && _n_db == true) {
        alert("url=" + u)
    } else {
        window.location.href = u
    }
}
