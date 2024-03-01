const {FolkMQ} = require("@noear/folkmq");

async function main() {
    //客户端1
    const client1 =  await FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
        .nameAs("app1")
        .connect();

    const router = FolkMQ.newRouter(m=>m.getTag()).doOn("/test/hello",req=>{
        req.response(FolkMQ.newEntity("me to!"));
    });
    //客户端1监听
    client1.listen(router.consume.bind(router));


    //客户端2
    let client2 =  await FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
        .nameAs("app2")
        .connect();

    //客户端2发送
    let rep = await client2.send(FolkMQ.newMessage("hello").tag("/test/hello"),"app1").await();
    console.info(":::"+rep.dataAsString());
}

main();