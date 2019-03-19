var jQuery;
var wssh = {};


jQuery(function($){
  /*jslint browser:true */

  var status = $('#status'),
      btn = $('.btn-primary'),
      style = {};


  function parse_xterm_style() {
    var text = $('.xterm-helpers style').text();
    var arr = text.split('xterm-normal-char{width:');
    style.width = parseFloat(arr[1]);
    arr = text.split('div{height:');
    style.height = parseFloat(arr[1]);
  }


  function current_geometry() {
    if (!style.width || !style.height) {
      parse_xterm_style();
    }

    var cols = parseInt(window.innerWidth / style.width, 10) - 1;
    var rows = parseInt(window.innerHeight / style.height, 10);
    return {'cols': cols, 'rows': rows};
  }


  function resize_term(term, sock) {
    var geometry = current_geometry(),
        cols = geometry.cols,
        rows = geometry.rows;
    // console.log([cols, rows]);
    // console.log(term.geometry);

    if (cols !== term.geometry[0] || rows !== term.geometry[1]) {
      console.log('resizing term');
      term.resize(cols, rows);
      sock.send(JSON.stringify({'resize': [cols, rows]}));
    }
  }


  function callback(msg) {
    if (msg.status) {
      status.text(msg.status);
      setTimeout(function(){
        btn.prop('disabled', false);
      }, 3000);
      return;
    }

    var ws_url = window.location.href.replace('http', 'ws'),
        join = (ws_url[ws_url.length-1] === '/' ? '' : '/'),
        url = ws_url + join + 'ssh/' + msg.id,
        sock = new window.WebSocket(url),
        encoding = msg.encoding,
        terminal = document.getElementById('#terminal'),
        term = new window.Terminal({
          cursorBlink: true,
        });

    console.log(url);
    console.log(encoding);
    wssh.sock = sock;
    wssh.term = term;
    term.on('data', function(data) {
      sock.send(JSON.stringify({'data': data}));
    });

    sock.onopen = function() {
      $('.container').hide();
      term.open(terminal, true);
      term.toggleFullscreen(true);
    };

    sock.onmessage = function(msg) {
      var reader = new window.FileReader();

      reader.onloadend = function(){
        var decoder = new window.TextDecoder(encoding);
        var text = decoder.decode(reader.result);
        // console.log(text);
        term.write(text);
        if (!term.resized) {
          resize_term(term, sock);
          term.resized = true;
        }
      };

      reader.readAsArrayBuffer(msg.data);
    };

    sock.onerror = function(e) {
      console.log(e);
    };

    sock.onclose = function(e) {
      console.log(e);
      term.destroy();
      wssh.term = undefined;
      wssh.sock = undefined;
      $('.container').show();
      status.text(e.reason);
      btn.prop('disabled', false);
    };
  }


  $('form#connect').submit(function(event) {
      event.preventDefault();
      var form = $(this),
          url = form.attr('action'),
          type = form.attr('type'),
          data = new FormData(this);
      if (!data.get('hostname') || !data.get('port') || !data.get('username')) {
        status.text('Hostname, port and username are required.');
        return;
      }

      var pk = data.get('privatekey');
      if (pk && pk.size > 16384) {
        status.text('Key size exceeds maximum value.');
        return;
      }

      status.text('');
      btn.prop('disabled', true);
      $.ajax({
          url: url,
          type: type,
          data: data,
          success: callback,
          cache: false,
          contentType: false,
          processData: false
      });

  });


  $(window).resize(function(){
    if (wssh.term && wssh.sock) {
      resize_term(wssh.term, wssh.sock);
    }
  });

});
