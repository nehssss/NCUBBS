
//侧栏悬浮
//!function(i){i.fn.SidebarFixed=function(t){function e(t,e){return!0===t.initialized||!(i("body").width()<t.minWidth)&&(o(t,e),!0)}function o(t,e){t.initialized=!0,0===i("#theia-sticky-sidebar-stylesheet-"+t.namespace).length&&i("head").append(i('<style id="theia-sticky-sidebar-stylesheet-'+t.namespace+'">.SidebarFixed:after {content: ""; display: table; clear: both;}</style>')),e.each(function(){function e(){n.fixedScrollTop=0,n.sidebar.css({"min-height":"1px"}),n.stickySidebar.css({position:"static",width:"",transform:"none"})}function o(t){var e=t.height();return t.children().each(function(){e=Math.max(e,i(this).height())}),e}var n={};if(n.sidebar=i(this),n.options=t||{},n.container=i(n.options.containerSelector),0==n.container.length&&(n.container=n.sidebar.parent()),n.sidebar.parents().css("-webkit-transform","none"),n.sidebar.css({position:n.options.defaultPosition,overflow:"visible","-webkit-box-sizing":"border-box","-moz-box-sizing":"border-box","box-sizing":"border-box"}),n.stickySidebar=n.sidebar.find(".SidebarFixed"),0==n.stickySidebar.length){var s=/(?:text|application)\/(?:x-)?(?:javascript|ecmascript)/i;n.sidebar.find("script").filter(function(i,t){return 0===t.type.length||t.type.match(s)}).remove(),n.stickySidebar=i("<div>").addClass("SidebarFixed").append(n.sidebar.children()),n.sidebar.append(n.stickySidebar)}n.marginBottom=parseInt(n.sidebar.css("margin-bottom")),n.paddingTop=parseInt(n.sidebar.css("padding-top")),n.paddingBottom=parseInt(n.sidebar.css("padding-bottom"));var d=n.stickySidebar.offset().top,r=n.stickySidebar.outerHeight();n.stickySidebar.css("padding-top",1),n.stickySidebar.css("padding-bottom",1),d-=n.stickySidebar.offset().top,r=n.stickySidebar.outerHeight()-r-d,0==d?(n.stickySidebar.css("padding-top",0),n.stickySidebarPaddingTop=0):n.stickySidebarPaddingTop=1,0==r?(n.stickySidebar.css("padding-bottom",0),n.stickySidebarPaddingBottom=0):n.stickySidebarPaddingBottom=1,n.previousScrollTop=null,n.fixedScrollTop=0,e(),n.onScroll=function(n){if(n.stickySidebar.is(":visible")){if(i("body").width()<n.options.minWidth)return void e();if(n.options.disableOnResponsiveLayouts&&n.sidebar.outerWidth("none"==n.sidebar.css("float"))+50>n.container.width())return void e();var s=i(document).scrollTop(),d="static";if(s>=n.sidebar.offset().top+(n.paddingTop-n.options.additionalMarginTop)){var r,c=n.paddingTop+t.additionalMarginTop,p=n.paddingBottom+n.marginBottom+t.additionalMarginBottom,b=n.sidebar.offset().top,l=n.sidebar.offset().top+o(n.container),f=0+t.additionalMarginTop;r=n.stickySidebar.outerHeight()+c+p<i(window).height()?f+n.stickySidebar.outerHeight():i(window).height()-n.marginBottom-n.paddingBottom-t.additionalMarginBottom;var h=b-s+n.paddingTop,g=l-s-n.paddingBottom-n.marginBottom,u=n.stickySidebar.offset().top-s,S=n.previousScrollTop-s;"fixed"==n.stickySidebar.css("position")&&"modern"==n.options.sidebarBehavior&&(u+=S),"stick-to-top"==n.options.sidebarBehavior&&(u=t.additionalMarginTop),"stick-to-bottom"==n.options.sidebarBehavior&&(u=r-n.stickySidebar.outerHeight()),u=S>0?Math.min(u,f):Math.max(u,r-n.stickySidebar.outerHeight()),u=Math.max(u,h),u=Math.min(u,g-n.stickySidebar.outerHeight());var m=n.container.height()==n.stickySidebar.outerHeight();d=!m&&u==f||!m&&u==r-n.stickySidebar.outerHeight()?"fixed":s+u-n.sidebar.offset().top-n.paddingTop<=t.additionalMarginTop?"static":"absolute"}if("fixed"==d){var y=i(document).scrollLeft();n.stickySidebar.css({position:"fixed",width:a(n.stickySidebar)+"px",transform:"translateY("+u+"px)",left:n.sidebar.offset().left+parseInt(n.sidebar.css("padding-left"))-y+"px",top:"0px"})}else if("absolute"==d){var k={};"absolute"!=n.stickySidebar.css("position")&&(k.position="absolute",k.transform="translateY("+(s+u-n.sidebar.offset().top-n.stickySidebarPaddingTop-n.stickySidebarPaddingBottom)+"px)",k.top="0px"),k.width=a(n.stickySidebar)+"px",k.left="",n.stickySidebar.css(k)}else"static"==d&&e();"static"!=d&&1==n.options.updateSidebarHeight&&n.sidebar.css({"min-height":n.stickySidebar.outerHeight()+n.stickySidebar.offset().top-n.sidebar.offset().top+n.paddingBottom}),n.previousScrollTop=s}},n.onScroll(n),i(document).on("scroll."+n.options.namespace,function(i){return function(){i.onScroll(i)}}(n)),i(window).on("resize."+n.options.namespace,function(i){return function(){i.stickySidebar.css({position:"static"}),i.onScroll(i)}}(n)),"undefined"!=typeof ResizeSensor&&new ResizeSensor(n.stickySidebar[0],function(i){return function(){i.onScroll(i)}}(n))})}function a(i){var t;try{t=i[0].getBoundingClientRect().width}catch(i){}return void 0===t&&(t=i.width()),t}var n={containerSelector:"",additionalMarginTop:0,additionalMarginBottom:0,updateSidebarHeight:!0,minWidth:0,disableOnResponsiveLayouts:!0,sidebarBehavior:"modern",defaultPosition:"relative",namespace:"TSS"};return t=i.extend(n,t),t.additionalMarginTop=parseInt(t.additionalMarginTop)||0,t.additionalMarginBottom=parseInt(t.additionalMarginBottom)||0,function(t,o){e(t,o)||(console.log("TSS: Body width smaller than options.minWidth. Init is delayed."),i(document).on("scroll."+t.namespace,function(t,o){return function(a){e(t,o)&&i(this).unbind(a)}}(t,o)),i(window).on("resize."+t.namespace,function(t,o){return function(a){e(t,o)&&i(this).unbind(a)}}(t,o)))}(t,this),this}}(jQuery);
//

