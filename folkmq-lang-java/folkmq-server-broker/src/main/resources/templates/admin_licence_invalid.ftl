<!DOCTYPE HTML>
<html>
<head>
    <title>${title!}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
    <link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
    <link rel="stylesheet" href="${css}/font-awesome-4.7.0/css/font-awesome.min.css" />
    <link rel="stylesheet" href="${css}/main.css"/>
    <script src="${js}/lib.js" ></script>
    <script src="${js}/layer/layer.js"></script>
    <style>
        body > header aside a{display:inline-block; height:100%; padding:0 15px; }
        body > header aside .split{border-left:1px solid #444;}
    </style>
</head>
<body>
<header>
    <label>${app}</label>
    <nav>
    </nav>
    <aside>
        <a href="https://folkmq.noear.org" target="_blank"><i class='fa fa-code-fork'></i></a>
        <a class='split' href='/login'><i class='fa fa-fw fa-circle-o-notch'></i>退出</a>
    </aside>
</header>
<main>
    <left>
        <menu>
            <div onclick="$('main').toggleClass('smlmenu');if(window.onMenuHide){window.onMenuHide();}"><i class='fa fa-bars'></i></div>
            <items>

            </items>
        </menu>
    </left>
    <right class="pad10">
        <toolbar class="blockquote">
            <left>许可证</left>
            <right></right>
        </toolbar>
        <detail>
            <form id="form">
                <div>${sn!}</div>
            </form>
        </detail>
    </right>
</main>
</body>
</html>






