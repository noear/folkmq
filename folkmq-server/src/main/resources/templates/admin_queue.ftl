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
            <td class="left" rowspan="2">队列</td>
            <td colspan="9">消息</td>
            <td width="50px" rowspan="2" class="center">会话</td>
            <td width="130px" rowspan="2" class="center">状态</td>
        </tr>
        <tr>
            <td width="100px" class="center">总数</td>
            <td width="100px" class="center">延时1<br/>(5s)</td>
            <td width="100px" class="center">延时2<br/>(30s)</td>
            <td width="100px" class="center">延时3<br/>(3m)</td>
            <td width="80px" class="center">延时4<br/>(9m)</td>
            <td width="80px" class="center">延时5<br/>(15m)</td>
            <td width="80px" class="center">延时6<br/>(30m)</td>
            <td width="60px" class="center">延时7<br/>(1h)</td>
            <td width="60px" class="center">延时8<br/>(2h)</td>
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