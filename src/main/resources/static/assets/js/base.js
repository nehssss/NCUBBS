/*一些公共方法*/
$(function() {
    layui.use(['layer', 'form', 'element', 'laydate','admin'], function () {
        var $ = layui.jquery;
        var form = layui.form;
        var element = layui.element;
        var layer = layui.layer;
        var admin = layui.admin;

    });
    var header_height = $('.javabb-header').height();
    //全站滚动条事件
    $(window).scroll(function() {
        if ($(window).scrollTop() >= header_height) {
            $('.javabb-header').addClass('fixed');
        } else {
            $('.javabb-header').removeClass('fixed');
        }
    });
    //弹窗搜索
    console.log("%c Javabb论坛 http://portal.javabb.cn 代码如歌:) ", "color: #fff; background: #2196f3; padding:6px 2px;");
    //点击弹出搜索
    $('li.search').on('click', function (event) {
        event.preventDefault();
        $('.pop-search').addClass('show');
        $('body').css('overflow', 'hidden');
        $('.pop-search-content input').focus();
    });

    //点击关闭搜索
    $('.pop-search .close').click(function () {
        $('.pop-search').removeClass('show');
        $('body').css('overflow', 'auto');
    });

    //按esc键关闭 弹窗搜索
    $(document).keyup(function (event) {
        if (event.which == '27') {
            $('.pop-search').removeClass('show');
            $('body').css('overflow', 'auto');
        }
    });

    //提交搜索
    $(".pop-search-content span").click(function () {
        var search_val = $(".pop-search-content input").val();
        if (search_val == '') {
            layer.msg('搜索的内容不能为空！');
            return false;
        }
        window.location.href = jinsom.home_url + '/?s=' + search_val;
    });

    // 回车搜索
    $(".pop-search-content input").keypress(function (e) {
        if (e.which == 13) {
            var search_val = $(".pop-search-content input").val();
            if (search_val == '') {
                layer.msg('搜索的内容不能为空！');
                return false;
            }
            window.location.href = '/?s=' + search_val;
        }
    });

    //论坛ajax搜索 回车搜索
    $("#bbs-search").keypress(function (e) {
        if (e.which == 13) {
            //jinsom_ajax_bbs_search();
        }
    });
    //返回顶部
    $(".totop").click(function () {
        var speed = 500; //滑动的速度
        $('body,html').animate({
            scrollTop: 0
        }, speed);
        return false;
    });
    $(window).scroll(function () {
        height = $(document).scrollTop();
        if (height > 400) {
            $(".totop").show()
        } else {
            $(".totop").hide()
        }
        ;

    });
    //下拉事件
    $('.tiezi-title .do').click(function(event){
        event.stopPropagation();
        $(this).children(".tiezi-setting").toggle(100);
    });

});

function buyHideContent(id,price) {

    layer.confirm('你确定花费'+price+'元宝购买隐藏内容吗？',{
        btn:['确定','取消'],
        yes:function (index,layero) {
            if(!isLogin){
                B.error("请先登录");
            }
           B.post({
               url:'/post/buyHideContent',
               data:{hideContentId:id,price:price},
               loading:true,
               success:function (result) {
                   if(result.success){
                       B.info(result.msg);
                       window.location.reload();
                   }else{
                       B.error(result.msg);
                       return;
                   }
               }
           });
        }
    });

}


//});