<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 流量</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
    <link rel="shortcut icon" type="image/x-icon" href="${static}/favicon.ico"/>
    <link rel="stylesheet" href="${css}/main.css"/>
    <script src="${js}/lib.js"></script>
    <script src="${js}/layer/layer.js"></script>
    <style>
        datagrid b{color: #8D8D8D;font-weight: normal}
    </style>
</head>
<body>
<toolbar class="blockquote">
    <left>流量</left>
    <right></right>
</toolbar>
<datagrid class="list">
    <table>
        <thead>
        <tr>
            <td width="200px" class="left">项目</td>
            <td class="left">最新流量</td>
            <td class="left">最大流量</td>
        </tr>
        </thead>
        <tbody id="tbody">
            <tr>
                <td class="left">接收消息</td>
                <td class="left">${qpsPublish.lastValue} / 秒</td>
                <td class="left">${qpsPublish.maxValue} / 秒</td>
            </tr>
            <tr>
                <td class="left">派发消息</td>
                <td class="left">${qpsDistribute.lastValue} / 秒</td>
                <td class="left">${qpsDistribute.maxValue} / 秒</td>
            </tr>
        </tbody>
    </table>
</datagrid>

</body>
</html>