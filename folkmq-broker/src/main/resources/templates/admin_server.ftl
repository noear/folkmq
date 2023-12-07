<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 集群节点</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
    <link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
    <link rel="stylesheet" href="${css}/main.css"/>
    <script src="${js}/lib.js"></script>
    <script src="${js}/layer/layer.js"></script>
    <style>
        datagrid b{color: #8D8D8D;font-weight: normal}
    </style>
</head>
<body>
<toolbar class="blockquote">
    <left>集群节点</left>
    <right></right>
</toolbar>
<datagrid class="list">
    <table>
        <thead>
        <tr>
            <td width="300px" class="left">地址</td>
            <td class="left">后台</td>
        </tr>
        </thead>
        <tbody id="tbody">
        <#list list as item>
            <tr>
                <td class="left">${item.addree}</td>
                <td class="left break">
                    <a href="${item.adminUrl}" target="_blank">${item.adminUrl}</a>
                </td>
            </tr>
        </#list>
        </tbody>
    </table>
</datagrid>

</body>
</html>