/** EasyWeb iframe v3.0.7 data:2018-11-19 */

layui.define(['layer', 'admin', 'element'], function (exports) {
    var $ = layui.jquery;
    var layer = layui.layer;
    var admin = layui.admin;
    var element = layui.element;

    var cacheTab = layui.data('easyweb').cacheTab;
    var index = {
        cacheTab: cacheTab == undefined ? true : cacheTab,  // 是否记忆打开的选项卡
        // 加载主体部分
        loadView: function (param) {
            var menuPath = param.menuPath;
            var menuName = param.menuName;

            if (!menuPath) {
                console.error('url不能为空');
                return;
            }

            // 判断选项卡是否已添加
            var flag = false;
            $('.layui-layout-admin .layui-body .layui-tab .layui-tab-title>li').each(function () {
                if ($(this).attr('lay-id') === menuPath) {
                    flag = true;
                    return false;
                }
            });
            // 没有则添加
            if (!flag) {
                element.tabAdd('admin-pagetabs', {
                    id: menuPath,
                    title: menuName ? menuName : '无标题',
                    content: '<iframe src="' + menuPath + '" frameborder="0" class="admin-iframe"></iframe>'
                });
                // 记忆选项卡
                if (index.cacheTab) {
                    var indexTabs = admin.getTempData('indexTabs');
                    if (!indexTabs) {
                        indexTabs = new Array();
                    }
                    var isAddCache = false;
                    for (var i = 0; i < indexTabs.length; i++) {
                        if (param.menuPath == indexTabs[i].menuPath) {
                            isAddCache = true;
                        }
                    }
                    if (!isAddCache) {
                        indexTabs.push(param);
                        admin.putTempData('indexTabs', indexTabs);
                    }
                }
            }
            // 切换到该选项卡
            element.tabChange('admin-pagetabs', menuPath);
            // 移动设备切换页面隐藏侧导航
            if (document.body.clientWidth <= 750) {
                admin.flexible(true);
            }
        },
        // 打开新页面
        openTab: function (param) {
            var url = param.url;
            var title = param.title;

            index.loadView({
                menuPath: url,
                menuName: title
            });
        },
        // 关闭选项卡
        closeTab: function (url) {
            element.tabDelete('admin-pagetabs', url);
        },
        // 关闭选项卡记忆功能
        closeTabCache: function () {
            index.cacheTab = false;
            admin.putTempData('indexTabs', undefined);
        },
        // 清除缓存的tab
        clearTabCache: function () {
            admin.putTempData('indexTabs', undefined);
        },
        // 加载设置
        loadSetting: function () {
            // 恢复记忆的tab选项卡
            if (index.cacheTab) {
                var indexTabs = admin.getTempData('indexTabs');
                if (indexTabs) {
                    var tabPosition = admin.getTempData('tabPosition');
                    var mi = 0;
                    for (var i = 0; i < indexTabs.length; i++) {
                        index.loadView(indexTabs[i]);
                        if (indexTabs[i].menuPath == tabPosition) {
                            mi = i;
                        }
                    }
                    setTimeout(function () {
                        index.loadView(indexTabs[mi]);
                    }, 500);
                }
            }
            // 是否开启footer
            var openFooter = layui.data('easyweb').openFooter;
            if (openFooter != undefined && openFooter == false) {
                $('.layui-layout-admin .layui-footer').css('display', 'none');
                $('.layui-layout-admin .layui-body').css('bottom', '0');
            }
            // 是否开启tab自动刷新
            var tabAutoRefresh = layui.data('easyweb').tabAutoRefresh;
            if (tabAutoRefresh) {
                $('.layui-body .layui-tab[lay-filter="admin-pagetabs"]').attr('lay-autoRefresh', 'true');
            }
        }
    };

    // 监听侧导航栏点击事件
    element.on('nav(admin-side-nav)', function (elem) {
        var $that = $(elem);
        var menuUrl = $that.attr('lay-href');
        if (menuUrl && menuUrl != 'javascript:;') {
            var menuName = $that.text();
            index.loadView({
                menuPath: menuUrl,
                menuName: menuName
            });
        } else if ($('.layui-side .layui-nav-tree').attr('lay-accordion') == 'true' && $that.parent().hasClass('layui-nav-item')) {
            if ($that.parent().hasClass('layui-nav-itemed') || $that.parent().hasClass('layui-this')) {
                $('.layui-layout-admin .layui-side .layui-nav .layui-nav-item').removeClass('layui-nav-itemed');
                $that.parent().addClass('layui-nav-itemed');
            }
        } else {
            admin.setNavHoverCss($that.parentsUntil('.layui-nav-item').parent().children().eq(0));
        }
    });

    // tab选项卡切换监听
    element.on('tab(admin-pagetabs)', function (data) {
        var layId = $(this).attr('lay-id');

        admin.rollPage('auto');  // 自动滚动
        admin.activeNav(layId);  // 设置导航栏选中
        // $('.layui-table-tips-c').trigger('click');  // 切换tab关闭表格内浮窗

        // 解决切换tab滚动条时而消失的问题
        var $iframe = $('.layui-layout-admin .layui-body .layui-tab-content .layui-tab-item.layui-show .admin-iframe')[0];
        if ($iframe) {
            $iframe.style.height = "99%";
            $iframe.scrollWidth;
            $iframe.style.height = "100%";
        }
        $iframe.focus();

        // 记忆当前选中的选项卡
        if (index.cacheTab) {
            admin.putTempData('tabPosition', layId);
        }

        // 切换tab自动刷新
        var autoRefresh = $('.layui-body .layui-tab[lay-filter="admin-pagetabs"]').attr('lay-autoRefresh');
        if (autoRefresh == 'true') {
            setTimeout(function () {
                top.layui.admin.refresh();
            }, 300);
        }
    });

    // tab选项卡删除监听
    element.on('tabDelete(admin-pagetabs)', function (data) {
        if (index.cacheTab) {
            var layId = $(this).parent().attr('lay-id');
            var indexTabs = admin.getTempData('indexTabs');
            for (var i = 0; i < indexTabs.length; i++) {
                if (layId == indexTabs[i].menuPath) {
                    indexTabs.splice(i, 1);
                }
            }
            admin.putTempData('indexTabs', indexTabs);
        }
    });

    exports('index', index);
});
