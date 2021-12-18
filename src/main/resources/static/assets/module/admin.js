
layui.define(['layer'], function (exports) {
    var $ = layui.jquery;
    var layer = layui.layer;
    var setter = layui.cache;
    var admin = {version: 'v3.1.7', layerData: {}};

    /* 右侧弹出 */
    admin.popupRight = function (param) {
        param.anim = -1;
        param.offset = 'r';
        param.move = false;
        param.fixed = true;
        if (param.area === undefined) param.area = '336px';
        if (param.title === undefined) param.title = false;
        if (param.closeBtn === undefined) param.closeBtn = false;
        if (param.shadeClose === undefined) param.shadeClose = true;
        if (param.skin === undefined) param.skin = 'layui-anim layui-anim-rl layui-layer-adminRight';
        return admin.open(param);
    };

    /* 封装layer.open */
    admin.open = function (param) {
        if (param.content && param.type === 2) param.url = undefined;  // 参数纠正
        if (param.url && (param.type === 2 || param.type === undefined)) param.type = 1;  // 参数纠正
        if (param.area === undefined) param.area = param.type === 2 ? ['360px', '300px'] : '360px';
        if (param.offset === undefined) param.offset = '70px';
        if (param.shade === undefined) param.shade = .1;
        if (param.fixed === undefined) param.fixed = false;
        if (param.resize === undefined) param.resize = false;
        if (param.skin === undefined) param.skin = 'layui-layer-admin';
        var eCallBack = param.end;
        param.end = function () {
            layer.closeAll('tips');
            eCallBack && eCallBack();
        };
        if (param.url) {
            var sCallBack = param.success;
            param.success = function (layero, index) {
                $(layero).data('tpl', param.tpl || '');
                admin.reloadLayer(index, param.url, sCallBack);
            };
        }
        var layIndex = layer.open(param);
        if (param.data) admin.layerData['d' + layIndex] = param.data;
        return layIndex;
    };

    /* 获取弹窗数据 */
    admin.getLayerData = function (index, key) {
        if (index === undefined) {
            index = parent.layer.getFrameIndex(window.name);
            if (index === undefined) return null;
            else return parent.layui.admin.getLayerData(parseInt(index), key);
        } else if (isNaN(index)) {
            var layId = $(index).parents('.layui-layer').first().attr('id');
            if (layId) index = layId.substring(11);
        }
        var layerData = admin.layerData['d' + index];
        if (key && layerData) return layerData[key];
        return layerData;
    };

    /* 放入弹窗数据 */
    admin.putLayerData = function (key, value, index) {
        if (index === undefined) {
            index = parent.layer.getFrameIndex(window.name);
            if (index === undefined) return;
            else return parent.layui.admin.putLayerData(key, value, parseInt(index));
        } else if (isNaN(index)) {
            var layId = $(index).parents('.layui-layer').first().attr('id');
            if (layId) index = layId.substring(11);
        }
        var layerData = admin.getLayerData(index);
        if (!layerData) layerData = {};
        layerData[key] = value;
        admin.layerData['d' + index] = layerData;
    };

    /* 刷新url方式的layer */
    admin.reloadLayer = function (index, url, success) {
        if (typeof url === 'function') {
            success = url;
            url = undefined;
        }
        if (isNaN(index)) {
            var layId = $(index).parents('.layui-layer').first().attr('id');
            if (layId) index = layId.substring(11);
        }
        var $layero = $('#layui-layer' + index);
        if (url === undefined) url = $layero.data('url');
        if (!url) return;
        $layero.data('url', url);
        admin.showLoading($layero);
        admin.ajax({
            url: url,
            dataType: 'html',
            success: function (res) {
                admin.removeLoading($layero, false);
                if (typeof res !== 'string') res = JSON.stringify(res);
                var tpl = $layero.data('tpl');
                if (tpl === true || tpl === 'true') {
                    var data = admin.getLayerData(index) || {};
                    data.layerIndex = index;
                    res = admin.util.tpl(res, data, setter.tplOpen, setter.tplClose);
                }
                $layero.children('.layui-layer-content').html(res);
                success && success($layero[0], index);
            }
        });
    };

    /* 封装layer.alert */
    admin.alert = function (content, options, yes) {
        if (typeof options === 'function') {
            yes = options;
            options = {};
        }
        if (options.skin === undefined) options.skin = 'layui-layer-admin';
        if (options.shade === undefined) options.shade = .1;
        return layer.alert(content, options, yes);
    };

    /* 封装layer.confirm */
    admin.confirm = function (content, options, yes, cancel) {
        if (typeof options === 'function') {
            cancel = yes;
            yes = options;
            options = {};
        }
        if (options.skin === undefined) options.skin = 'layui-layer-admin';
        if (options.shade === undefined) options.shade = .1;
        return layer.confirm(content, options, yes, cancel);
    };

    /* 封装layer.prompt */
    admin.prompt = function (options, yes) {
        if (typeof options === 'function') {
            yes = options;
            options = {};
        }
        if (options.skin === undefined) options.skin = 'layui-layer-admin layui-layer-prompt';
        if (options.shade === undefined) options.shade = .1;
        return layer.prompt(options, yes);
    };

    /* 封装ajax请求，返回数据类型为json */
    admin.req = function (url, data, success, method, option) {
        if (typeof data === 'function') {
            option = method;
            method = success;
            success = data;
            data = {};
        }
        if (method !== undefined && typeof method !== 'string') {
            option = method;
            method = undefined;
        }
        if (!method) method = 'GET';
        if (typeof data === 'string') {
            if (!option) option = {};
            if (!option.contentType) option.contentType = 'application/json';
        } else if (setter.reqPutToPost) {
            if ('put' === method.toLowerCase()) {
                method = 'POST';
                data._method = 'PUT';
            } else if ('delete' === method.toLowerCase()) {
                method = 'GET';
                data._method = 'DELETE';
            }
        }
        return admin.ajax($.extend({
            url: (setter.baseServer || '') + url,
            data: data,
            type: method,
            dataType: 'json',
            success: success
        }, option));
    };

    /* 封装ajax请求 */
    admin.ajax = function (param) {
        var oldParam = admin.util.deepClone(param);
        if (!param.dataType) param.dataType = 'json';
        if (!param.headers) param.headers = {};
        // 统一设置header
        var headers = setter.getAjaxHeaders(param.url);
        if (headers) {
            for (var i = 0; i < headers.length; i++) {
                if (param.headers[headers[i].name] === undefined) {
                    param.headers[headers[i].name] = headers[i].value;
                }
            }
        }
        // success预处理
        var success = param.success;
        param.success = function (result, status, xhr) {
            var before = setter.ajaxSuccessBefore(admin.parseJSON(result), param.url, {
                param: oldParam,
                reload: function (p) {
                    admin.ajax($.extend(oldParam, p));
                },
                update: function (r) {
                    result = r;
                }
            });
            if (before !== false) success && success(result, status, xhr);
            else param.cancel && param.cancel();
        };
        param.error = function (xhr, status) {
            param.success({code: xhr.status, msg: xhr.statusText}, status, xhr);
        };
        // 解决缓存问题
        if (layui.cache.version && (!setter.apiNoCache || param.dataType.toLowerCase() !== 'json')) {
            if (param.url.indexOf('?') === -1) param.url += '?v=';
            else param.url += '&v=';
            if (layui.cache.version === true) param.url += new Date().getTime();
            else param.url += layui.cache.version;
        }
        return $.ajax(param);
    };

    /* 解析json */
    admin.parseJSON = function (str) {
        if (typeof str === 'string') {
            try {
                return JSON.parse(str);
            } catch (e) {
            }
        }
        return str;
    };

    /* 显示加载动画 */
    admin.showLoading = function (elem, type, opacity, size) {
        if (elem !== undefined && (typeof elem !== 'string') && !(elem instanceof $)) {
            type = elem.type;
            opacity = elem.opacity;
            size = elem.size;
            elem = elem.elem;
        }
        if (type === undefined) type = 1;
        if (size === undefined) size = 'sm';
        if (elem === undefined) elem = 'body';
        var loader = [
            '<div class="ball-loader ' + size + '"><span></span><span></span><span></span><span></span></div>',
            '<div class="rubik-loader ' + size + '"></div>',
            '<div class="signal-loader ' + size + '"><span></span><span></span><span></span><span></span></div>'
        ];
        $(elem).addClass('page-no-scroll');  // 禁用滚动条
        $(elem).scrollTop(0);
        var $loading = $(elem).children('.page-loading');
        if ($loading.length <= 0) {
            $(elem).append('<div class="page-loading">' + loader[type - 1] + '</div>');
            $loading = $(elem).children('.page-loading');
        }
        if (opacity !== undefined) $loading.css('background-color', 'rgba(255,255,255,' + opacity + ')');
        $loading.show();
    };

    /* 移除加载动画 */
    admin.removeLoading = function (elem, fade, del) {
        if (elem === undefined) elem = 'body';
        if (fade === undefined) fade = true;
        var $loading = $(elem).children('.page-loading');
        if (del) $loading.remove();
        else if (fade) $loading.fadeOut('fast');
        else $loading.hide();
        $(elem).removeClass('page-no-scroll');
    };

    /* 缓存临时数据 */
    admin.putTempData = function (key, value, local) {
        var tableName = local ? setter.tableName : setter.tableName + '_tempData';
        if (value === undefined || value === null) {
            if (local) {
                layui.data(tableName, {key: key, remove: true});
            } else {
                layui.sessionData(tableName, {key: key, remove: true});
            }
        } else {
            if (local) {
                layui.data(tableName, {key: key, value: value});
            } else {
                layui.sessionData(tableName, {key: key, value: value});
            }
        }
    };

    /* 获取缓存临时数据 */
    admin.getTempData = function (key, local) {
        if (typeof key === 'boolean') {
            local = key;
            key = undefined;
        }
        var tableName = local ? setter.tableName : setter.tableName + '_tempData';
        var tempData = local ? layui.data(tableName) : layui.sessionData(tableName);
        if (!key) return tempData;
        return tempData ? tempData[key] : undefined;
    };

    /* 关闭elem所在的页面层弹窗 */
    admin.closeDialog = function (elem) {
        if (elem) {
            var id = $(elem).parents('.layui-layer').first().attr('id');
            if (id && id.length >= 11) layer.close(id.substring(11));
        } else {
            parent.layer.close(parent.layer.getFrameIndex(window.name));
        }
    };

    /* 让当前的iframe弹层自适应高度 */
    admin.iframeAuto = function () {
        parent.layer.iframeAuto(parent.layer.getFrameIndex(window.name));
    };

    /* 获取浏览器高度 */
    admin.getPageHeight = function () {
        return document.documentElement.clientHeight || document.body.clientHeight;
    };

    /* 获取浏览器宽度 */
    admin.getPageWidth = function () {
        return document.documentElement.clientWidth || document.body.clientWidth;
    };

    /* 绑定表单弹窗 */
    admin.modelForm = function (layero, btnFilter, formFilter) {
        var $layero = $(layero);
        $layero.addClass('layui-form');
        if (formFilter) $layero.attr('lay-filter', formFilter);
        // 确定按钮绑定submit
        var $btnSubmit = $layero.find('.layui-layer-btn .layui-layer-btn0');
        $btnSubmit.attr('lay-submit', '');
        $btnSubmit.attr('lay-filter', btnFilter);
    };

    /* loading按钮 */
    admin.btnLoading = function (elem, text, loading) {
        if (text !== undefined && (typeof text === 'boolean')) {
            loading = text;
            text = undefined;
        }
        if (text === undefined) text = '&nbsp;加载中';
        if (loading === undefined) loading = true;
        var $elem = $(elem);
        if (loading) {
            $elem.addClass('ew-btn-loading');
            $elem.prepend('<span class="ew-btn-loading-text"><i class="layui-icon layui-icon-loading layui-anim layui-anim-rotate layui-anim-loop"></i>' + text + '</span>');
            $elem.prop('disabled', 'disabled');
        } else {
            $elem.removeClass('ew-btn-loading');
            $elem.children('.ew-btn-loading-text').remove();
            //$elem.removeProp('disabled', 'disabled');
            $elem.removeAttr('disabled');
        }
        console.info($elem.parent().html());
    };

    /* open事件解析layer参数 */
    admin.parseLayerOption = function (option) {
        // 数组类型进行转换
        for (var f in option) {
            if (!option.hasOwnProperty(f)) continue;
            if (option[f] && option[f].toString().indexOf(',') !== -1) {
                option[f] = option[f].toString().split(',');
            }
        }
        // function类型参数转换
        var funStrs = ['success', 'cancel', 'end', 'full', 'min', 'restore'];
        for (var i = 0; i < funStrs.length; i++) {
            for (var k in option) {
                if (!option.hasOwnProperty(k)) continue;
                if (k === funStrs[i]) option[k] = window[option[k]];
            }
        }
        // content取内容
        if (option.content && (typeof option.content === 'string') && option.content.indexOf('#') === 0) {
            if ($(option.content).is('script')) {
                option.content = $(option.content).html();
            } else {
                option.content = $(option.content);
            }
        }
        if (option.type === undefined && option.url === undefined) option.type = 2;  // 默认为iframe类型
        return option;
    };
    /* 修改配置信息 */
    admin.putSetting = function (key, value) {
        setter[key] = value;
        admin.putTempData(key, value, true);
    };
    /* 字符串形式的parent.parent转window对象 */
    admin.strToWin = function (str) {
        var win = window;
        if (!str) return win;
        var ws = str.split('.');
        for (var i = 0; i < ws.length; i++) win = win[ws[i]];
        return win;
    };
    /* admin提供的事件 */
    admin.events = {
        /* 后退 */
        back: function () {
            admin.strToWin($(this).data('window')).history.back();
        },
        /* 打开消息 */
        message: function () {
            var option = admin.util.deepClone($(this).data());
            admin.strToWin(option.window).layui.admin.popupRight($.extend({
                id: 'layer-notice',
                url: option.url || 'page/tpl/tpl-message.html'
            }, admin.parseLayerOption(option)));
        },
        /* 打开修改密码弹窗 */
        resetPwd: function () {
            var option = admin.util.deepClone($(this).data());
            admin.req('/user/userNoOauth',{userId:option.id},function (res) {
                if(res.success){
                    layer.msg("第三方账号无需密码直接登录", {icon: 1});
                }else{
                    admin.strToWin(option.window).layui.admin.open($.extend({
                        id: 'layer-psw',
                        title: '修改密码',
                        shade: 0,
                        url: option.url || '/common/resetPwd'
                    }, admin.parseLayerOption(option)));
                }
            },'GET');

        },
        /*登陆*/
        login: function(){
            window.location.href = "/common/login2";
            /*var option = admin.util.deepClone($(this).data());
            admin.strToWin(option.window).layui.admin.open($.extend({
                id: 'layer-',
                title: '欢迎回来',
                shade: 0,
                url: option.url || '/common/loginForm'
            }, admin.parseLayeloginrOption(option)));*/
        },
        /**
         * 退出登录
         * 参数 data-confirm true/false 是否提示退出登录
         *      data-content 提示内容
         *
         */
        logout: function () {
            var option = admin.util.deepClone($(this).data());
            if (false === option.confirm || 'false' === option.confirm) {
                doLogout();
                return;
            }
            admin.strToWin(option.window).layui.layer.confirm(option.content || '确定要退出登录吗？', $.extend({
                title: '温馨提示',
                skin: 'layui-layer-admin',
                shade: .1
            }, admin.parseLayerOption(option)), function () {
                doLogout();
            });

            function doLogout() {
                if (option.ajax) {
                    var loadIndex = layer.load(2);
                    admin.req(option.ajax, function (res) {
                        layer.close(loadIndex);
                        if (res.success) {
                            location.replace(option.url || '/');
                        } else {
                            layer.msg(res.msg, {icon: 2});
                        }
                    }, option.method || 'get');
                } else {
                    location.replace(option.url || '/');
                }
            }
        },
        /*注册*/
        reg: function(){
            window.location.href = "/common/register2";
            /*var option = admin.util.deepClone($(this).data());
            admin.strToWin(option.window).layui.admin.open($.extend({
                id: 'layer-reg',
                title: '注册',
                shade: 0,
                url: option.url || '/common/registerForm'
            }, admin.parseLayerOption(option)));*/
        },
        /*找回密码*/
        findPwd: function(){
            var option = admin.util.deepClone($(this).data());
            admin.strToWin(option.window).layui.admin.open($.extend({
                id: 'layer-findPwd',
                title: '找回密码',
                shade: 0,
                url: option.url || '/common/findPwd'
            }, admin.parseLayerOption(option)));
            admin.closeDialog('[name=login]')
        },
        /* 打开弹窗 */
        open: function () {
            var option = admin.util.deepClone($(this).data());
            admin.strToWin(option.window).layui.admin.open(admin.parseLayerOption(option));
        },
        /* 打开右侧弹窗 */
        popupRight: function () {
            var option = admin.util.deepClone($(this).data());
            admin.strToWin(option.window).layui.admin.popupRight(admin.parseLayerOption(option));
        },
        /* 关闭当前弹窗(智能) */
        closeDialog: function () {
            if ($(this).parents('.layui-layer').length > 0) admin.closeDialog(this);
            else admin.closeDialog();
        },
        /* 关闭当前iframe弹窗 */
        closeIframeDialog: function () {
            admin.closeDialog();
        },
        /* 关闭当前页面层弹窗 */
        closePageDialog: function () {
            admin.closeDialog(this);
        },
    };

    /* 选择位置 */
    admin.chooseLocation = function (param) {
        var dialogTitle = param.title;  // 弹窗标题
        var onSelect = param.onSelect;  // 选择回调
        var needCity = param.needCity;  // 是否返回行政区
        var mapCenter = param.center;  // 地图中心
        var defaultZoom = param.defaultZoom;  // 地图默认缩放级别
        var pointZoom = param.pointZoom;  // 选中时地图缩放级别
        var searchKeywords = param.keywords;  // poi检索关键字
        var searchPageSize = param.pageSize;  // poi检索最大数量
        var mapJsUrl = param.mapJsUrl;  // 高德地图js的url
        if (dialogTitle === undefined) dialogTitle = '选择位置';
        if (defaultZoom === undefined) defaultZoom = 11;
        if (pointZoom === undefined) pointZoom = 17;
        if (searchKeywords === undefined) searchKeywords = '';
        if (searchPageSize === undefined) searchPageSize = 30;
        if (mapJsUrl === undefined) mapJsUrl = 'https://webapi.amap.com/maps?v=1.4.14&key=006d995d433058322319fa797f2876f5';
        var isSelMove = false, selLocation;
        // 搜索附近
        var searchNearBy = function (lat, lng) {
            AMap.service(['AMap.PlaceSearch'], function () {
                var placeSearch = new AMap.PlaceSearch({
                    type: '',
                    pageSize: searchPageSize,
                    pageIndex: 1
                });
                var cpoint = [lng, lat];
                placeSearch.searchNearBy(searchKeywords, cpoint, 1000, function (status, result) {
                    if (status === 'complete') {
                        var pois = result.poiList.pois;
                        var htmlList = '';
                        for (var i = 0; i < pois.length; i++) {
                            var poiItem = pois[i];
                            if (poiItem.location !== undefined) {
                                htmlList += '<div data-lng="' + poiItem.location.lng + '" data-lat="' + poiItem.location.lat + '" class="ew-map-select-search-list-item">';
                                htmlList += '     <div class="ew-map-select-search-list-item-title">' + poiItem.name + '</div>';
                                htmlList += '     <div class="ew-map-select-search-list-item-address">' + poiItem.address + '</div>';
                                htmlList += '     <div class="ew-map-select-search-list-item-icon-ok layui-hide"><i class="layui-icon layui-icon-ok-circle"></i></div>';
                                htmlList += '</div>';
                            }
                        }
                        $('#ew-map-select-pois').html(htmlList);
                    }
                });
            });
        };
        // 渲染地图
        var renderMap = function () {
            var mapOption = {
                resizeEnable: true, // 监控地图容器尺寸变化
                zoom: defaultZoom  // 初缩放级别
            };
            mapCenter && (mapOption.center = mapCenter);
            var map = new AMap.Map('ew-map-select-map', mapOption);
            // 地图加载完成
            map.on('complete', function () {
                var center = map.getCenter();
                searchNearBy(center.lat, center.lng);
            });
            // 地图移动结束事件
            map.on('moveend', function () {
                if (isSelMove) {
                    isSelMove = false;
                } else {
                    $('#ew-map-select-tips').addClass('layui-hide');
                    $('#ew-map-select-center-img').removeClass('bounceInDown');
                    setTimeout(function () {
                        $('#ew-map-select-center-img').addClass('bounceInDown');
                    });
                    var center = map.getCenter();
                    searchNearBy(center.lat, center.lng);
                }
            });
            // poi列表点击事件
            $('#ew-map-select-pois').off('click').on('click', '.ew-map-select-search-list-item', function () {
                $('#ew-map-select-tips').addClass('layui-hide');
                $('#ew-map-select-pois .ew-map-select-search-list-item-icon-ok').addClass('layui-hide');
                $(this).find('.ew-map-select-search-list-item-icon-ok').removeClass('layui-hide');
                $('#ew-map-select-center-img').removeClass('bounceInDown');
                setTimeout(function () {
                    $('#ew-map-select-center-img').addClass('bounceInDown');
                });
                var lng = $(this).data('lng');
                var lat = $(this).data('lat');
                var name = $(this).find('.ew-map-select-search-list-item-title').text();
                var address = $(this).find('.ew-map-select-search-list-item-address').text();
                selLocation = {name: name, address: address, lat: lat, lng: lng};
                isSelMove = true;
                map.setZoomAndCenter(pointZoom, [lng, lat]);
            });
            // 确定按钮点击事件
            $('#ew-map-select-btn-ok').click(function () {
                if (selLocation === undefined) {
                    layer.msg('请点击位置列表选择', {icon: 2, anim: 6});
                } else if (onSelect) {
                    if (needCity) {
                        var loadIndex = layer.load(2);
                        map.setCenter([selLocation.lng, selLocation.lat]);
                        map.getCity(function (result) {
                            layer.close(loadIndex);
                            selLocation.city = result;
                            admin.closeDialog('#ew-map-select-btn-ok');
                            onSelect(selLocation);
                        });
                    } else {
                        admin.closeDialog('#ew-map-select-btn-ok');
                        onSelect(selLocation);
                    }
                } else {
                    admin.closeDialog('#ew-map-select-btn-ok');
                }
            });
            // 搜索提示
            var $inputSearch = $('#ew-map-select-input-search');
            $inputSearch.off('input').on('input', function () {
                var keywords = $(this).val();
                var $selectTips = $('#ew-map-select-tips');
                if (!keywords) {
                    $selectTips.html('');
                    $selectTips.addClass('layui-hide');
                }
                AMap.plugin('AMap.Autocomplete', function () {
                    var autoComplete = new AMap.Autocomplete({
                        city: '全国'
                    });
                    autoComplete.search(keywords, function (status, result) {
                        if (result.tips) {
                            var tips = result.tips;
                            var htmlList = '';
                            for (var i = 0; i < tips.length; i++) {
                                var tipItem = tips[i];
                                if (tipItem.location !== undefined) {
                                    htmlList += '<div data-lng="' + tipItem.location.lng + '" data-lat="' + tipItem.location.lat + '" class="ew-map-select-search-list-item">';
                                    htmlList += '     <div class="ew-map-select-search-list-item-icon-search"><i class="layui-icon layui-icon-search"></i></div>';
                                    htmlList += '     <div class="ew-map-select-search-list-item-title">' + tipItem.name + '</div>';
                                    htmlList += '     <div class="ew-map-select-search-list-item-address">' + tipItem.address + '</div>';
                                    htmlList += '</div>';
                                }
                            }
                            $selectTips.html(htmlList);
                            if (tips.length === 0) {
                                $('#ew-map-select-tips').addClass('layui-hide');
                            } else {
                                $('#ew-map-select-tips').removeClass('layui-hide');
                            }
                        } else {
                            $selectTips.html('');
                            $selectTips.addClass('layui-hide');
                        }
                    });
                });
            });
            $inputSearch.off('blur').on('blur', function () {
                var keywords = $(this).val();
                var $selectTips = $('#ew-map-select-tips');
                if (!keywords) {
                    $selectTips.html('');
                    $selectTips.addClass('layui-hide');
                }
            });
            $inputSearch.off('focus').on('focus', function () {
                var keywords = $(this).val();
                if (keywords) $('#ew-map-select-tips').removeClass('layui-hide');
            });
            // tips列表点击事件
            $('#ew-map-select-tips').off('click').on('click', '.ew-map-select-search-list-item', function () {
                $('#ew-map-select-tips').addClass('layui-hide');
                var lng = $(this).data('lng');
                var lat = $(this).data('lat');
                selLocation = undefined;
                map.setZoomAndCenter(pointZoom, [lng, lat]);
            });
        };
        // 显示弹窗
        var htmlStr = '<div class="ew-map-select-tool" style="position: relative;">';
        htmlStr += '        搜索：<input id="ew-map-select-input-search" class="layui-input icon-search inline-block" style="width: 190px;" placeholder="输入关键字搜索" autocomplete="off" />';
        htmlStr += '        <button id="ew-map-select-btn-ok" class="layui-btn icon-btn pull-right" type="button"><i class="layui-icon">&#xe605;</i>确定</button>';
        htmlStr += '        <div id="ew-map-select-tips" class="ew-map-select-search-list layui-hide">';
        htmlStr += '        </div>';
        htmlStr += '   </div>';
        htmlStr += '   <div class="layui-row ew-map-select">';
        htmlStr += '        <div class="layui-col-sm7 ew-map-select-map-group" style="position: relative;">';
        htmlStr += '             <div id="ew-map-select-map"></div>';
        htmlStr += '             <i id="ew-map-select-center-img2" class="layui-icon layui-icon-add-1"></i>';
        htmlStr += '             <img id="ew-map-select-center-img" src="https://3gimg.qq.com/lightmap/components/locationPicker2/image/marker.png" alt=""/>';
        htmlStr += '        </div>';
        htmlStr += '        <div id="ew-map-select-pois" class="layui-col-sm5 ew-map-select-search-list">';
        htmlStr += '        </div>';
        htmlStr += '   </div>';
        admin.open({
            id: 'ew-map-select',
            type: 1,
            title: dialogTitle,
            area: '750px',
            content: htmlStr,
            success: function (layero, dIndex) {
                var $content = $(layero).children('.layui-layer-content');
                $content.css('overflow', 'visible');
                admin.showLoading($content);
                if (undefined === window.AMap) {
                    $.getScript(mapJsUrl, function () {
                        renderMap();
                        admin.removeLoading($content);
                    });
                } else {
                    renderMap();
                    admin.removeLoading($content);
                }
            }
        });
    };

    /* 裁剪图片 */
    admin.cropImg = function (param) {
        var uploadedImageType = 'image/jpeg';  // 当前图片的类型
        var aspectRatio = param.aspectRatio;  // 裁剪比例
        var imgSrc = param.imgSrc;  // 裁剪图片
        var imgType = param.imgType;  // 图片类型
        var onCrop = param.onCrop;  // 裁剪完成回调
        var limitSize = param.limitSize;  // 限制选择的图片大小
        var acceptMime = param.acceptMime;  // 限制选择的图片类型
        var imgExts = param.exts;  // 限制选择的图片类型
        var dialogTitle = param.title;  // 弹窗的标题
        if (aspectRatio === undefined) aspectRatio = 1;
        if (dialogTitle === undefined) dialogTitle = '裁剪图片';
        if (imgType) uploadedImageType = imgType;
        layui.use(['Cropper', 'upload'], function () {
            var Cropper = layui.Cropper;
            var upload = layui.upload;

            // 渲染组件
            function renderElem() {
                var imgCropper, $cropImg = $('#ew-crop-img');
                // 上传文件按钮绑定事件
                var uploadOptions = {
                    elem: '#ew-crop-img-upload',
                    auto: false,
                    drag: false,
                    choose: function (obj) {
                        obj.preview(function (index, file, result) {
                            uploadedImageType = file.type;
                            $cropImg.attr('src', result);
                            if (!imgSrc || !imgCropper) {
                                imgSrc = result;
                                renderElem();
                            } else {
                                imgCropper.destroy();
                                imgCropper = new Cropper($cropImg[0], options);
                            }
                        });
                    }
                };
                if (limitSize !== undefined) uploadOptions.size = limitSize;
                if (acceptMime !== undefined) uploadOptions.acceptMime = acceptMime;
                if (imgExts !== undefined) uploadOptions.exts = imgExts;
                upload.render(uploadOptions);
                // 没有传图片触发上传图片
                if (!imgSrc) {
                    $('#ew-crop-img-upload').trigger('click');
                    return;
                }
                // 渲染裁剪组件
                var options = {
                    aspectRatio: aspectRatio,
                    preview: '#ew-crop-img-preview'
                };
                imgCropper = new Cropper($cropImg[0], options);
                // 操作按钮绑定事件
                $('.ew-crop-tool').on('click', '[data-method]', function () {
                    var data = $(this).data(), cropped, result;
                    if (!imgCropper || !data.method) return;
                    data = $.extend({}, data);
                    cropped = imgCropper.cropped;
                    switch (data.method) {
                        case 'rotate':
                            if (cropped && options.viewMode > 0) imgCropper.clear();
                            break;
                        case 'getCroppedCanvas':
                            if (uploadedImageType === 'image/jpeg') {
                                if (!data.option) data.option = {};
                                data.option.fillColor = '#fff';
                            }
                            break;
                    }
                    result = imgCropper[data.method](data.option, data.secondOption);
                    switch (data.method) {
                        case 'rotate':
                            if (cropped && options.viewMode > 0) imgCropper.crop();
                            break;
                        case 'scaleX':
                        case 'scaleY':
                            $(this).data('option', -data.option);
                            break;
                        case 'getCroppedCanvas':
                            if (result) {
                                onCrop && onCrop(result.toDataURL(uploadedImageType));
                                admin.closeDialog('#ew-crop-img');
                            } else {
                                layer.msg('裁剪失败', {icon: 2, anim: 6});
                            }
                            break;
                    }
                });
            }

            // 显示弹窗
            var htmlStr = '<div class="layui-row">';
            htmlStr += '        <div class="layui-col-sm8" style="min-height: 9rem;">';
            htmlStr += '             <img id="ew-crop-img" src="' + (imgSrc ? imgSrc : '') + '" style="max-width:100%;" alt=""/>';
            htmlStr += '        </div>';
            htmlStr += '        <div class="layui-col-sm4 layui-hide-xs" style="padding: 15px;text-align: center;">';
            htmlStr += '             <div id="ew-crop-img-preview" style="width: 100%;height: 9rem;overflow: hidden;display: inline-block;border: 1px solid #dddddd;"></div>';
            htmlStr += '        </div>';
            htmlStr += '   </div>';
            htmlStr += '   <div class="text-center ew-crop-tool" style="padding: 15px 10px 5px 0;">';
            htmlStr += '        <div class="layui-btn-group" style="margin-bottom: 10px;margin-left: 10px;">';
            htmlStr += '             <button title="放大" data-method="zoom" data-option="0.1" class="layui-btn icon-btn" type="button"><i class="layui-icon layui-icon-add-1"></i></button>';
            htmlStr += '             <button title="缩小" data-method="zoom" data-option="-0.1" class="layui-btn icon-btn" type="button"><span style="display: inline-block;width: 12px;height: 2.5px;background: rgba(255, 255, 255, 0.9);vertical-align: middle;margin: 0 4px;"></span></button>';
            htmlStr += '        </div>';
            htmlStr += '        <div class="layui-btn-group layui-hide-xs" style="margin-bottom: 10px;">';
            htmlStr += '             <button title="向左旋转" data-method="rotate" data-option="-45" class="layui-btn icon-btn" type="button"><i class="layui-icon layui-icon-refresh-1" style="transform: rotateY(180deg) rotate(40deg);display: inline-block;"></i></button>';
            htmlStr += '             <button title="向右旋转" data-method="rotate" data-option="45" class="layui-btn icon-btn" type="button"><i class="layui-icon layui-icon-refresh-1" style="transform: rotate(30deg);display: inline-block;"></i></button>';
            htmlStr += '        </div>';
            htmlStr += '        <div class="layui-btn-group" style="margin-bottom: 10px;">';
            htmlStr += '             <button title="左移" data-method="move" data-option="-10" data-second-option="0" class="layui-btn icon-btn" type="button"><i class="layui-icon layui-icon-left"></i></button>';
            htmlStr += '             <button title="右移" data-method="move" data-option="10" data-second-option="0" class="layui-btn icon-btn" type="button"><i class="layui-icon layui-icon-right"></i></button>';
            htmlStr += '             <button title="上移" data-method="move" data-option="0" data-second-option="-10" class="layui-btn icon-btn" type="button"><i class="layui-icon layui-icon-up"></i></button>';
            htmlStr += '             <button title="下移" data-method="move" data-option="0" data-second-option="10" class="layui-btn icon-btn" type="button"><i class="layui-icon layui-icon-down"></i></button>';
            htmlStr += '        </div>';
            htmlStr += '        <div class="layui-btn-group" style="margin-bottom: 10px;">';
            htmlStr += '             <button title="左右翻转" data-method="scaleX" data-option="-1" class="layui-btn icon-btn" type="button" style="position: relative;width: 41px;"><i class="layui-icon layui-icon-triangle-r" style="position: absolute;left: 9px;top: 0;transform: rotateY(180deg);font-size: 16px;"></i><i class="layui-icon layui-icon-triangle-r" style="position: absolute; right: 3px; top: 0;font-size: 16px;"></i></button>';
            htmlStr += '             <button title="上下翻转" data-method="scaleY" data-option="-1" class="layui-btn icon-btn" type="button" style="position: relative;width: 41px;"><i class="layui-icon layui-icon-triangle-d" style="position: absolute;left: 11px;top: 6px;transform: rotateX(180deg);line-height: normal;font-size: 16px;"></i><i class="layui-icon layui-icon-triangle-d" style="position: absolute; left: 11px; top: 14px;line-height: normal;font-size: 16px;"></i></button>';
            htmlStr += '        </div>';
            htmlStr += '        <div class="layui-btn-group" style="margin-bottom: 10px;">';
            htmlStr += '             <button title="重新开始" data-method="reset" class="layui-btn icon-btn" type="button"><i class="layui-icon layui-icon-refresh"></i></button>';
            htmlStr += '             <button title="选择图片" id="ew-crop-img-upload" class="layui-btn icon-btn" type="button" style="border-radius: 0 2px 2px 0;"><i class="layui-icon layui-icon-upload-drag"></i></button>';
            htmlStr += '        </div>';
            htmlStr += '        <button data-method="getCroppedCanvas" data-option="{ &quot;maxWidth&quot;: 4096, &quot;maxHeight&quot;: 4096 }" class="layui-btn icon-btn" type="button" style="margin-left: 10px;margin-bottom: 10px;"><i class="layui-icon">&#xe605;</i>完成</button>';
            htmlStr += '   </div>';
            admin.open({
                title: dialogTitle,
                area: '665px',
                type: 1,
                content: htmlStr,
                success: function (layero, dIndex) {
                    $(layero).children('.layui-layer-content').css('overflow', 'visible');
                    renderElem();
                }
            });
        });
    };

    /* 工具类 */
    admin.util = {
        /* 百度地图坐标转高德地图坐标 */
        Convert_BD09_To_GCJ02: function (point) {
            var x_pi = (3.14159265358979324 * 3000.0) / 180.0;
            var x = point.lng - 0.0065, y = point.lat - 0.006;
            var z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
            var theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
            point.lng = z * Math.cos(theta);
            point.lat = z * Math.sin(theta);
            return point;
        },
        /* 高德地图坐标转百度地图坐标 */
        Convert_GCJ02_To_BD09: function (point) {
            var x_pi = (3.14159265358979324 * 3000.0) / 180.0;
            var x = point.lng, y = point.lat;
            var z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
            var theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
            point.lng = z * Math.cos(theta) + 0.0065;
            point.lat = z * Math.sin(theta) + 0.006;
            return point;
        },
        /* 动态数字 */
        animateNum: function (elem, isThd, delay, grain) {
            isThd = isThd === null || isThd === undefined || isThd === true || isThd === 'true';  // 是否是千分位
            delay = isNaN(delay) ? 500 : delay;   // 动画延迟
            grain = isNaN(grain) ? 100 : grain;   // 动画粒度
            var getPref = function (str) {
                var pref = '';
                for (var i = 0; i < str.length; i++) if (!isNaN(str.charAt(i))) return pref; else pref += str.charAt(i);
            }, getSuf = function (str) {
                var suf = '';
                for (var i = str.length - 1; i >= 0; i--) if (!isNaN(str.charAt(i))) return suf; else suf = str.charAt(i) + suf;
            }, toThd = function (num, isThd) {
                if (!isThd) return num;
                if (!/^[0-9]+.?[0-9]*$/.test(num)) return num;
                num = num.toString();
                return num.replace(num.indexOf('.') > 0 ? /(\d)(?=(\d{3})+(?:\.))/g : /(\d)(?=(\d{3})+(?:$))/g, '$1,');
            };
            $(elem).each(function () {
                var $this = $(this);
                var num = $this.data('num');
                if (!num) {
                    num = $this.text().replace(/,/g, '');  // 内容
                    $this.data('num', num);
                }
                var flag = 'INPUT,TEXTAREA'.indexOf($this.get(0).tagName) >= 0;  // 是否是输入框
                var pref = getPref(num.toString());
                var suf = getSuf(num.toString());
                var strNum = num.toString().replace(pref, '').replace(suf, '');
                if (isNaN(strNum * 1) || strNum === '0') {
                    flag ? $this.val(num) : $this.html(num);
                    console.error('not a number');
                    return;
                }
                var int_dec = strNum.split('.');
                var deciLen = int_dec[1] ? int_dec[1].length : 0;
                var startNum = 0.0, endNum = strNum;
                if (Math.abs(endNum * 1) > 10) startNum = parseFloat(int_dec[0].substring(0, int_dec[0].length - 1) + (int_dec[1] ? '.0' + int_dec[1] : ''));
                var oft = (endNum - startNum) / grain, temp = 0;
                var mTime = setInterval(function () {
                    var str = pref + toThd(startNum.toFixed(deciLen), isThd) + suf;
                    flag ? $this.val(str) : $this.html(str);
                    startNum += oft;
                    temp++;
                    if (Math.abs(startNum) >= Math.abs(endNum * 1) || temp > 5000) {
                        str = pref + toThd(endNum, isThd) + suf;
                        flag ? $this.val(str) : $this.html(str);
                        clearInterval(mTime);
                    }
                }, delay / grain);
            });
        },
        /* 深度克隆对象 */
        deepClone: function (obj) {
            var result;
            var oClass = admin.util.isClass(obj);
            if (oClass === 'Object') result = {};
            else if (oClass === 'Array') result = [];
            else return obj;
            if(obj.__node) delete obj.__node;
            for (var key in obj) {
                if(key === '__node') continue;
                if (!obj.hasOwnProperty(key)) continue;
                var copy = obj[key], cClass = admin.util.isClass(copy);
                if (cClass === 'Object') result[key] = arguments.callee(copy); // 递归调用
                else if (cClass === 'Array') result[key] = arguments.callee(copy);
                else result[key] = obj[key];
            }
            return result;
        },
        /* 获取变量类型 */
        isClass: function (o) {
            if (o === null) return 'Null';
            if (o === undefined) return 'Undefined';
            return Object.prototype.toString.call(o).slice(8, -1);
        },
        /* 判断富文本是否为空 */
        fullTextIsEmpty: function (text) {
            if (!text) return true;
            var noTexts = ['img', 'audio', 'video', 'iframe', 'object'];
            for (var i = 0; i < noTexts.length; i++) {
                if (text.indexOf('<' + noTexts[i]) > -1) return false;
            }
            var str = text.replace(/\s*/g, '');  // 去掉所有空格
            if (!str) return true;
            str = str.replace(/&nbsp;/ig, '');  // 去掉所有&nbsp;
            if (!str) return true;
            str = str.replace(/<[^>]+>/g, '');   // 去掉所有html标签
            return !str;
        },
        /* 移除元素的style */
        removeStyle: function (elem, options) {
            if (typeof options === 'string') options = [options];
            $(elem).each(function () {
                try {
                    var arr = $(this).attr('style').split(';');
                    var style = '';
                    for (var i = 0; i < arr.length; i++) {
                        if (!$.trim(arr[i])) continue;
                        var add = true;
                        var att = $.trim(arr[i].split(':')[0]);
                        for (var j = 0; j < options.length; j++) {
                            if (!$.trim(options[j]) || att === $.trim(options[j])) {
                                add = false;
                                break;
                            }
                        }
                        if (add) style += ($.trim(arr[i]) + ';');
                    }
                    $(this).attr('style', style);
                } catch (e) {
                }
            });
        },
        /* 滚动到顶部 */
        scrollTop: function (elem) {
            if (!elem) elem = $('html,body');
            else elem = $(elem);
            elem.animate({scrollTop: 0}, 300);
        },
        /* 模板解析 */
        tpl: function (html, data, openCode, closeCode) {
            if (html === undefined || html === null || typeof html !== 'string') return html;
            if (!data) data = {};
            if (!openCode) openCode = '{{';
            if (!closeCode) closeCode = '}}';
            var tool = {
                exp: function (str) {
                    return new RegExp(str, 'g');
                },
                // 匹配满足规则内容
                query: function (type, _, __) {
                    var types = [
                        '#([\\s\\S])+?',   // js语句
                        '([^{#}])*?' // 普通字段
                    ][type || 0];
                    return tool.exp((_ || '') + openCode + types + closeCode + (__ || ''));
                },
                escape: function (str) {
                    return String(str || '').replace(/&(?!#?[a-zA-Z0-9]+;)/g, '&amp;')
                        .replace(/</g, '&lt;').replace(/>/g, '&gt;')
                        .replace(/'/g, '&#39;').replace(/"/g, '&quot;');
                },
                error: function (e, tplog) {
                    console.error('Laytpl Error：' + e + '\n' + (tplog || ''));
                },
                parse: function (tpl, data) {
                    var tplog = tpl;
                    try {
                        var jss = tool.exp('^' + openCode + '#'), jsse = tool.exp(closeCode + '$');
                        tpl = tpl.replace(tool.exp(openCode + '#'), openCode + '# ')
                            .replace(tool.exp(closeCode + '}'), '} ' + closeCode).replace(/\\/g, '\\\\')
                            // 不匹配指定区域的内容
                            .replace(tool.exp(openCode + '!(.+?)!' + closeCode), function (str) {
                                str = str.replace(tool.exp('^' + openCode + '!'), '')
                                    .replace(tool.exp('!' + closeCode), '')
                                    .replace(tool.exp(openCode + '|' + closeCode), function (tag) {
                                        return tag.replace(/(.)/g, '\\$1')
                                    });
                                return str
                            })
                            // 匹配JS规则内容
                            .replace(/(?="|')/g, '\\').replace(tool.query(), function (str) {
                                str = str.replace(jss, '').replace(jsse, '');
                                return '";' + str.replace(/\\/g, '') + ';view+="';
                            })
                            // 匹配普通字段
                            .replace(tool.query(1), function (str) {
                                var start = '"+(';
                                if (str.replace(/\s/g, '') === openCode + closeCode) {
                                    return '';
                                }
                                str = str.replace(tool.exp(openCode + '|' + closeCode), '');
                                if (/^=/.test(str)) {
                                    str = str.replace(/^=/, '');
                                    start = '"+_escape_(';
                                }
                                return start + str.replace(/\\/g, '') + ')+"';
                            })
                            // 换行符处理
                            .replace(/\r\n/g, '\\r\\n" + "').replace(/\n/g, '\\n" + "').replace(/\r/g, '\\r" + "');
                        tpl = '"use strict";var view = "' + tpl + '";return view;';
                        tpl = new Function('d, _escape_', tpl);
                        return tpl(data, tool.escape);
                    } catch (e) {
                        tool.error(e, tplog);
                        return tplog;
                    }
                }
            };
            return tool.parse(html, data);
        }
    };

    /* 事件监听 */
    admin.on = function (events, callback) {
        return layui.onevent.call(this, 'admin', events, callback);
    };

    /* 所有javabb-event */
    $(document).on('click', '*[javabb-event]', function () {
        var event = $(this).attr('javabb-event');
        var te = admin.events[event];
        te && te.call(this, $(this));
    });

    /* 所有lay-tips处理 */
    $(document).on('mouseenter', '*[lay-tips]', function () {
        var $this = $(this);
        admin.tips({
            elem: this,
            text: $this.attr('lay-tips'),
            direction: $this.attr('lay-direction'),
            bg: $this.attr('lay-bg'),
            offset: $this.attr('lay-offset'),
            padding: $this.attr('lay-padding'),
            color: $this.attr('lay-color'),
            bgImg: $this.attr('lay-bgImg'),
            fontSize: $this.attr('lay-fontSize')
        });
    }).on('mouseleave', '*[lay-tips]', function () {
        layer.closeAll('tips');
    });

    /* tips方法封装 */
    admin.tips = function (option) {
        layer.tips(option.text, option.elem, {
            tips: [option.direction || 1, option.bg || '#303133'],
            tipsMore: option.tipsMore,
            time: option.time || -1,
            success: function (layero) {
                var $content = $(layero).children('.layui-layer-content');
                if (option.padding || option.padding === 0) $content.css('padding', option.padding);
                if (option.color) $content.css('color', option.color);
                if (option.bgImg) $content.css('background-image', option.bgImg);
                if (option.fontSize) $content.css('font-size', option.fontSize);
                if (!option.offset) return;
                var offset = option.offset.split(',');
                var top = offset[0], left = offset.length > 1 ? offset[1] : undefined;
                if (top) $(layero).css('margin-top', top);
                if (left) $(layero).css('margin-left', left);
            }
        });
    };
    /* 读取缓存的配置信息 */
    setter = $.extend({
        tableName: 'javabb-bbs-web',  // 存储表名
        apiNoCache: true,  // ajax请求json数据不带版本号
        ajaxSuccessBefore: function (res, url, obj) {
            return admin.ajaxSuccessBefore ? admin.ajaxSuccessBefore(res, url, obj) : true;
        },
        getAjaxHeaders: function (res, url, obj) {
            return admin.getAjaxHeaders ? admin.getAjaxHeaders(res, url, obj) : [];
        }
    }, setter);
    var cache = admin.getTempData(true);
    /*恢复配置文件*/
    admin.recoverConfig = function(){

    };
    admin.recoverConfig();
    admin.setter = setter;
    exports('admin', admin);
});
