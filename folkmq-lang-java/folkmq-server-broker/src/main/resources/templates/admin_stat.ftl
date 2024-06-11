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
            <td class="left" width="300px">当前流量（每秒）</td>
            <td class="left" width="300px">最大流量（每秒）</td>
            <td></td>
        </tr>
        </thead>
        <tbody id="tbody">
            <tr>
                <td class="left">输入转发消息</td>
                <td class="left">${qpsInput.lastValue}</td>
                <td class="left">${qpsInput.maxValue}</td>
            </tr>
            <tr>
                <td class="left">输出转发消息</td>
                <td class="left">${qpsOutput.lastValue}</td>
                <td class="left">${qpsOutput.maxValue}</td>
            </tr>
        </tbody>
    </table>
</datagrid>

</body>
</html>