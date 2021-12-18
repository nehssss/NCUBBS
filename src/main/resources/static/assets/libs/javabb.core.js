

/* 文字滚动 start */
(function ($) {

})(jQuery);

var javabb = window.javabb || {
    /**
     * 通知  文字滚动
     */
    initTextSlider: function () {
        $.fn.textSlider = function (settings) {
            settings = jQuery.extend({
                speed: "normal",
                line: 2,
                timer: 3000
            }, settings);
            return this.each(function () {
                $.fn.textSlider.scllor($(this), settings);
            });
        };
        $.fn.textSlider.scllor = function ($this, settings) {
            var ul = $("ul:eq(0)", $this);
            var timerID;
            var li = ul.children();
            var liHight = $(li[0]).height();
            var upHeight = 0 - settings.line * liHight;//滚动的高度；
            var scrollUp = function () {
                ul.animate({marginTop: upHeight}, settings.speed, function () {
                    for (i = 0; i < settings.line; i++) {
                        ul.find("li:first", $this).appendTo(ul);
                    }
                    ul.css({marginTop: 0});
                });
            };
            var autoPlay = function () {
                timerID = window.setInterval(scrollUp, settings.timer);
            };
            var autoStop = function () {
                window.clearInterval(timerID);
            };
            //事件绑定
            ul.hover(autoStop, autoPlay).mouseout();
        };
        /*绑定元素 滚动通知*/
        if ($("#scrolldiv")) {
            $("#scrolldiv").textSlider({line: 1, speed: 300, timer: 10000});
        }
    },
    /*发帖编辑器*/
    tinymce: {
        _instance: window.tinymce,
        defaultConfig: {
            selector: '#editor',
            height: 400,
            imageUploadUrl: '/api/file/upload',
        },
        commentInit: function (option) {
            tinymce.init({
                selector: option.selector || javabb.tinymce.defaultConfig.selector,
                height: option.height || javabb.tinymce.defaultConfig.height,
                language: 'zh_CN',
                menubar: false,
                toolbar: option.toolbar || 'undo redo | styleselect | bold italic | link image',
            });
        },
        init:function(option){
            //var config = $.extend(javabb.tinymce.defaultConfig, option);
            // 渲染富文本编辑器
            tinymce.init({
                selector: option.selector || javabb.tinymce.defaultConfig.selector,
                height: option.height || javabb.tinymce.defaultConfig.height,
                language: 'zh_CN',
                menubar: false,
                plugins: option.plugins || 'code print preview fullscreen paste searchreplace save autosave link autolink image axupimgs imagetools media table codesample lists advlist hr charmap emoticons anchor directionality pagebreak nonbreaking visualblocks visualchars wordcount',
                toolbar: option.toolbar || 'fullscreen preview code | undo redo |formatselect fontsizeselect | forecolor backcolor | bold italic underline strikethrough | quicktable alignleft aligncenter alignright alignjustify | fontselect  outdent indent | numlist bullist | link image axupimgs media emoticons charmap anchor pagebreak codesample | ltr rtl',
                toolbar_drawer: 'sliding',
                branding: false,
                autosave_ask_before_unload: false,
                file_picker_types: 'media',
                file_picker_callback: function (callback, value, meta) {
                    top.layer.msg('演示环境不允许上传', {anim: 6});
                },
                images_upload_handler: function (blobInfo, success, failure) {
                    var xhr, formData;
                    var file = blobInfo.blob();//转化为易于理解的file对象
                    xhr = new XMLHttpRequest();
                    xhr.withCredentials = false;
                    xhr.open('POST', '/api/file/upload?dir=article');
                    xhr.onload = function() {
                        if (xhr.status != 200) {
                            failure('HTTP Error: ' + xhr.status);
                            return;
                        }
                        /*if (!json || typeof json.url != 'string') {
                            failure('Invalid JSON: ' + xhr.responseText);
                            return;
                        }*/
                        var json = JSON.parse(xhr.responseText);
                        console.info(json);
                        if (json.code === 200) {
                            success(json.fullFilePath);
                        }
                    };
                    formData = new FormData();
                    formData.append('file', file, file.name);
                    xhr.send(formData);
                }
            });
        },
        getContent: function (selector) {
            return tinymce.get(selector).getContent();
        },
        getContentText: function (selector) {
            return tinymce.get(selector).getContent({format: 'text'});
        },
        setContent: function (selector, content) {
            tinymce.get(selector).setContent(content);
        },
        appendContent: function(selector,content){
            tinymce.get(selector).insertContent(content,{});
        }

    },

}

$(document).ready(function () {
    javabb.initTextSlider();
});