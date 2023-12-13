<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 消息发布</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
    <link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
    <link rel="stylesheet" href="${css}/main.css"/>
    <script src="${js}/lib.js"></script>
    <script src="${js}/layer/layer.js"></script>
    <script src="${js}/laydate/laydate.js"></script>
    <style>
        datagrid b{color: #8D8D8D;font-weight: normal}
    </style>

    <script>
        function publishDo() {
            let data={};
            data.topic = $('#topic').val();
            data.scheduled = $('#scheduled').val();
            data.qos = $('#qos0').prop('checked')?0:1;
            data.content = $('#content').val().trim();

            if(!data.topic){
                top.layer.alert('请输入主题');
            }

            $.post('/admin/publish/ajax/post', data, rst => {
                if (rst.code == 200) {
                    top.layer.msg('操作成功')
                    setTimeout(function () {
                        top.location.reload();
                    }, 800);
                } else {
                    top.layer.msg(rst.description);
                }
            });
        }

        $(function (){
            //日期时间选择器
            laydate.render({
                elem: '#scheduled'
                ,type: 'datetime'
            });
        });
    </script>
</head>
<body>
<toolbar class="blockquote">
    <left>消息发布</left>
    <right></right>
</toolbar>
<detail>
    <form id="form">
        <table>
            <tbody>
            <tr>
                <th>主题</th>
                <td><input id="topic" type="text" /></td>
            </tr>
            <tr>
                <th>定时派发</th>
                <td><input id="scheduled" readonly type="text" placeholder="yyyy-MM-dd HH:mm:ss" /></td>
            </tr>
            <tr>
                <th>质量</th>
                <td>
                    <boxlist>
                        <label><input type="radio" id="qos0" name="qos" value="0" /><a>Qos0</a></label>
                        <label><input type="radio" name="qos" value="1" checked /><a>Qos1</a></label>
                    </boxlist>
                </td>
            </tr>
            <tr>
                <th>内容</th>
                <td>
                    <textarea id="content"></textarea>
                </td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <button class="edit" id="publishBtn" onclick="publishDo()" type="button">发布</button>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</detail>

</body>
</html>