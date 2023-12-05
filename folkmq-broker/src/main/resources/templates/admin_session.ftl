<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 查看会话</title>
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
    <left>会话</left>
    <right></right>
</toolbar>
<datagrid class="list">
    <table>
        <thead>
        <tr>
            <td width="300px" class="left">名字</td>
            <td class="left">会话数量</td>
        </tr>
        </thead>
        <tbody id="tbody">
        <#list list as item>
            <tr>
                <td class="left">${item.name}</td>
                <td class="left break">${item.sessionCount}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</datagrid>

</body>
</html>