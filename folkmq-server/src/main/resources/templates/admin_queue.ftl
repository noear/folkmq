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
            <td width="100px" class="center">消息数量</td>
            <td width="100px" class="center">延时1</td>
            <td width="100px" class="center">延时2</td>
            <td width="100px" class="center">延时3</td>
            <td width="80px" class="center">延时4</td>
            <td width="80px" class="center">延时5</td>
            <td width="80px" class="center">延时6</td>
            <td width="60px" class="center">延时7</td>
            <td width="60px" class="center">延时8</td>
            <td width="50px" class="center">会话</td>
            <td width="130px" class="center">状态</td>
        </tr>
        </thead>
        <tbody id="tbody">
        <#list list as item>
            <tr>
                <td class="left">${item.queue}</td>
                <td class="center">${item.messageCount}</td>
                <td class="center">${item.messageDelayedCount1}</td>
                <td class="center">${item.messageDelayedCount2}</td>
                <td class="center">${item.messageDelayedCount3}</td>
                <td class="center">${item.messageDelayedCount4}</td>
                <td class="center">${item.messageDelayedCount5}</td>
                <td class="center">${item.messageDelayedCount6}</td>
                <td class="center">${item.messageDelayedCount7}</td>
                <td class="center">${item.messageDelayedCount8}</td>
                <td class="center">${item.sessionCount}</td>
                <td class="center">${item.state}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</datagrid>

</body>
</html>