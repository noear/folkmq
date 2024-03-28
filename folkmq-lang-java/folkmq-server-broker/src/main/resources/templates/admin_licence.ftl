<!DOCTYPE HTML>
<html class="frm10">
<head>
    <title>${app} - 许可证</title>
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
    <left>许可证</left>
    <right></right>
</toolbar>
<detail>
    <form id="form">
        <#if isAuthorized>
            <table>
                <tbody>
                <tr>
                    <td>许可证号</td>
                    <td>${sn!}</td>
                </tr>

                <tr>
                    <td>开始时间</td>
                    <td>${subscribeDate!}</td>
                </tr>
                <tr>
                    <td>有效时长</td>
                    <td>${subscribeMonths!}</td>
                </tr>
                <tr>
                    <td>授权对象</td>
                    <td>${consumer!}</td>
                </tr>

                </tbody>
            </table>
        <#else>
            <div>${sn!}</div>
        </#if>
    </form>
</detail>

</body>
</html>