function ajax_bbs_menu(type,obj){
    $(obj).addClass('on').siblings().removeClass('on');
    var bbs_id=$('.jinsom-bbs-header').attr('data');
    $('.jinsom-bbs-list-box').html(jinsom.loading);
    $.ajax({
        type: "POST",
        url:jinsom.jinsom_ajax_url+"/ajax/bbs.php",
        data: {page:1,bbs_id:bbs_id,type:type},
        success: function(msg){

            $('.jinsom-bbs-list-box').empty();
            if($(obj).parents('.jinsom-bbs-box').next().hasClass('jinsom-bbs-list-4')){
                container=$('#jinsom-bbs-list-4');
                $(msg).find('img').each(function(index){
                    jinsom_loadImage($(this).attr('src'));
                })
                var $newElems = $(msg).css({ opacity: 0}).appendTo(container);
                $newElems.imagesLoaded(function(){
                    $newElems.animate({ opacity: 1},800);
                    container.masonry( 'reload', $newElems,true);
                });
            }else{
                $('.jinsom-bbs-list-box').html(msg);
            }


        }
    });
}
// 加精华
function doSupport(postId,support){
    if (!isLogin) {
        layer.msg('请先登录系统', {icon: 7, anim: 6, time: 1500}, function () {
            return ;
        });
    }
    B.post({
        url:'/post/doSupport',
        data:{postId:postId,support:support},
        loading:true,
        success:function (res) {
            if (res.success) {
                B.info("操作成功");
                window.location.reload()
            }else{
                B.error("操作失败");
            }
        }
    });
}
// 文章置顶
function doTop(postId,top){
    if (!loginUser) {
        layer.msg('请先登录系统', {icon: 7, anim: 6, time: 1500}, function () {
            return ;
        });
    }
    B.post({
        url:'/post/doTop',
        data:{postId:postId,top:top},
        loading:true,
        success:function (res) {
            if (res.success) {
                B.info("操作成功");
                window.location.reload()
            }else{
                B.error("操作失败");
            }
        }
    });
}
// 文章删除
function doDelete(postId){
    if (!loginUser) {
        layer.msg('请先登录系统', {icon: 7, anim: 6, time: 1500}, function () {
            return ;
        });
    }
    B.post({
        url:'/post/doDelete',
        data:{postId:postId},
        loading:true,
        success:function (res) {
            if (res.success) {
                B.info("删除成功");
            }else{
                B.error("删除失败");
            }
        }
    });
}
// 举报
function doAccusation(postId){
    if (!loginUser) {
        layer.alert("当前用户未登录", {
            icon: 2,
            title: '举报',
            shade: .1,
            skin: 'layui-layer-admin',
            btn: '前往登录',
            yes: function (index, layero) {
                showLoginForm();
            }
        });
        return false;
    }
    B.post({
        url:'/post/doAccusation',
        data:{postId:postId},
        loading:true,
        success:function (res) {
            if (res.success) {
                B.info("举报成功");
            }else{
                B.error("举报失败");
            }
        }
    });
}
function doSearch(catalogId){
    var keyword = $('#keyword').val();
    if (B.trim(keyword) == '') {
        B.error('请输入你要搜索的内容！');
        return false;
    }

}