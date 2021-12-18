//提交blog评论
function javabb_comment_blog(blogId,obj){
    var content = B.trim($(obj).siblings('.javabb-comment-textarea').val());
    if (!isLogin) {
        B.error("请先登录");
        return;
    }
    if (B.isEmpty(content)) {
        B.error("评论内容不能为空");
        return;
    }
    B.post({
        url:'/blog/comment',
        loading:true,
        data:{
            commentContent:content,
            articleId:blogId
        },
        success:function (res) {
            if(res.success){
                B.info("评论成功");
                window.location.reload();
            }else{
                B.error(res.msg);
                return;
            }
        }
    });
}
// 提交post评论
function javabb_comment_post(articleId, catalogId) {
    var content = javabb.tinymce.getContent('content')
    if (!isLogin) {
        B.error("请先登录");
        return;
    }
    if (B.isEmpty(content)) {
        B.error("回复内容不能为空");
        return;
    }
    var param = {
        articleId: articleId,
        catalogId: catalogId,
        commentContent: content,
        parentId: $('#parentId').val()
    }
    B.post({
        url: '/post/comment/',
        data: param,
        loading: true,
        success: function (result) {
            if (result.success) {
                B.info(result.msg);
                window.location.reload();
            } else {
                B.error(result.msg);
            }
        }
    })

}