<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 许可证</title>
    <link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
    <link rel="stylesheet" href="${css}/main.css"/>
    <script src="${js}/lib.js"></script>
    <script src="${js}/layer/layer.js"></script>
    <style>
        datagrid b{color: #8D8D8D;font-weight: normal}
    </style>

    <#if checkBtnShow>
    <script>
        function checkDo(){
            $.post('/admin/licence/ajax/check', {check:1}, data=>{
                if(data.code == 200){
                    top.layer.msg('操作成功')
                    setTimeout(function () {
                        top.location.reload();
                    }, 800);
                }else if(data.code == 401){
                    top.layer.msg(data.description);
                    setTimeout(function () {
                        top.location.reload();
                    }, 800);
                }else{
                    top.layer.msg(data.description);
                }
            });
        }
    </script>
    </#if>
</head>
<body>
<toolbar class="blockquote">
    <left>授权</left>
    <right></right>
</toolbar>
<detail>
    <form id="form">
    <table>
        <tbody>
        <tr>
            <td>许可证号</td>
            <td>${licence!}</td>
        </tr>
        <#if isAuthorized>
            <tr>
                <td>开始时间</td>
                <td>${subscribeDate!}</td>
            </tr>
            <tr>
                <td>有效时长</td>
                <td>${subscribeMonths!'0'}月</td>
            </tr>
            <tr>
                <td>授权对象</td>
                <td>${consumer!}</td>
            </tr>
        </#if>
        <tr>
            <td></td>
            <td>
                <#if checkBtnShow>
                <button class="edit" id="checkBtn" onclick="checkDo()" type="button">授权检测</button>
                </#if>
            </td>
        </tr>
        </tbody>
    </table>
    </form>
</detail>

</body>
</html>