// 以下代码是配置layui扩展模块的目录，以及加载主题
layui.config({
    base: getProjectUrl() + 'assets/module/',
    baseServer: '',                 // admin.req的url会自动在前面加这个
    tableName: 'javabb-bbs-web',    // 存储表名
    reqPutToPost: true,             // 为true会自动把put变post，delete变get并加_method
    apiNoCache: true,               // ajax请求json不加版本号
    getAjaxHeaders: function(){},   // ajax统一传递header
    ajaxSuccessBefore: function(){},// ajax统一预处理
}).extend({
    formSelects: 'formSelects/formSelects-v4',
    treeTable: 'treeTable/treeTable',
    dropdown: 'dropdown/dropdown',
    notice: 'notice/notice',
    steps: 'steps/steps',
    treeSelect:'treeSelect/treeSelect',
    sliderVerify:'sliderVerify/sliderVerify',
    QRCode: 'QRCode',
    tagsInput: 'tagsInput/tagsInput',
    cascader: 'cascader/cascader',
    fileChoose: 'fileChoose/fileChoose'
}).use(['layer','table'], function () {
    var $ = layui.jquery;
    var layer = layui.layer;

});

// 获取当前项目的绝对路径
function getProjectUrl() {
    var layuiDir = layui.cache.dir;
    if (!layuiDir) {
        var js = document.scripts, last = js.length - 1, src;
        for (var i = last; i > 0; i--) {
            if (js[i].readyState === 'interactive') {
                src = js[i].src;
                break;
            }
        }
        var jsPath = src || js[last].src;
        layuiDir = jsPath.substring(0, jsPath.lastIndexOf('/') + 1);
    }
    return layuiDir.substring(0, layuiDir.indexOf('assets'));
}