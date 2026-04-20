var app = (function() {
  var TOKEN_KEY = 'goodsystem_token';
  var USER_ID_KEY = 'goodsystem_user_id';
  var USERNAME_KEY = 'goodsystem_username';

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

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(USERNAME_KEY);
  }

  function setSession(token, userId, username) {
    setToken(token);
    if (userId != null && userId !== undefined) {
      localStorage.setItem(USER_ID_KEY, String(userId));
    }
    if (username) localStorage.setItem(USERNAME_KEY, username);
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
              var parseErr = new Error('服务器返回格式异常');
              parseErr.status = res.status;
              throw parseErr;
            }
          } else {
            var err502 = new Error(res.status === 502 ? '后端服务未就绪，请稍后重试' : '服务器返回异常');
            err502.status = res.status;
            throw err502;
          }
        } else {
          if (!res.ok) {
            var errEmpty = new Error(res.status === 502 ? '后端服务未就绪，请稍后重试' : '请求失败 ' + res.status);
            errEmpty.status = res.status;
            throw errEmpty;
          }
          data = { success: true, data: null };
        }
        if (!res.ok) {
          var err = new Error((data && data.msg) || '请求失败');
          err.status = res.status;
          err.code = data && data.code;
          throw err;
        }
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

  function fetchMe() {
    return request('GET', '/auth/me');
  }

  function ensureUserId() {
    var raw = localStorage.getItem(USER_ID_KEY);
    if (raw) {
      var n = Number(raw);
      if (!isNaN(n)) return Promise.resolve(n);
    }
    return fetchMe().then(function(res) {
      if (res.success && res.data && res.data.id != null) {
        localStorage.setItem(USER_ID_KEY, String(res.data.id));
        if (res.data.username) localStorage.setItem(USERNAME_KEY, res.data.username);
        return res.data.id;
      }
      throw new Error(res.msg || '无法获取用户ID');
    });
  }

  function productSearch(keyword) {
    var q = keyword ? ('?keyword=' + encodeURIComponent(keyword)) : '';
    return request('GET', '/product/search' + q);
  }

  function getProductDetail(id) {
    return request('GET', '/product/detail/' + encodeURIComponent(id));
  }

  function placeOrder(productId) {
    return request('POST', '/seckill/order', { productId: Number(productId) });
  }

  function listOrders(userId) {
    return request('GET', '/seckill/orders/user/' + encodeURIComponent(userId));
  }

  function getOrder(orderId) {
    return request('GET', '/seckill/order/' + encodeURIComponent(orderId));
  }

  function payOrder(orderId) {
    return request('POST', '/seckill/pay', { orderId: Number(orderId) });
  }

  function showMessage(el, text, type) {
    if (!el) return;
    el.textContent = text || '';
    el.hidden = !text;
    el.className = 'message' + (type ? ' ' + type : '');
  }

  function escapeHtml(s) {
    if (s == null) return '';
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function formatMoney(v) {
    if (v == null || v === '') return '—';
    var n = Number(v);
    if (isNaN(n)) return String(v);
    return n.toFixed(2);
  }

  function renderNav(userEl, loginEl, logoutEl) {
    var token = getToken();
    var name = localStorage.getItem(USERNAME_KEY) || '';
    if (userEl) {
      userEl.textContent = token ? (name ? 'Hi，' + name : '已登录') : '';
    }
    if (loginEl) loginEl.hidden = !!token;
    if (logoutEl) {
      logoutEl.hidden = !token;
      logoutEl.onclick = function(e) {
        e.preventDefault();
        clearSession();
        window.location.href = 'index.html';
      };
    }
  }

  return {
    getToken: getToken,
    setToken: setToken,
    setSession: setSession,
    clearSession: clearSession,
    login: login,
    register: register,
    request: request,
    fetchMe: fetchMe,
    ensureUserId: ensureUserId,
    productSearch: productSearch,
    getProductDetail: getProductDetail,
    placeOrder: placeOrder,
    listOrders: listOrders,
    getOrder: getOrder,
    payOrder: payOrder,
    showMessage: showMessage,
    escapeHtml: escapeHtml,
    formatMoney: formatMoney,
    renderNav: renderNav
  };
})();
