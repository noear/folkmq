<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 队列会话</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
    <link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
    <link rel="stylesheet" href="${css}/main.css"/>
    <script src="${js}/lib.js"></script>
    <script src="${js}/layer/layer.js"></script>
    <style>
        datagrid b{color: #8D8D8D;font-weight: normal}
    </style>
    <script>
        function showSessionList(queue){
            top.layer.open({
                type: 2,
                content: 'http://h5.noear.org'
            });
        }
    </script>
</head>
<body>
<datagrid class="list">
    <table>
        <thead>
        <tr>
            <td class="left">地址</td>
        </tr>
        </thead>
        <tbody id="tbody">
        <#list list as item>
            <tr>
                <td class="left">${item}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</datagrid>

</body>
</html>