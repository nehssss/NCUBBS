/*登陆js,处理登陆注册,找回密码,邮箱验证等*/
/*		登陆表单		*/
function showLoginForm(){
    layer.load(1);
    layer.open({
        title:'欢迎回来',
        btn: false,
        type: 2,
        offset:'100px',
        resize:false,
        skin:'layui-layer-admin',
        area: ['360px','auto'],
        content: '/common/loginForm'
    });
    admin.strToWin(option.window).layui.admin.open($.extend({
        id: 'layer-login',
        title: '欢迎回来',
        shade: 0,
        url: option.url || '/common/loginForm'
    }, admin.parseLayerOption(option)));
}
function showRegForm(){
    layer.open({
        title:'账号注册',
        btn: false,
        type: 2,
        offset:'100px',
        resize:false,
        area: ['360px','450px'],
        content: '/common/registerForm'
    });
}
function pop_login(){
    var username = $('#pop-username').val();
    var password = $('#pop-password').val();
    if(B.isEmpty(username)){
        B.error("账号不能为空");
        //$('#pop-username').focus();
        //return;
    }
    if(B.isEmpty(password)){
        B.error("密码不能为空");
        $('#pop-password').focus();
        return;
    }
    B.post({
        url:'/login',
        data:{username:username,password:password},
        loading:true,
        success:function(res){
            if(res.success){
                layer.msg('登录成功', {icon: 1, time: 1000}, function () {
                    top.window.location.reload();
                });
            }else{
                B.error(res.msg);
            }
        }
    });
}