$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");//关闭弹出框

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName,"content":content},
		function (data){
			data = $.parseJSON(data);
			if(data.code == 0){
				$("#hintBody").text("发送成功");
			}else{
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");//打开提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();//刷新页面
			}, 2000);
		}
	);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}