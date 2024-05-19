<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 队列操作</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
    <link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
    <link rel="stylesheet" href="${css}/main.css"/>
    <script src="${js}/lib.js"></script>
    <script src="${js}/layer/layer.js"></script>
    <style>
        datagrid tr td{line-height: 40px; height: 40px;}
    </style>
    <script>
        $(function (){
            $('#distributeBtn').click(function (){
                if(confirm("你，确定要“强制派发”？")){
                    const loadIndex = top.layer.load(2);
                    $.ajax({
                        type: "POST",
                        url: '/admin/queue_details/ajax/distribute',
                        data: {"topic": "${topic}", "consumerGroup":"${consumerGroup}"},
                        success: function (data) {
                            top.layer.close(loadIndex);
                            if (data.code == 200) {
                                top.layer.msg('操作成功')
                                setTimeout(function () {
                                    top.layer.closeAll();
                                }, 800);
                            } else {
                                top.layer.msg(data.description);
                            }
                        },
                        error: function (data) {
                            top.layer.close(loadIndex);
                            top.layer.msg('网络请求出错...');
                        }
                    });
                }
            }) ;

            $('#clearBtn').click(function (){
                if(confirm("你，确定要“强制清空”？")){
                    const loadIndex = top.layer.load(2);
                    $.ajax({
                        type: "POST",
                        url: '/admin/queue_details/ajax/clear',
                        data: {"topic": "${topic}", "consumerGroup":"${consumerGroup}"},
                        success: function (data) {
                            top.layer.close(loadIndex);
                            if (data.code == 200) {
                                top.layer.msg('操作成功')
                                setTimeout(function () {
                                    top.layer.closeAll();
                                }, 800);
                            } else {
                                top.layer.msg(data.description);
                            }
                        },
                        error: function (data) {
                            top.layer.close(loadIndex);
                            top.layer.msg('网络请求出错...');
                        }
                    });
                }
            }) ;

            $('#deleteBtn').click(function (){
                if(confirm("你，确定要“强制删除”？")){
                    const loadIndex = top.layer.load(2);
                    $.ajax({
                        type: "POST",
                        url: '/admin/queue_details/ajax/delete',
                        data: {"topic": "${topic}", "consumerGroup":"${consumerGroup}"},
                        success: function (data) {
                            top.layer.close(loadIndex);
                            if (data.code == 200) {
                                top.layer.msg('操作成功')
                                setTimeout(function () {
                                    top.layer.closeAll();
                                }, 800);
                            } else {
                                top.layer.msg(data.description);
                            }
                        },
                        error: function (data) {
                            top.layer.close(loadIndex);
                            top.layer.msg('网络请求出错...');
                        }
                    });
                }
            }) ;
        });
    </script>
</head>
<body>
<blockquote>
    ${topic}#${consumerGroup}
</blockquote>

<datagrid class="list">
    <table>
        <tbody id="tbody">
        <tr>
            <td class="left">前面有停过消费者服务？立即派发延时消息！（不要乱点）</td>
            <td class="right"><button id="distributeBtn" class="edit">强制派发</button></td>
        </tr>
        <tr>
            <td class="left">都是无用的消息？</td>
            <td class="right"><button id="clearBtn" class="edit">强制清空</button></td>
        </tr>
        <tr>
            <td class="left">队列不要了？或者无用的？</td>
            <td class="right"><button id="deleteBtn" class="edit">强制删除</button></td>
        </tr>
        </tbody>
    </table>
</datagrid>
</body>
</html>