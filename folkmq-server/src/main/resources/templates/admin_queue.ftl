<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 查看主题</title>
    <link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
    <link rel="stylesheet" href="${css}/main.css"/>
    <script src="${js}/lib.js"></script>
    <script src="${js}/layer/layer.js"></script>
    <style>
        datagrid b{color: #8D8D8D;font-weight: normal}
    </style>
</head>
<body>
<toolbar class="blockquote">
    <left>队列</left>
    <right></right>
</toolbar>
<datagrid class="list">
    <table>
        <thead>
        <tr>
            <td class="left">队列</td>
            <td width="200px" class="center">消息数量</td>
            <td width="200px" class="center">会话数量</td>
        </tr>
        </thead>
        <tbody id="tbody">
        <#list list as item>
            <tr>
                <td class="left">${item.queue}</td>
                <td class="center">${item.messageCount}</td>
                <td class="center">${item.sessionCount}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</datagrid>

</body>
</html>