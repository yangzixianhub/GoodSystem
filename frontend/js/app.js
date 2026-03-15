var app = (function() {
  var TOKEN_KEY = 'goodsystem_token';

  function getBaseUrl() {
    return ''; 
  }

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function setToken(token) {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  }

  function request(method, url, body) {
    var opts = {
      method: method,
      headers: { 'Content-Type': 'application/json' }
    };
    if (body && (method === 'POST' || method === 'PUT')) {
      opts.body = JSON.stringify(body);
    }
    var token = getToken();
    if (token) opts.headers['Authorization'] = 'Bearer ' + token;
    return fetch((getBaseUrl() + url), opts).then(function(res) {
      var contentType = (res.headers.get('Content-Type') || '').toLowerCase();
      var isJson = contentType.indexOf('application/json') !== -1;
      var textPromise = res.text();
      return textPromise.then(function(text) {
        var data = null;
        if (text && text.trim()) {
          if (isJson) {
            try {
              data = JSON.parse(text);
            } catch (e) {
              throw new Error('服务器返回格式异常');
            }
          } else {
            throw new Error(res.status === 502 ? '后端服务未就绪，请稍后重试' : '服务器返回异常');
          }
        } else {
          if (!res.ok) {
            throw new Error(res.status === 502 ? '后端服务未就绪，请稍后重试' : '请求失败 ' + res.status);
          }
          data = { success: true, data: null };
        }
        if (!res.ok) throw new Error((data && data.msg) || '请求失败');
        return data;
      });
    });
  }

  function login(username, password) {
    return request('POST', '/auth/login', { username: username, password: password });
  }

  function register(username, password, phone) {
    return request('POST', '/auth/register', { username: username, password: password, phone: phone });
  }

  function showMessage(el, text, type) {
    if (!el) return;
    el.textContent = text || '';
    el.hidden = !text;
    el.className = 'message' + (type ? ' ' + type : '');
  }

  return {
    getToken: getToken,
    setToken: setToken,
    login: login,
    register: register,
    request: request,
    showMessage: showMessage
  };
})();
