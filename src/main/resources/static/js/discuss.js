function like(btn, entityType, entityId, entityUserId){//接受页面的参数
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId},//传给服务端
        function (data){
            data = $.parseJSON(data);
            if(data.code == 0){
                //成功时，改变“赞”和数量
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
            }else {
                alert(data.msg);
            }
        }
    );
}