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
    <script>
        $(function (){
            $("menu a").click(function (){
                $("a.sel").removeClass("sel");
                $(this).addClass("sel");
            });
        });
    </script>
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
                <a class='sel' href='/admin/session' target="dock">会话看板</a>
                <a href='/admin/server' target="dock">节点看板</a>
                <a href='/admin/topic' target="dock">主题看板</a>
                <a href='/admin/queue' target="dock">消息看板</a>
                <br/><br/>
                <a href='/admin/licence' target="dock">${licenceBtn!}</a>
            </items>
        </menu>
    </left>
    <right class="frm">
        <iframe src="/admin/session" frameborder="0" name="dock"></iframe>
    </right>
</main>
</body>
</html>






