
(function ($) {

    $.fn.extend({
        // 翻牌效果
        flipCard: function (options) {
            var op = $.extend({}, options);
            return this.each(function () {
                var $box = $(this);
                $box.removeClass('animation-rotate-up').removeClass('animation-rotate-up-back');
                $box.addClass("animation-rotate-up");
                setTimeout(function () {
                    $box.addClass("animation-rotate-up-back");
                }, 500);
            });
        },

        // 文字向上冒泡
        effectBubble: function (options) {
            var op = $.extend({content: '+1', y: -100, duration: 1000, effectType: 'ease', className: '', fontSize: 2}, options);

            return this.each(function () {
                var $box = $(this), flyId = 'effect-fly-' + (new Date().getTime());

                var tpl = '<span id="#flyId#" class="effect-bubble-fly #class#" style="opacity: 1;top:#top#px;left:#left#px;font-size: #fontSize#rem;">#content#</span>';
                var html = tpl.replaceAll('#left#', 12).replaceAll('#top#', -8)
                    .replaceAll('#flyId#', flyId).replaceAll('#content#', op.content)
                    .replaceAll('#class#', op.className).replaceAll('#fontSize#', op.fontSize);

                var $fly = $(html).appendTo($box);
                $fly.fadeIn(100, "swing", function () {
                    $fly.animate({top: op.y, opacity: 0}, 100, function () {
                        $fly.fadeOut(op.duration, function () {
                            $fly.remove();
                        });
                    });
                });
            });
        }
    });
})(jQuery);
$(document).ready(function() {
    $.bubble.init();

});