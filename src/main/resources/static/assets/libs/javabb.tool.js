/**
 * 工具类
 */
(function ($) {
    /* 鼠标点击向上冒泡弹出提示动画 */
    $.extend({
        bubble: {
            _tip: ['法制', '爱国', '敬业', '诚信', '友善', '富强', '民主', '文明', '和谐', '自由', '平等', '公正'],
            init: function () {
                var bubbleIndex = 0;
                $('body').click(function (e) {
                    bubbleIndex = bubbleIndex >= $.bubble._tip.length ? 0 : bubbleIndex;
                    if (!e.originalEvent) {
                        return;
                    }
                    var x = e.originalEvent.x || e.originalEvent.layerX || 0;
                    var y = e.originalEvent.y || e.originalEvent.layerY || 0;
                    var html = '<span style="position: fixed;z-index:9999;left: ' + x + 'px;top: ' + y + 'px;"></span>';
                    var $box = $(html).appendTo($(this));
                    $box.effectBubble({
                        y: -100,
                        className: 'thumb-bubble',
                        fontSize: 0.5,
                        content: '<i class="fa fa-smile-o"></i>' + $.bubble._tip[bubbleIndex]
                    });
                    setTimeout(function () {
                        $box.remove();
                    }, 1002);
                    bubbleIndex++;
                });
            },
            unbind: function (duration) {
                $("body").unbind('click');
                if (duration && !isNaN(duration = parseInt(duration))) {
                    setTimeout(function () {
                        $.bubble.init();
                    }, duration);
                }
            }
        }
    });
    /**
     * 工具方法
     */
    $.extend({
        tool: {
            cache: function (key, value) {
                if (!value) {
                    return localStorage.getItem(key);
                }
                localStorage.setItem(key, value);
                return false;
            },
            delCache: function (key) {
                if (this.cache(key)) {
                    localStorage.removeItem(key);
                }
            },
            isEmpty: function (value) {
                if (value == null || this.trim(value) == "") {
                    return true;
                }
                return false;
            },
            isInteger: function () {
                return (new RegExp(/^\d+$/).test(this));
            },
            isNumber: function (value, element) {
                return (new RegExp(/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/).test(this));
            },
            trim: function (value) {
                if (value == null) {
                    return "";
                }
                return value.replace(/(^\s*)|(\s*$)|\r|\n/g, "");
            },
            html2Txt: function (value) {
                value = this.trim(value);
                value = value.replace(/(\n)/g, "");
                value = value.replace(/(\t)/g, "");
                value = value.replace(/(\r)/g, "");
                value = value.replace(/<\/?[^>]*>/g, "");
                value = value.replace(/\s*/g, "");
                return value;
            },
            currentPath: function () {
                /* // 域
                 var domain = document.domain;
                 // 当前页
                 var nowurl = document.URL;
                 // 来源页
                 var fromurl = document.referrer;

                 console.log(domain);
                 console.log(fromurl);
                 console.log(nowurl);*/
                return window.location.pathname;
            },
            getMeta: function (name) {
                var meta = document.getElementsByTagName('meta');
                var share_desc = '';
                for (i in meta) {
                    if (typeof meta[i].name != "undefined" && meta[i].name.toLowerCase() == name.toLowerCase()) {
                        share_desc = meta[i].content;
                        break;
                    }
                }
                return share_desc;
            },
            random: function (min, max) {
                return Math.floor((Math.random() * max) + min);
            },
            shuffle: function (arr) {
                if (!arr) {
                    return arr;
                }
                var len = arr.length;
                for (var i = 0; i < len; i++) {
                    var end = len - 1;
                    var index = (Math.random() * (end + 1)) >> 0;
                    var temp = arr[end];
                    arr[end] = arr[index];
                    arr[index] = temp;
                }
                return arr;
            }
        }
    });


})(jQuery);

/**
 * 扩展String方法
 */
$.extend(String.prototype, {
    /*trim: function () {
        return this.replace(/(^\s*)|(\s*$)|\r|\n/g, "");
    },*/
    startsWith: function (pattern) {
        return this.indexOf(pattern) === 0;
    },
    endsWith: function (pattern) {
        var d = this.length - pattern.length;
        return d >= 0 && this.lastIndexOf(pattern) === d;
    },
    replaceSuffix: function (index) {
        return this.replace(/\[[0-9]+\]/, '[' + index + ']').replace('#index#', index);
    },
    getRequestURI: function () {
        var indexOf = this.indexOf("?");
        return (indexOf == -1) ? this : this.substr(0, indexOf);
    },
    getParams: function (encode) {
        var params = {},
            indexOf = this.indexOf("?");
        if (indexOf != -1) {
            var str = this.substr(indexOf + 1),
                strs = str.split("&");
            for (var i = 0; i < strs.length; i++) {
                var item = strs[i].split("=");
                var val = encode ? item[1].encodeParam() : item[1];
                params[item[0]] = item.length > 1 ? val : '';
            }
        }
        return params;
    },
    encodeParam: function () {
        return encodeURIComponent(this);
    },
    replaceAll: function (os, ns) {
        return this.replace(new RegExp(os, "gm"), ns);
    },
    skipChar: function (ch) {
        if (!this || this.length === 0) {
            return '';
        }
        if (this.charAt(0) === ch) {
            return this.substring(1).skipChar(ch);
        }
        return this;
    },
    isPositiveInteger: function () {
        return (new RegExp(/^[1-9]\d*$/).test(this));
    },
    isInteger: function () {
        return (new RegExp(/^\d+$/).test(this));
    },
    isNumber: function (value, element) {
        return (new RegExp(/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/).test(this));
    },
    isValidPwd: function () {
        return (new RegExp(/^([_]|[a-zA-Z0-9]){6,32}$/).test(this));
    },
    isValidMail: function () {
        return (new RegExp(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/).test(this.trim()));
    },
    isSpaces: function () {
        for (var i = 0; i < this.length; i += 1) {
            var ch = this.charAt(i);
            if (ch != ' ' && ch != "\n" && ch != "\t" && ch != "\r") {
                return false;
            }
        }
        return true;
    },
    isMobile: function () {
        return (new RegExp(/(^[0-9]{11,11}$)/).test(this));
    },
    isUrl: function () {
        return (new RegExp(/^[a-zA-z]+:\/\/([a-zA-Z0-9\-\.]+)([-\w .\/?%&=:]*)$/).test(this));
    },
    isExternalUrl: function () {
        return this.isUrl() && this.indexOf("://" + document.domain) == -1;
    },
    parseCurrency: function (num) {
        var numberValue = parseFloat(this);
        return parseFloat(numberValue.toFixed(num || 2));
    }
});

/**
 * 时间对象的格式化;
 */
Date.prototype.format = function (format) {
    /*
     * eg:format="YYYY-MM-dd hh:mm:ss";
     */
    var o = {
        "M+": this.getMonth() + 1, // month
        "d+": this.getDate(), // day
        "h+": this.getHours(), // hour
        "m+": this.getMinutes(), // minute
        "s+": this.getSeconds(), // second
        "q+": Math.floor((this.getMonth() + 3) / 3), // quarter
        "S": this.getMilliseconds()
        // millisecond
    };

    if (/(y+)/.test(format)) {
        format = format.replace(RegExp.$1, (this.getFullYear() + "")
            .substr(4 - RegExp.$1.length));
    }

    for (var k in o) {
        if (new RegExp("(" + k + ")").test(format)) {
            format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k]
                : ("00" + o[k]).substr(("" + o[k]).length));
        }
    }
    return format;
};