// 提交评论
function javabb_comment_post(postId,catalogId){
    var loginUser=[[${session.sessionUser}]];
    var content = ue.getContent();
    if(B.isEmpty(loginUser)){
        B.error("请先登录")
    }
    if(B.isEmpty(content)){
        B.error("回复内容不能为空");
    }


}
